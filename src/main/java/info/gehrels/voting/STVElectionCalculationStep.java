package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.VotesByCandidateCalculation.calculateVotesByCandidate;
import static info.gehrels.voting.VotesForCandidateCalculation.calculateVotesForCandidate;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class STVElectionCalculationStep {

	private final ElectionCalculationListener electionCalculationListener;
	private final AmbiguityResolver ambiguityResolver;

	public STVElectionCalculationStep(ElectionCalculationListener electionCalculationListener,
	                                  AmbiguityResolver ambiguityResolver) {
		this.ambiguityResolver = validateThat(ambiguityResolver, is(not(nullValue())));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(not(nullValue())));
	}

	public ElectionStepResult declareWinnerOrStrikeCandidate(BigFraction quorum,
	                                                         ImmutableCollection<BallotState> ballotStates,
	                                                         VoteWeightRedistributor redistributor,
	                                                         int numberOfElectedCandidates,
	                                                         CandidateStates candidateStates) {
		Candidate winner = bestCandidateThatReachedTheQuorum(quorum, ballotStates,
		                                                     candidateStates);
		if (winner != null) {
			return calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(quorum,
			                                                                           ballotStates,
			                                                                           redistributor,
			                                                                           numberOfElectedCandidates,
			                                                                           winner,
			                                                                           candidateStates);
		} else {
			return calculateElectionStepResultByStrikingTheWeakestCandidate(quorum, ballotStates,
			                                                                numberOfElectedCandidates,
			                                                                candidateStates);
		}
	}

	private Candidate bestCandidateThatReachedTheQuorum(BigFraction quorum,
	                                                    ImmutableCollection<BallotState> ballotStates,
	                                                    CandidateStates candidateStates) {
		Map<Candidate, BigFraction> votesByCandidate =
			calculateVotesByCandidate(candidateStates.getHopefulCandidates(), ballotStates);
		BigFraction numberOfVotesOfBestCandidate = BigFraction.MINUS_ONE;
		Collection<Candidate> bestCandidates = newArrayList();
		for (Entry<Candidate, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(quorum) >= 0) {
				if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) > 0) {
					numberOfVotesOfBestCandidate = votesForCandidate.getValue();
					bestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
				} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
					bestCandidates.add(votesForCandidate.getKey());
				}
			}
		}


		// TODO: Ist ambiguity resolution hier überhaupt nötig?
		return chooseOneOutOfManyCandidates(copyOf(bestCandidates));
	}


	private ElectionStepResult calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(BigFraction quorum,
	                                                                                               ImmutableCollection<BallotState> ballotStates,
	                                                                                               VoteWeightRedistributor redistributor,
	                                                                                               int numberOfElectedCandidates,
	                                                                                               Candidate winner,
	                                                                                               CandidateStates candidateStates) {
		electionCalculationListener
			.candidateIsElected(winner, calculateVotesForCandidate(winner, ballotStates), quorum);

		int newNumberOfElectedCandidates = numberOfElectedCandidates + 1;
		ballotStates = redistributor.redistributeExceededVoteWeight(winner, quorum, ballotStates);

		CandidateStates newCandidateStates = candidateStates.withElected(winner);
		ImmutableCollection<BallotState> newBallotStates = createBallotStatesPointingAtNextHopefulCandidate(
			ballotStates, newCandidateStates);

		electionCalculationListener.voteWeightRedistributionCompleted(
			calculateVotesByCandidate(newCandidateStates.getHopefulCandidates(), newBallotStates));

		return new ElectionStepResult(newBallotStates, newNumberOfElectedCandidates, newCandidateStates);
	}

	private ElectionStepResult calculateElectionStepResultByStrikingTheWeakestCandidate(BigFraction quorum,
	                                                                                    ImmutableCollection<BallotState> ballotStates,
	                                                                                    int numberOfElectedCandidates,
	                                                                                    CandidateStates candidateStates) {

		electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
		State state = strikeWeakestCandidate(ballotStates, candidateStates);
		return new ElectionStepResult(state.ballotStates, numberOfElectedCandidates, state.candidateStates);
	}

	private State strikeWeakestCandidate(
		ImmutableCollection<BallotState> ballotStates, CandidateStates candidateStates) {

		Map<Candidate, BigFraction> votesByCandidateBeforeStriking = calculateVotesByCandidate(
			candidateStates.getHopefulCandidates(), ballotStates);

		Candidate weakestCandidate = calculateWeakestCandidate(votesByCandidateBeforeStriking);

		// TODO: Mehrdeutigkeiten bei Schwächsten Kandidaten extern auswählen lassen
		candidateStates = candidateStates.withLooser(weakestCandidate);
		ballotStates = createBallotStatesPointingAtNextHopefulCandidate(ballotStates, candidateStates);

		Map<Candidate, BigFraction> votesByCandidateAfterStriking =
			calculateVotesByCandidate(candidateStates.getHopefulCandidates(), ballotStates);

		electionCalculationListener.candidateDropped(
			votesByCandidateBeforeStriking,
			weakestCandidate,
			votesByCandidateBeforeStriking.get(weakestCandidate),
			votesByCandidateAfterStriking);
		return new State(candidateStates, ballotStates);
	}


	private Candidate calculateWeakestCandidate(Map<Candidate, BigFraction> votesByCandidate) {
		BigFraction numberOfVotesOfBestCandidate = new BigFraction(Integer.MAX_VALUE, 1);
		//TODO: Hier sollten eigentlich 0-Kandidierende noch aufgeführt werden, solange sie nicht bereits gedroppd sind.
		Collection<Candidate> weakestCandidates = newArrayList();
		for (Entry<Candidate, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) < 0) {
				numberOfVotesOfBestCandidate = votesForCandidate.getValue();
				weakestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
			} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
				weakestCandidates.add(votesForCandidate.getKey());
			}
		}

		return chooseOneOutOfManyCandidates(copyOf(weakestCandidates));
	}

	private ImmutableCollection<BallotState> createBallotStatesPointingAtNextHopefulCandidate(
		ImmutableCollection<BallotState> ballotStates, CandidateStates candidateStates) {
		ImmutableList.Builder<BallotState> resultBuilder = ImmutableList.builder();
		for (BallotState ballotState : ballotStates) {
			resultBuilder.add(ballotState.withFirstHopefulCandidate(candidateStates));
		}

		return resultBuilder.build();
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

	public class ElectionStepResult {
		public final CandidateStates newCandidateStates;
		public final ImmutableCollection<BallotState> newBallotStates;
		public final int newNumberOfElectedCandidates;

		public ElectionStepResult(ImmutableCollection<BallotState> newBallotStates, int newNumberOfElectedCandidates,
		                          CandidateStates candidateStates) {
			this.newCandidateStates = candidateStates;
			this.newBallotStates = newBallotStates;
			this.newNumberOfElectedCandidates = newNumberOfElectedCandidates;
		}
	}

	private class State {
		private final CandidateStates candidateStates;
		private final ImmutableCollection<BallotState> ballotStates;

		public State(CandidateStates candidateStates,
		             ImmutableCollection<BallotState> ballotStates) {
			this.candidateStates = candidateStates;
			this.ballotStates = ballotStates;
		}
	}
}
