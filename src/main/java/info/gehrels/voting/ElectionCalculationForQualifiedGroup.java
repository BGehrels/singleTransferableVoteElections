package info.gehrels.voting;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

public class ElectionCalculationForQualifiedGroup {
	private final ImmutableCollection<Ballot> ballots;
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener electionCalculationListener;
	private final Election election;
	private AmbiguityResolver ambiguityResolver;

	public ElectionCalculationForQualifiedGroup(ImmutableCollection<Ballot> ballots,
	                                            QuorumCalculation quorumCalculation,
	                                            ElectionCalculationListener electionCalculationListener,
	                                            Election election, AmbiguityResolver ambiguityResolver) {
		this.ballots = ballots;
		this.quorumCalculation = quorumCalculation;
		this.electionCalculationListener = electionCalculationListener;
		this.election = election;
		this.ambiguityResolver = ambiguityResolver;
	}

	public ImmutableSet<Candidate> calculate(ImmutableSet<Candidate> qualifiedCandidates, int numberOfSeats) {
		int numberOfValidBallots = ballots.size();
		// Runden oder nicht runden?
		double quorum = quorumCalculation.calculateQuorum(numberOfValidBallots, numberOfSeats);
		electionCalculationListener.quorumHasBeenCalculated(numberOfValidBallots, numberOfSeats, quorum);

		ImmutableCollection<BallotState> ballotStates = constructBallotStates();
		ImmutableMap<Candidate, CandidateState> candidateStates = constructCandidateStates(qualifiedCandidates);

		int numberOfElectedCandidates = 0;

		electionCalculationListener
			.calculationStarted(true, election, calculateVotesByCandidate(candidateStates, ballotStates));

		while (notAllSeatsFilled(numberOfElectedCandidates, numberOfSeats) && anyCandidateIsHopeful(
			candidateStates)) {
			Candidate winner = bestCandidateThatReachedTheQuorum(quorum, candidateStates, ballotStates);
			if (winner != null) {
				electionCalculationListener
					.candidateIsElected(winner, calculateVotesForCandidate(winner, ballotStates), quorum);

				numberOfElectedCandidates++;
				redistributeExceededVoteWeight(winner, quorum, ballotStates
				);
				candidateStates.get(winner).setElected();
				electionCalculationListener.voteWeightRedistributionCompleted(
					calculateVotesByCandidate(candidateStates, ballotStates));

			} else {
				electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
				strikeWeakestCandidate(candidateStates, ballotStates);
			}
		}

		ImmutableSet<Candidate> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return electedCandidates;
	}

	private ImmutableCollection<BallotState> constructBallotStates() {
		ImmutableList.Builder<BallotState> builder = ImmutableList.builder();
		return builder.addAll(transform(ballots, new Function<Ballot, BallotState>() {
			@Override
			public BallotState apply(Ballot ballot) {
				return new BallotState(ballot);
			}
		})).build();
	}

	private ImmutableMap<Candidate, CandidateState> constructCandidateStates(
		ImmutableSet<Candidate> qualifiedCandidates) {
		Builder<Candidate, CandidateState> builder = ImmutableMap.builder();
		for (Candidate candidate : qualifiedCandidates) {
			builder.put(candidate, new CandidateState(candidate));
		}
		return builder.build();
	}

	private Map<Candidate, Double> calculateVotesByCandidate(Map<Candidate, CandidateState> candidateStates,
	                                                         Collection<BallotState> ballotStates) {
		Map<Candidate, Double> votesByCandidateDraft = new HashMap<>();
		for (BallotState ballotState : ballotStates) {
			CandidateState preferredHopefulCandidate = getPreferredHopefulCandidate(candidateStates, ballotState);
			if (preferredHopefulCandidate == null) {
				continue;
			}

			Double votes = votesByCandidateDraft.get(preferredHopefulCandidate.candidate);
			if (votes == null) {
				votesByCandidateDraft.put(preferredHopefulCandidate.candidate, ballotState.getVoteWeight());
			} else {
				votesByCandidateDraft.put(preferredHopefulCandidate.candidate, votes + ballotState.getVoteWeight());
			}
		}

		Builder<Candidate, Double> builder = ImmutableMap.builder();
		for (Candidate candidate : candidateStates.keySet()) {
			CandidateState candidateState = candidateStates.get(candidate);
			if (candidateState != null && !candidateState.isHopeful()) {
				continue;
			}

			Double votes = votesByCandidateDraft.get(candidate);
			builder.put(candidate, votes == null ? 0.0 : votes);
		}

		return builder.build();
	}


	private boolean notAllSeatsFilled(int numberOfElectedCandidates, int numberOfSeatsToElect) {

		boolean notAllSeatsFilled = numberOfElectedCandidates < numberOfSeatsToElect;
		electionCalculationListener.numberOfElectedPositions(numberOfElectedCandidates, numberOfSeatsToElect);
		return notAllSeatsFilled;
	}

	private boolean anyCandidateIsHopeful(ImmutableMap<Candidate, CandidateState> candidateStates) {
		for (CandidateState candidateState : candidateStates.values()) {
			if (candidateState.isHopeful()) {
				return true;
			}
		}

		electionCalculationListener.noCandidatesAreLeft();
		return false;
	}

	private Candidate bestCandidateThatReachedTheQuorum(double quorum,
	                                                    ImmutableMap<Candidate, CandidateState> candidateStates,
	                                                    ImmutableCollection<BallotState> ballotStates) {
		Map<Candidate, Double> votesByCandidate = calculateVotesByCandidate(candidateStates, ballotStates);
		double numberOfVotesOfBestCandidate = -1;
		Collection<Candidate> bestCandidates = newArrayList();
		for (Entry<Candidate, Double> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue() >= quorum) {
				if (votesForCandidate.getValue() > numberOfVotesOfBestCandidate) {
					numberOfVotesOfBestCandidate = votesForCandidate.getValue();
					bestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
				} else if (votesForCandidate.getValue() == numberOfVotesOfBestCandidate) {
					bestCandidates.add(votesForCandidate.getKey());
				}
			}
		}


		// TODO: Ist ambiguity resolution hier überhaupt nötig?
		return chooseOneOutOfManyCandidates(copyOf(bestCandidates));
	}

	private Candidate chooseOneOutOfManyCandidates(ImmutableSet<Candidate> candidates) {
		Candidate winner = null;

		if (candidates.size() == 1) {
			return candidates.iterator().next();
		} else if (candidates.size() > 1) {
			electionCalculationListener.delegatingToExternalAmbiguityResolution(candidates);
			AmbiguityResolverResult ambiguityResolverResult = ambiguityResolver.chooseOneOfMany(candidates);
			electionCalculationListener.externalyResolvedAmbiguity(ambiguityResolverResult);
			winner = ambiguityResolverResult.choosenCandidate;
		}

		return winner;
	}

	private double calculateVotesForCandidate(Candidate candidate, Collection<BallotState> ballotStates) {
		double votes = 0;
		for (BallotState ballotState : ballotStates) {
			if (ballotState.getPreferredCandidate() == candidate) {
				votes += ballotState.getVoteWeight();
			}
		}

		return votes;
	}

	private void redistributeExceededVoteWeight(Candidate candidate, double quorum,
	                                            Collection<BallotState> ballotStates) {
		double votesForCandidate = calculateVotesForCandidate(candidate, ballotStates);
		double excessiveVotes = votesForCandidate - quorum;
		double excessiveFractionOfVoteWeight = excessiveVotes / votesForCandidate;

		for (BallotState ballotState : ballotStates) {
			if (ballotState.getPreferredCandidate() == candidate) {
				ballotState.reduceVoteWeight(excessiveFractionOfVoteWeight);

				electionCalculationListener.voteWeightRedistributed(excessiveFractionOfVoteWeight,
				                                                    ballotState.ballotId, ballotState.getVoteWeight());
			}
		}

	}

	private void strikeWeakestCandidate(Map<Candidate, CandidateState> candidateStates,
	                                    Collection<BallotState> ballotStates) {
		Map<Candidate, Double> votesByCandidateBeforeStriking = calculateVotesByCandidate(candidateStates,
		                                                                                  ballotStates);

		Candidate weakestCandidate = calculateWeakestCandidate(votesByCandidateBeforeStriking);

		// TODO: Mehrdeutigkeiten bei Schwächsten Kandidaten extern auswählen lassen
		candidateStates.get(weakestCandidate).setLooser();

		Map<Candidate, Double> votesByCandidateAfterStriking = calculateVotesByCandidate(candidateStates, ballotStates);

		electionCalculationListener.candidateDropped(
			votesByCandidateBeforeStriking,
			weakestCandidate.name,
			votesByCandidateBeforeStriking.get(weakestCandidate),
			votesByCandidateAfterStriking);
	}

	private Candidate calculateWeakestCandidate(Map<Candidate, Double> votesByCandidate) {
		double numberOfVotesOfBestCandidate = Double.MAX_VALUE;
		//TODO: Hier sollten eigentlich 0-Kandidierende noch aufgeführt werden, solange sie nicht bereits gedroppd sind.
		Collection<Candidate> weakestCandidates = newArrayList();
		for (Entry<Candidate, Double> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue() < numberOfVotesOfBestCandidate) {
				numberOfVotesOfBestCandidate = votesForCandidate.getValue();
				weakestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
			} else if (votesForCandidate.getValue() == numberOfVotesOfBestCandidate) {
				weakestCandidates.add(votesForCandidate.getKey());
			}
		}

		return chooseOneOutOfManyCandidates(copyOf(weakestCandidates));
	}

	private ImmutableSet<Candidate> getElectedCandidates(ImmutableMap<Candidate, CandidateState> candidateStates) {
		ImmutableSet.Builder<Candidate> builder = ImmutableSet.builder();

		for (CandidateState candidateState : candidateStates.values()) {
			if (candidateState.elected) {
				builder.add(candidateState.candidate);
			}
		}
		return builder.build();
	}


	private CandidateState getPreferredHopefulCandidate(Map<Candidate, CandidateState> candidateStates,
	                                                    BallotState ballotState) {
		Candidate preferredCandidate = ballotState.getPreferredCandidate();
		while (preferredCandidate != null) {
			CandidateState candidateState = candidateStates.get(preferredCandidate);
			if (candidateState != null && candidateState.isHopeful()) {
				return candidateState;
			}

			preferredCandidate = ballotState.proceedToNextPreference();
		}

		return null;
	}


	class BallotState {
		public int ballotId;
		private double voteWeight;
		private Iterator<Candidate> ballotIterator;
		private Candidate candidateOfCurrentPreference;

		public BallotState(Ballot ballot) {
			this.ballotId = ballot.id;
			this.ballotIterator = ballot.getRankedCandidatesByElection(election).iterator();
			proceedToNextPreference();

			voteWeight = 1;
		}

		public Candidate getPreferredCandidate() {
			return candidateOfCurrentPreference;
		}

		public double getVoteWeight() {
			return voteWeight;
		}

		public Candidate proceedToNextPreference() {
			candidateOfCurrentPreference = null;
			if (ballotIterator.hasNext()) {
				candidateOfCurrentPreference = ballotIterator.next();
			}

			return candidateOfCurrentPreference;
		}

		public void reduceVoteWeight(double fractionOfExcessiveVotes) {
			voteWeight *= fractionOfExcessiveVotes;
		}

	}

	private class CandidateState {
		private final Candidate candidate;
		private boolean elected = false;
		private boolean looser = false;


		public CandidateState(Candidate candidate) {
			this.candidate = candidate;
		}

		public boolean isHopeful() {
			return !elected && !looser;
		}

		public void setElected() {
			this.elected = true;
		}

		public void setLooser() {
			this.looser = true;
		}
	}


}
