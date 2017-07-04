/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.ImmutableSortedSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class STVElectionCalculationStep<CANDIDATE extends Candidate> {


	private final STVElectionCalculationListener<CANDIDATE> electionCalculationListener;
	private final AmbiguityResolver<CANDIDATE> ambiguityResolver;

	public STVElectionCalculationStep(STVElectionCalculationListener<CANDIDATE> electionCalculationListener,
	                                  AmbiguityResolver<CANDIDATE> ambiguityResolver) {
		this.ambiguityResolver = validateThat(ambiguityResolver, is(not(nullValue())));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(not(nullValue())));
	}

	public final ElectionStepResult<CANDIDATE> declareWinnerOrStrikeCandidate(BigFraction quorum,
																			  ImmutableCollection<VoteState<CANDIDATE>> voteStates,
																			  VoteWeightRecalculator<CANDIDATE> redistributor,
																			  long numberOfElectedCandidates,
																			  CandidateStates<CANDIDATE> candidateStates) {
		ImmutableSet<CANDIDATE> winningCandidates = allCandidatesThatReachedTheQuorum(quorum, voteStates, candidateStates);
		if (winningCandidates.isEmpty()) {
			return calculateElectionStepResultByStrikingTheWeakestCandidate(quorum,
					                                                        voteStates,
			                                                                numberOfElectedCandidates,
			                                                                candidateStates);
		} else {
			return calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(quorum,
			                                                                           voteStates,
			                                                                           redistributor,
			                                                                           numberOfElectedCandidates,
			                                                                           winningCandidates,
			                                                                           candidateStates);
		}
	}

	private ImmutableSet<CANDIDATE> allCandidatesThatReachedTheQuorum(BigFraction quorum,
																	  ImmutableCollection<VoteState<CANDIDATE>> voteStates,
																	  CandidateStates<CANDIDATE> candidateStates) {
		VoteDistribution<CANDIDATE> voteDistribution = new VoteDistribution<>(candidateStates.getHopefulCandidates(),
				                                                              voteStates);

		// We use a sorted set here, to provide Unit tests a predictable execution order.
		Builder<CANDIDATE> candidatesThatReachedTheQuorum = ImmutableSortedSet.orderedBy(comparing(o -> o.name));

        voteDistribution.votesByCandidate.entrySet().stream()
                .filter(votesForCandidate -> quorumIsReached(quorum, votesForCandidate))
                .forEach(votesForCandidate -> candidatesThatReachedTheQuorum.add(votesForCandidate.getKey()));


		return candidatesThatReachedTheQuorum.build();
	}

    private boolean quorumIsReached(BigFraction quorum, Entry<CANDIDATE, BigFraction> votesForCandidate) {
        return votesForCandidate.getValue().compareTo(quorum) >= 0;
    }


    private ElectionStepResult<CANDIDATE> calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(
														BigFraction quorum,
														ImmutableCollection<VoteState<CANDIDATE>> originalVoteStates,
														VoteWeightRecalculator<CANDIDATE> redistributor,
														long numberOfElectedCandidates,
														ImmutableSet<CANDIDATE> winners,
														CandidateStates<CANDIDATE> originalCandidateStates) {

		VoteDistribution<CANDIDATE> originalVoteDistribution = new VoteDistribution<>(originalCandidateStates.getHopefulCandidates(),
				                                                                      originalVoteStates);

		CandidateStates<CANDIDATE> newCandidateStates = originalCandidateStates;
		ImmutableCollection<VoteState<CANDIDATE>> voteStatesAfterRedistribution = originalVoteStates;
		long newNumberOfElectedCandidates = numberOfElectedCandidates;
		for (CANDIDATE winner : winners) {
			electionCalculationListener.candidateIsElected(winner,
                                                           originalVoteDistribution.votesByCandidate.get(winner),
					                                       quorum);
			newCandidateStates = newCandidateStates.withElected(winner);
			voteStatesAfterRedistribution = redistributor.recalculateExceededVoteWeight(winner,
					                                                                    quorum,
			                                                                            voteStatesAfterRedistribution,
			                                                                            originalCandidateStates);
			newNumberOfElectedCandidates++;
		}


		ImmutableCollection<VoteState<CANDIDATE>> newVoteStates =
				createVoteStatesPointingAtNextHopefulCandidate(voteStatesAfterRedistribution, newCandidateStates);

		electionCalculationListener.voteWeightRedistributionCompleted(
				originalVoteStates,
				newVoteStates,
		        new VoteDistribution<>(
			    	newCandidateStates.getHopefulCandidates(),
					newVoteStates
				)
		);

		return new ElectionStepResult<>(newVoteStates, newNumberOfElectedCandidates, newCandidateStates);
	}

	private ElectionStepResult<CANDIDATE> calculateElectionStepResultByStrikingTheWeakestCandidate(
																BigFraction quorum,
																ImmutableCollection<VoteState<CANDIDATE>> voteStates,
																long numberOfElectedCandidates,
																CandidateStates<CANDIDATE> candidateStates) {

		electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
		State<CANDIDATE> state = strikeWeakestCandidate(voteStates, candidateStates);
		return new ElectionStepResult<>(state.voteStates, numberOfElectedCandidates, state.candidateStates);
	}

	private State<CANDIDATE> strikeWeakestCandidate(
		ImmutableCollection<VoteState<CANDIDATE>> voteStates,
		CandidateStates<CANDIDATE> oldCandidateStates) {

		VoteDistribution<CANDIDATE> voteDistributionBeforeStriking = new VoteDistribution<>(
			oldCandidateStates.getHopefulCandidates(), voteStates);

		CANDIDATE weakestCandidate = calculateWeakestCandidate(voteDistributionBeforeStriking);

		CandidateStates<CANDIDATE> newCandidateStates = oldCandidateStates.withLoser(weakestCandidate);
		ImmutableCollection<VoteState<CANDIDATE>> newVoteStates = createVoteStatesPointingAtNextHopefulCandidate(
			voteStates, newCandidateStates);

		VoteDistribution<CANDIDATE> voteDistributionAfterStriking = new VoteDistribution<>(
			newCandidateStates.getHopefulCandidates(), newVoteStates);

		electionCalculationListener.candidateDropped(voteDistributionBeforeStriking, weakestCandidate);
		electionCalculationListener.voteWeightRedistributionCompleted(voteStates, newVoteStates, voteDistributionAfterStriking);
		return new State<>(newCandidateStates, newVoteStates);
	}


	private CANDIDATE calculateWeakestCandidate(VoteDistribution<CANDIDATE> voteDistribution) {
		BigFraction numberOfVotesOfBestCandidate = new BigFraction(Integer.MAX_VALUE, 1);
		Collection<CANDIDATE> weakestCandidates = newArrayList();

		for (Entry<CANDIDATE, BigFraction> votesForCandidate : voteDistribution.votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) < 0) {
				numberOfVotesOfBestCandidate = votesForCandidate.getValue();
				weakestCandidates = new ArrayList<>(singletonList(votesForCandidate.getKey()));
			} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
				weakestCandidates.add(votesForCandidate.getKey());
			}
		}

		return chooseOneOutOfManyCandidates(copyOf(weakestCandidates));
	}

	private ImmutableCollection<VoteState<CANDIDATE>> createVoteStatesPointingAtNextHopefulCandidate(
		ImmutableCollection<VoteState<CANDIDATE>> voteStates,
		CandidateStates<CANDIDATE> candidateStates) {
		ImmutableList.Builder<VoteState<CANDIDATE>> resultBuilder = ImmutableList.builder();
		for (VoteState<CANDIDATE> oldVoteState : voteStates) {
			resultBuilder.add(oldVoteState.withFirstHopefulCandidate(candidateStates));
		}

		return resultBuilder.build();
	}

	private CANDIDATE chooseOneOutOfManyCandidates(ImmutableSet<CANDIDATE> candidates) {
		if (candidates.size() == 1) {
			return candidates.iterator().next();
		}

		CANDIDATE chosenCandidate = null;
		if (candidates.size() > 1) {
			electionCalculationListener.delegatingToExternalAmbiguityResolution(candidates);
			AmbiguityResolverResult<CANDIDATE> ambiguityResolverResult = ambiguityResolver.chooseOneOfMany(candidates);
			electionCalculationListener.externallyResolvedAmbiguity(ambiguityResolverResult);
			chosenCandidate = ambiguityResolverResult.chosenCandidate;
		}

		return chosenCandidate;
	}

	private static class State<C extends Candidate> {
		private final CandidateStates<C> candidateStates;
		private final ImmutableCollection<VoteState<C>> voteStates;

		private State(CandidateStates<C> candidateStates, ImmutableCollection<VoteState<C>> voteStates) {
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
