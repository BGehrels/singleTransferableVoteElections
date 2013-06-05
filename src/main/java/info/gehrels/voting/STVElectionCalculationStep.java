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

public class STVElectionCalculationStep<CANDIDATE_TYPE extends Candidate> {

	private final ElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;
	private final AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver;

	public STVElectionCalculationStep(ElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener,
	                                  AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver) {
		this.ambiguityResolver = validateThat(ambiguityResolver, is(not(nullValue())));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(not(nullValue())));
	}

	public ElectionStepResult<CANDIDATE_TYPE> declareWinnerOrStrikeCandidate(BigFraction quorum,
	                                                         ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates,
	                                                         VoteWeightRedistributor<CANDIDATE_TYPE> redistributor,
	                                                         int numberOfElectedCandidates,
	                                                         CandidateStates<CANDIDATE_TYPE> candidateStates) {
		CANDIDATE_TYPE winner = bestCandidateThatReachedTheQuorum(quorum, ballotStates, candidateStates);
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

	private CANDIDATE_TYPE bestCandidateThatReachedTheQuorum(BigFraction quorum,
	                                                    ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates,
	                                                    CandidateStates<CANDIDATE_TYPE> candidateStates) {
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidate =
			calculateVotesByCandidate(candidateStates.getHopefulCandidates(), ballotStates);
		BigFraction numberOfVotesOfBestCandidate = BigFraction.MINUS_ONE;
		Collection<CANDIDATE_TYPE> bestCandidates = newArrayList();
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
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


	private ElectionStepResult<CANDIDATE_TYPE> calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(BigFraction quorum,
	                                                                                               ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates,
	                                                                                               VoteWeightRedistributor<CANDIDATE_TYPE> redistributor,
	                                                                                               int numberOfElectedCandidates,
	                                                                                               CANDIDATE_TYPE winner,
	                                                                                               CandidateStates<CANDIDATE_TYPE> candidateStates) {
		electionCalculationListener
			.candidateIsElected(winner, calculateVotesForCandidate(winner, ballotStates), quorum);

		int newNumberOfElectedCandidates = numberOfElectedCandidates + 1;
		ballotStates = redistributor.redistributeExceededVoteWeight(winner, quorum, ballotStates);

		CandidateStates<CANDIDATE_TYPE> newCandidateStates = candidateStates.withElected(winner);
		ImmutableCollection<BallotState<CANDIDATE_TYPE>> newBallotStates = createBallotStatesPointingAtNextHopefulCandidate(
			ballotStates, newCandidateStates);

		electionCalculationListener.voteWeightRedistributionCompleted(
			calculateVotesByCandidate(newCandidateStates.getHopefulCandidates(), newBallotStates));

		return new ElectionStepResult<>(newBallotStates, newNumberOfElectedCandidates,
		                                              newCandidateStates);
	}

	private ElectionStepResult<CANDIDATE_TYPE> calculateElectionStepResultByStrikingTheWeakestCandidate(BigFraction quorum,
	                                                                                    ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates,
	                                                                                    int numberOfElectedCandidates,
	                                                                                    CandidateStates<CANDIDATE_TYPE> candidateStates) {

		electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
		State<CANDIDATE_TYPE> state = strikeWeakestCandidate(ballotStates, candidateStates);
		return new ElectionStepResult<>(state.ballotStates, numberOfElectedCandidates,
		                                              state.candidateStates);
	}

	private State<CANDIDATE_TYPE> strikeWeakestCandidate(
		ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates,
		CandidateStates<CANDIDATE_TYPE> candidateStates) {

		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateBeforeStriking = calculateVotesByCandidate(
			candidateStates.getHopefulCandidates(), ballotStates);

		CANDIDATE_TYPE weakestCandidate = calculateWeakestCandidate(votesByCandidateBeforeStriking);

		// TODO: Mehrdeutigkeiten bei Schwächsten Kandidaten extern auswählen lassen
		candidateStates = candidateStates.withLooser(weakestCandidate);
		ballotStates = createBallotStatesPointingAtNextHopefulCandidate(ballotStates, candidateStates);

		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateAfterStriking =
			calculateVotesByCandidate(candidateStates.getHopefulCandidates(), ballotStates);

		electionCalculationListener.candidateDropped(
			votesByCandidateBeforeStriking,
			weakestCandidate,
			votesByCandidateBeforeStriking.get(weakestCandidate),
			votesByCandidateAfterStriking);
		return new State<>(candidateStates, ballotStates);
	}


	private CANDIDATE_TYPE calculateWeakestCandidate(Map<CANDIDATE_TYPE, BigFraction> votesByCandidate) {
		BigFraction numberOfVotesOfBestCandidate = new BigFraction(Integer.MAX_VALUE, 1);
		//TODO: Hier sollten eigentlich 0-Kandidierende noch aufgeführt werden, solange sie nicht bereits gedroppd sind.
		Collection<CANDIDATE_TYPE> weakestCandidates = newArrayList();
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) < 0) {
				numberOfVotesOfBestCandidate = votesForCandidate.getValue();
				weakestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
			} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
				weakestCandidates.add(votesForCandidate.getKey());
			}
		}

		return chooseOneOutOfManyCandidates(copyOf(weakestCandidates));
	}

	private ImmutableCollection<BallotState<CANDIDATE_TYPE>> createBallotStatesPointingAtNextHopefulCandidate(
		ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates,
		CandidateStates<CANDIDATE_TYPE> candidateStates) {
		ImmutableList.Builder<BallotState<CANDIDATE_TYPE>> resultBuilder = ImmutableList.builder();
		for (BallotState<CANDIDATE_TYPE> ballotState : ballotStates) {
			resultBuilder.add(ballotState.withFirstHopefulCandidate(candidateStates));
		}

		return resultBuilder.build();
	}

	private CANDIDATE_TYPE chooseOneOutOfManyCandidates(ImmutableSet<CANDIDATE_TYPE> candidates) {
		CANDIDATE_TYPE winner = null;

		if (candidates.size() == 1) {
			return candidates.iterator().next();
		} else if (candidates.size() > 1) {
			electionCalculationListener.delegatingToExternalAmbiguityResolution(candidates);
			AmbiguityResolverResult<CANDIDATE_TYPE> ambiguityResolverResult = ambiguityResolver
				.chooseOneOfMany(candidates);
			electionCalculationListener.externalyResolvedAmbiguity(ambiguityResolverResult);
			winner = ambiguityResolverResult.chosenCandidate;
		}

		return winner;
	}

	public static class ElectionStepResult<CANDIDATE_TYPE extends Candidate> {
		public final CandidateStates<CANDIDATE_TYPE> newCandidateStates;
		public final ImmutableCollection<BallotState<CANDIDATE_TYPE>> newBallotStates;
		public final int newNumberOfElectedCandidates;

		public ElectionStepResult(ImmutableCollection<BallotState<CANDIDATE_TYPE>> newBallotStates, int newNumberOfElectedCandidates,
		                          CandidateStates<CANDIDATE_TYPE> candidateStates) {
			this.newCandidateStates = candidateStates;
			this.newBallotStates = newBallotStates;
			this.newNumberOfElectedCandidates = newNumberOfElectedCandidates;
		}
	}

	private class State<CANDIDATE_TYPE extends Candidate> {
		private final CandidateStates<CANDIDATE_TYPE> candidateStates;
		private final ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates;

		public State(CandidateStates<CANDIDATE_TYPE> candidateStates,
		             ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates) {
			this.candidateStates = candidateStates;
			this.ballotStates = ballotStates;
		}
	}
}
