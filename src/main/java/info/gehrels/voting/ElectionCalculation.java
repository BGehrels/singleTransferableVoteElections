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
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

public class ElectionCalculation {
	private final Election election;
	private final ImmutableCollection<Ballot> ballots;
	private final int numberOfFemaleSeats;
	private final int numberOfOpenSeats;
	private final AmbiguityResolver ambiguityResolver;
	private final ElectionCalculationListener electionCalculationListener;

	public ElectionCalculation(Election election, ImmutableCollection<Ballot> ballots,
	                           AmbiguityResolver ambiguityResolver,
	                           ElectionCalculationListener electionCalculationListener) {
		this.election = election;
		this.ballots = ballots;
		this.electionCalculationListener = electionCalculationListener;
		this.numberOfFemaleSeats = election.numberOfFemaleExclusivePositions;
		this.numberOfOpenSeats = election.numberOfNotFemaleExclusivePositions;
		this.ambiguityResolver = ambiguityResolver;
	}

	public ElectionResult calculateElectionResult() {
		int numberOfValidBallots = ballots.size();
		// Runden oder nicht runden?
		// Satzungsmäßig klarstellen, dass eigenes Quorum für Frauen und nicht Frauen...
		//double femaleQuorum = numberOfValidBallots / (numberOfFemaleSeats + 1) + 1;
		//double nonFemaleQuorum = numberOfValidBallots / (numberOfOpenSeats + 1) + 1;
		double femaleQuorum = numberOfValidBallots / (numberOfFemaleSeats + 1.0);
		double nonFemaleQuorum = numberOfValidBallots / (numberOfOpenSeats + 1.0);

		electionCalculationListener.quorumHasBeenCalculated(true, femaleQuorum);
		electionCalculationListener.quorumHasBeenCalculated(false, nonFemaleQuorum);

		ImmutableCollection<BallotState> ballotStates = constructBallotStates();
		ImmutableMap<Candidate, CandidateState> candidateStates = constructCandidateStates();

		int numberOfElectedFemaleCandidates = 0;

		while (notAllSeatsFilled(numberOfElectedFemaleCandidates, true) && anyCandidateIsHopeful(true,
		                                                                                         candidateStates)) {
			Candidate candidate = bestCandidateThatReachedTheQuorum(femaleQuorum, true, candidateStates, ballotStates);
			if (candidate != null) {
				numberOfElectedFemaleCandidates++;
				redistributeExceededVoteWeight(candidate, femaleQuorum, ballotStates);
				candidateStates.get(candidate).setElected();
			} else {
				strikeWeakestCandidate(true, candidateStates, ballotStates);
			}
		}

		resetLoosers(candidateStates);
		resetBallotStates(ballotStates);


		// TODO: reduce numberOfElectedOpenCandidates, if not enough women were elected.
		int numberOfElectedOpenCandidates = 0;
		while (notAllSeatsFilled(numberOfElectedOpenCandidates, false) && anyCandidateIsHopeful(false,
		                                                                                        candidateStates)) {
			Candidate candidate = bestCandidateThatReachedTheQuorum(nonFemaleQuorum, false, candidateStates,
			                                                        ballotStates);
			if (candidate != null) {
				redistributeExceededVoteWeight(candidate, nonFemaleQuorum, ballotStates);
				candidateStates.get(candidate).setElected();
				numberOfElectedOpenCandidates++;
			} else {
				strikeWeakestCandidate(false, candidateStates, ballotStates);
			}
		}

		ImmutableSet<Candidate> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return new ElectionResult(electedCandidates);
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

	private boolean notAllSeatsFilled(int numberOfElectedCandidates, boolean female) {
		int numberOfSeatsToElect = female ? numberOfFemaleSeats : numberOfOpenSeats;
		boolean notAllSeatsFilled = numberOfElectedCandidates < numberOfSeatsToElect;
		electionCalculationListener.numberOfElectedPositions(female, numberOfElectedCandidates, numberOfSeatsToElect);
		return notAllSeatsFilled;
	}

	private void resetBallotStates(Collection<BallotState> ballotStates) {
		for (BallotState ballotState : ballotStates) {
			ballotState.reset();
		}
	}

	private void resetLoosers(Map<Candidate, CandidateState> candidateStates) {
		for (CandidateState candidateState : candidateStates.values()) {
			candidateState.resetLooser();
		}
	}

	private void strikeWeakestCandidate(boolean onlyFemaleCandidates, Map<Candidate, CandidateState> candidateStates,
	                                    Collection<BallotState> ballotStates) {
		Map<Candidate, Double> candidateDoubleMap = calculateVotesByCandidate(onlyFemaleCandidates, candidateStates,
		                                                                      ballotStates);
		Candidate weakestCandidate = null;
		Double weakestVoteCount = null;
		for (Entry<Candidate, Double> votesForCandidate : candidateDoubleMap.entrySet()) {
			if (weakestVoteCount == null || votesForCandidate.getValue() < weakestVoteCount) {
				weakestCandidate = votesForCandidate.getKey();
				weakestVoteCount = votesForCandidate.getValue();
			}
		}

		// TODO: Mehrdeutigkeiten bei Schwächsten Kandidaten extern auswählen lassen
		electionCalculationListener.candidateDropped(weakestCandidate.name, weakestVoteCount);
		candidateStates.get(weakestCandidate).setLooser();
	}

	private void redistributeExceededVoteWeight(Candidate candidate, double quorum,
	                                            Collection<BallotState> ballotStates) {
		double votesForCandidate = calculateVotesForCandidate(candidate, ballotStates);
		double excessiveVotes = votesForCandidate - quorum;
		double excessiveFractionOfVoteWeight = excessiveVotes / votesForCandidate;
		System.out.println("Es werden " + excessiveFractionOfVoteWeight * 100 + "% der Stimmen weiterverteilt");

		for (BallotState ballotState : ballotStates) {
			if (ballotState.getPreferredCandidate() == candidate) {
				ballotState.reduceVoteWeight(excessiveFractionOfVoteWeight);

				electionCalculationListener.voteWeightRedistributed(excessiveFractionOfVoteWeight,
				                                                    ballotState.ballot, ballotState.getVoteWeight());
			}
		}
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

	private Candidate bestCandidateThatReachedTheQuorum(double quorum, boolean onlyFemaleCandidates,
	                                                    ImmutableMap<Candidate, CandidateState> candidateStates,
	                                                    ImmutableCollection<BallotState> ballotStates) {
		Map<Candidate, Double> votesByCandidate = calculateVotesByCandidate(onlyFemaleCandidates, candidateStates,
		                                                                    ballotStates);
		double numberOfVotesOfBestCandidate = -1;
		Collection<Candidate> bestCandidates = newArrayList();
		for (Entry<Candidate, Double> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue() > quorum) {
				System.out.println(votesForCandidate.getKey().name + " hat mit " + votesForCandidate.getValue()
				                   + " Stimmen das Quorum erreicht.");
				if (votesForCandidate.getValue() > numberOfVotesOfBestCandidate) {
					numberOfVotesOfBestCandidate = votesForCandidate.getValue();
					bestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
				} else if (votesForCandidate.getValue() == numberOfVotesOfBestCandidate) {
					bestCandidates.add(votesForCandidate.getKey());
				}
			}
		}


		// TODO: Ist ambiguity resolution hier überhaupt nötig?
		Candidate winner = chooseOneOutOfManyCandidates(
			ImmutableSet.copyOf(bestCandidates));
		if (winner != null) {
			System.out.println(winner.name + " hat das Quorum erreicht und ist gewählt");
		} else {
			System.out.println("Kein_e Kandidierende_r hat das Quorum erreicht.");
		}
		return winner;
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

	private boolean anyCandidateIsHopeful(boolean onlyFemaleCandidates,
	                                      ImmutableMap<Candidate, CandidateState> candidateStates) {
		for (CandidateState candidateState : candidateStates.values()) {
			if (isAcceptableCandidate(onlyFemaleCandidates, candidateState.candidate) && candidateState.isHopeful()) {
				System.out.println("Es gibt noch hoffnungsvolle Kandidierende");
				return true;
			}
		}
		System.out.println("Es gibt keine hoffnungsvollen Kandidierende mehr");
		return false;
	}

	private Map<Candidate, Double> calculateVotesByCandidate(boolean onlyFemaleCandidates,
	                                                         Map<Candidate, CandidateState> candidateStates,
	                                                         Collection<BallotState> ballotStates) {
		Map<Candidate, Double> votesByCandidate = new HashMap<>();
		for (BallotState ballotState : ballotStates) {
			CandidateState preferredHopefulCandidate = getPreferredHopefulCandidate(candidateStates, ballotState,
			                                                                        onlyFemaleCandidates);
			if (preferredHopefulCandidate == null) {
				continue;
			}

			Double votes = votesByCandidate.get(preferredHopefulCandidate.candidate);
			if (votes == null) {
				votesByCandidate.put(preferredHopefulCandidate.candidate, ballotState.getVoteWeight());
			} else {
				votesByCandidate.put(preferredHopefulCandidate.candidate, votes + ballotState.getVoteWeight());
			}
		}

		System.out.println("Die Stimmen verteilen sich wie folgt:");
		for (Entry<Candidate, Double> candidateDoubleEntry : votesByCandidate.entrySet()) {
			System.out.println(
				"\t" + candidateDoubleEntry.getKey().name + ": " + candidateDoubleEntry.getValue() + " Stimmen");
		}
		return votesByCandidate;
	}


	private CandidateState getPreferredHopefulCandidate(Map<Candidate, CandidateState> candidateStates,
	                                                    BallotState ballotState, boolean onlyFemaleCandidates) {
		Candidate preferredCandidate = ballotState.getPreferredCandidate();
		while (preferredCandidate != null) {
			CandidateState candidateState = candidateStates.get(preferredCandidate);
			if (isAcceptableCandidate(onlyFemaleCandidates, preferredCandidate) && candidateState.isHopeful()) {
				return candidateState;
			}

			preferredCandidate = ballotState.proceedToNextPreference();
		}

		return null;
	}

	private boolean isAcceptableCandidate(boolean onlyFemaleCandidates, Candidate candidate) {
		return !onlyFemaleCandidates || candidate.isFemale;
	}

	private ImmutableMap<Candidate, CandidateState> constructCandidateStates() {
		Builder<Candidate, CandidateState> builder = ImmutableMap.builder();
		for (Candidate candidate : election.candidates) {
			builder.put(candidate, new CandidateState(candidate));
		}
		return builder.build();
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

	private static class ElectionResult {
		public final ImmutableSet<Candidate> electedCandidates;

		private ElectionResult(ImmutableSet<Candidate> electedCandidates) {
			this.electedCandidates = electedCandidates;
		}
	}

	class BallotState {
		final Ballot ballot;
		private double voteWeigt;
		private Iterator<Candidate> ballotIterator;
		private Candidate candidateOfCurrentPreference;

		public BallotState(Ballot ballot) {
			this.ballot = ballot;
			reset();
		}

		public Candidate getPreferredCandidate() {
			return candidateOfCurrentPreference;
		}

		public double getVoteWeight() {
			return voteWeigt;
		}

		public Candidate proceedToNextPreference() {
			candidateOfCurrentPreference = null;
			if (ballotIterator.hasNext()) {
				candidateOfCurrentPreference = ballotIterator.next();
			}

			return candidateOfCurrentPreference;
		}

		public void reduceVoteWeight(double fractionOfExcessiveVotes) {
			voteWeigt *= fractionOfExcessiveVotes;
		}

		public void reset() {
			this.ballotIterator = ballot.getRankedCandidatesByElection(election).iterator();
			proceedToNextPreference();

			voteWeigt = 1;
		}
	}

	private class CandidateState {
		private final Candidate candidate;
		private boolean elected;
		private boolean looser;


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

		public void resetLooser() {
			this.looser = false;
		}
	}
}
