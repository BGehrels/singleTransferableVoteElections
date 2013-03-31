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
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static info.gehrels.voting.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import static java.util.Arrays.asList;

public class STVElectionCalculation {
	private final ImmutableCollection<Ballot> ballots;
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener electionCalculationListener;
	private final Election election;
	private final AmbiguityResolver ambiguityResolver;
	private final VoteWeightRedistributionMethod voteWeightRedistributionMethod;

	public STVElectionCalculation(ImmutableCollection<Ballot> ballots,
	                              QuorumCalculation quorumCalculation,
	                              ElectionCalculationListener electionCalculationListener,
	                              Election election, AmbiguityResolver ambiguityResolver,
	                              VoteWeightRedistributionMethod redistributionMethod) {
		this.ballots = ballots;
		this.quorumCalculation = quorumCalculation;
		this.electionCalculationListener = electionCalculationListener;
		this.election = election;
		this.ambiguityResolver = ambiguityResolver;
		voteWeightRedistributionMethod = redistributionMethod;
	}

	public ImmutableSet<Candidate> calculate(ImmutableSet<? extends Candidate> qualifiedCandidates, int numberOfSeats) {
		VoteWeightRedistributor redistributor = voteWeightRedistributionMethod.redistributorFor();
		int numberOfValidBallots = ballots.size();
		// Runden oder nicht runden?
		double quorum = quorumCalculation.calculateQuorum(numberOfValidBallots, numberOfSeats);
		electionCalculationListener.quorumHasBeenCalculated(numberOfValidBallots, numberOfSeats, quorum);

		ImmutableMap<Candidate, CandidateState> candidateStates = constructCandidateStates(qualifiedCandidates);
		ImmutableCollection<BallotState> ballotStates = constructBallotStates(candidateStates);

		int numberOfElectedCandidates = 0;

		electionCalculationListener
			.calculationStarted(election, calculateVotesByCandidate(candidateStates, ballotStates));

		while (notAllSeatsFilled(numberOfElectedCandidates, numberOfSeats) && anyCandidateIsHopeful(
			candidateStates)) {
			Candidate winner = bestCandidateThatReachedTheQuorum(quorum, candidateStates, ballotStates);
			if (winner != null) {
				electionCalculationListener
					.candidateIsElected(winner, calculateVotesForCandidate(winner, ballotStates), quorum);

				numberOfElectedCandidates++;
				ballotStates = redistributor.redistributeExceededVoteWeight(winner, quorum, ballotStates);
				candidateStates.get(winner).setElected();
				ballotStates = createBallotStatesPointingAtNextHopefulCandidate(ballotStates, candidateStates);
				electionCalculationListener.voteWeightRedistributionCompleted(
					calculateVotesByCandidate(candidateStates, ballotStates));

			} else {
				electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
				strikeWeakestCandidate(candidateStates, ballotStates);
				ballotStates = createBallotStatesPointingAtNextHopefulCandidate(ballotStates, candidateStates);
			}
		}

		ImmutableSet<Candidate> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return electedCandidates;
	}

	private ImmutableCollection<BallotState> createBallotStatesPointingAtNextHopefulCandidate(
		ImmutableCollection<BallotState> ballotStates,
		ImmutableMap<Candidate, CandidateState> candidateStates) {
		ImmutableList.Builder<BallotState> resultBuilder = ImmutableList.builder();
		for (BallotState ballotState : ballotStates) {
			resultBuilder.add(createBallotStatePointingAtNextHopefulCandidate(candidateStates, ballotState));
		}

		return resultBuilder.build();
	}

	private ImmutableCollection<BallotState> constructBallotStates(final Map<Candidate, CandidateState> candidateStates) {
		ImmutableList.Builder<BallotState> builder = ImmutableList.builder();
		return builder.addAll(transform(ballots, new Function<Ballot, BallotState>() {
			@Override
			public BallotState apply(Ballot ballot) {
				return createBallotStatePointingAtNextHopefulCandidate(candidateStates, new BallotState(ballot, election));
			}
		})).build();
	}


	private ImmutableMap<Candidate, CandidateState> constructCandidateStates(
		ImmutableSet<? extends Candidate> qualifiedCandidates) {
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
			Candidate preferredHopefulCandidate = ballotState.getPreferredCandidate();
			if (preferredHopefulCandidate == null) {
				continue;
			}

			Double votes = votesByCandidateDraft.get(preferredHopefulCandidate);
			if (votes == null) {
				votesByCandidateDraft.put(preferredHopefulCandidate, ballotState.getVoteWeight());
			} else {
				votesByCandidateDraft.put(preferredHopefulCandidate, votes + ballotState.getVoteWeight());
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


	private BallotState createBallotStatePointingAtNextHopefulCandidate(
		Map<Candidate, CandidateState> candidateStates,	BallotState ballotState) {
		BallotState result = ballotState;

		Candidate preferredCandidate = result.getPreferredCandidate();
		while (preferredCandidate != null) {

			CandidateState candidateState = candidateStates.get(preferredCandidate);
			if (candidateState != null && candidateState.isHopeful()) {
				return result;
			}

			result = result.withNextPreference();
			preferredCandidate = result.getPreferredCandidate();
		}

		return result;
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

		@Override
		public String toString() {
			return candidate.toString() + ": " + (elected ? "" : "not ") + "elected, " + (looser ? "" : "no ") + "looser";
		}
	}


}
