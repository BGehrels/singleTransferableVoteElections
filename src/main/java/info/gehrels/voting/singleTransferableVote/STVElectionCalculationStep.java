package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class STVElectionCalculationStep<CANDIDATE_TYPE extends Candidate> {

	private final STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;
	private final AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver;

	public STVElectionCalculationStep(STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener,
	                                  AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver) {
		this.ambiguityResolver = validateThat(ambiguityResolver, is(not(nullValue())));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(not(nullValue())));
	}

	public final ElectionStepResult<CANDIDATE_TYPE> declareWinnerOrStrikeCandidate(BigFraction quorum,
	                                                         ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
	                                                         VoteWeightRecalculator<CANDIDATE_TYPE> redistributor,
	                                                         long numberOfElectedCandidates,
	                                                         CandidateStates<CANDIDATE_TYPE> candidateStates) {
		CANDIDATE_TYPE winner = bestCandidateThatReachedTheQuorum(quorum, voteStates, candidateStates);
		if (winner != null) {
			return calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(quorum,
			                                                                           voteStates,
			                                                                           redistributor,
			                                                                           numberOfElectedCandidates,
			                                                                           winner,
			                                                                           candidateStates);
		} else {
			return calculateElectionStepResultByStrikingTheWeakestCandidate(quorum, voteStates,
			                                                                numberOfElectedCandidates,
			                                                                candidateStates);
		}
	}

	private CANDIDATE_TYPE bestCandidateThatReachedTheQuorum(BigFraction quorum,
	                                                    ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
	                                                    CandidateStates<CANDIDATE_TYPE> candidateStates) {
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidate =
			VotesByCandidateCalculation.calculateVotesByCandidate(candidateStates.getHopefulCandidates(), voteStates);
		BigFraction numberOfVotesOfBestCandidate = BigFraction.MINUS_ONE;
		Collection<CANDIDATE_TYPE> bestCandidates = newArrayList();
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(quorum) >= 0) {
				if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) > 0) {
					numberOfVotesOfBestCandidate = votesForCandidate.getValue();
					bestCandidates = new ArrayList<>(singletonList(votesForCandidate.getKey()));
				} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
					bestCandidates.add(votesForCandidate.getKey());
				}
			}
		}


		// TODO: Ist ambiguity resolution hier überhaupt nötig? Was sagt eigentlich die Satzung zur Streichungsreihenfolge?
		return chooseOneOutOfManyCandidates(copyOf(bestCandidates));
	}


	private ElectionStepResult<CANDIDATE_TYPE> calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(BigFraction quorum,
	                                                                                               ImmutableCollection<VoteState<CANDIDATE_TYPE>> originalVoteStates,
	                                                                                               VoteWeightRecalculator<CANDIDATE_TYPE> redistributor,
	                                                                                               long numberOfElectedCandidates,
	                                                                                               CANDIDATE_TYPE winner,
	                                                                                               CandidateStates<CANDIDATE_TYPE> candidateStates) {
		electionCalculationListener
			.candidateIsElected(winner, VotesForCandidateCalculation.calculateVotesForCandidate(winner, originalVoteStates), quorum);

		long newNumberOfElectedCandidates = numberOfElectedCandidates + 1;
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStatesAfterRedistribution = redistributor
			.recalculateExceededVoteWeight(winner, quorum, originalVoteStates, candidateStates);

		CandidateStates<CANDIDATE_TYPE> newCandidateStates = candidateStates.withElected(winner);
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates = createVoteStatesPointingAtNextHopefulCandidate(
			voteStatesAfterRedistribution, newCandidateStates);

		electionCalculationListener.voteWeightRedistributionCompleted(originalVoteStates, newVoteStates,
			VotesByCandidateCalculation
				.calculateVotesByCandidate(newCandidateStates.getHopefulCandidates(), newVoteStates));

		return new ElectionStepResult<>(newVoteStates, newNumberOfElectedCandidates,
		                                              newCandidateStates);
	}

	private ElectionStepResult<CANDIDATE_TYPE> calculateElectionStepResultByStrikingTheWeakestCandidate(BigFraction quorum,
	                                                                                    ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
	                                                                                    long numberOfElectedCandidates,
	                                                                                    CandidateStates<CANDIDATE_TYPE> candidateStates) {

		electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
		State<CANDIDATE_TYPE> state = strikeWeakestCandidate(voteStates, candidateStates);
		return new ElectionStepResult<>(state.voteStates, numberOfElectedCandidates,
		                                              state.candidateStates);
	}

	private State<CANDIDATE_TYPE> strikeWeakestCandidate(
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
		CandidateStates<CANDIDATE_TYPE> oldcandidateStates) {

		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateBeforeStriking = VotesByCandidateCalculation
			.calculateVotesByCandidate(
				oldcandidateStates.getHopefulCandidates(), voteStates);

		CANDIDATE_TYPE weakestCandidate = calculateWeakestCandidate(votesByCandidateBeforeStriking);

		CandidateStates<CANDIDATE_TYPE> newCandidateStates = oldcandidateStates.withLooser(weakestCandidate);
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates = createVoteStatesPointingAtNextHopefulCandidate(
			voteStates, newCandidateStates);

		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateAfterStriking =
			VotesByCandidateCalculation.calculateVotesByCandidate(newCandidateStates.getHopefulCandidates(), newVoteStates);

		electionCalculationListener.candidateDropped(
			votesByCandidateBeforeStriking,
			weakestCandidate,
			votesByCandidateBeforeStriking.get(weakestCandidate));
		electionCalculationListener.voteWeightRedistributionCompleted(voteStates, newVoteStates, votesByCandidateAfterStriking);
		return new State<>(newCandidateStates, newVoteStates);
	}


	private CANDIDATE_TYPE calculateWeakestCandidate(Map<CANDIDATE_TYPE, BigFraction> votesByCandidate) {
		BigFraction numberOfVotesOfBestCandidate = new BigFraction(Integer.MAX_VALUE, 1);
		//TODO: Hier sollten eigentlich 0-Kandidierende noch aufgeführt werden, solange sie nicht bereits gedroppd sind.
		Collection<CANDIDATE_TYPE> weakestCandidates = newArrayList();
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) < 0) {
				numberOfVotesOfBestCandidate = votesForCandidate.getValue();
				weakestCandidates = new ArrayList<>(singletonList(votesForCandidate.getKey()));
			} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
				weakestCandidates.add(votesForCandidate.getKey());
			}
		}

		return chooseOneOutOfManyCandidates(copyOf(weakestCandidates));
	}

	private ImmutableCollection<VoteState<CANDIDATE_TYPE>> createVoteStatesPointingAtNextHopefulCandidate(
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
		CandidateStates<CANDIDATE_TYPE> candidateStates) {
		ImmutableList.Builder<VoteState<CANDIDATE_TYPE>> resultBuilder = ImmutableList.builder();
		for (VoteState<CANDIDATE_TYPE> oldVoteState : voteStates) {
			resultBuilder.add(oldVoteState.withFirstHopefulCandidate(candidateStates));
		}

		return resultBuilder.build();
	}

	private CANDIDATE_TYPE chooseOneOutOfManyCandidates(ImmutableSet<CANDIDATE_TYPE> candidates) {
		if (candidates.size() == 1) {
			return candidates.iterator().next();
		}

		CANDIDATE_TYPE choosenCandidate = null;
		if (candidates.size() > 1) {
			electionCalculationListener.delegatingToExternalAmbiguityResolution(candidates);
			AmbiguityResolverResult<CANDIDATE_TYPE> ambiguityResolverResult = ambiguityResolver
				.chooseOneOfMany(candidates);
			electionCalculationListener.externalyResolvedAmbiguity(ambiguityResolverResult);
			choosenCandidate = ambiguityResolverResult.chosenCandidate;
		}

		return choosenCandidate;
	}

	private class State<CANDIDATE_TYPE extends Candidate> {
		private final CandidateStates<CANDIDATE_TYPE> candidateStates;
		private final ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates;

		private State(CandidateStates<CANDIDATE_TYPE> candidateStates,
		              ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates) {
			this.candidateStates = candidateStates;
			this.voteStates = voteStates;
		}
	}

	public static class ElectionStepResult<CANDIDATE_TYPE extends Candidate> {
		final CandidateStates<CANDIDATE_TYPE> newCandidateStates;
		final ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates;
		final long newNumberOfElectedCandidates;

		ElectionStepResult(ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates,
		                          long newNumberOfElectedCandidates,
		                          CandidateStates<CANDIDATE_TYPE> candidateStates) {
			this.newCandidateStates = candidateStates;
			this.newVoteStates = newVoteStates;
			this.newNumberOfElectedCandidates = newNumberOfElectedCandidates;
		}
	}
}
