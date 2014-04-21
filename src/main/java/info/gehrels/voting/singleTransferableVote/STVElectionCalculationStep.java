/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
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
		ImmutableSet<CANDIDATE_TYPE> winningCandidates = allCandidatesThatReachedTheQuorum(quorum, voteStates,
		                                                                                   candidateStates);
		if (winningCandidates.isEmpty()) {
			return calculateElectionStepResultByStrikingTheWeakestCandidate(quorum, voteStates,
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

	private ImmutableSet<CANDIDATE_TYPE> allCandidatesThatReachedTheQuorum(BigFraction quorum,
	                                                                       ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
	                                                                       CandidateStates<CANDIDATE_TYPE> candidateStates) {
		VoteDistribution<CANDIDATE_TYPE> voteDistribution = new VoteDistribution<>(
			candidateStates.getHopefulCandidates(), voteStates);
		Builder<CANDIDATE_TYPE> candidatesThatReachedTheQuorum = ImmutableSet.builder();
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : voteDistribution.votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(quorum) >= 0) {
				candidatesThatReachedTheQuorum.add(votesForCandidate.getKey());
			}
		}


		return candidatesThatReachedTheQuorum.build();
	}


	private ElectionStepResult<CANDIDATE_TYPE> calculateElectionStepResultByRedistributingTheWinnersExceedingVotes(
		BigFraction quorum,
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> originalVoteStates,
		VoteWeightRecalculator<CANDIDATE_TYPE> redistributor,
		long numberOfElectedCandidates,
		ImmutableSet<CANDIDATE_TYPE> winners,
		CandidateStates<CANDIDATE_TYPE> originalCandidateStates) {

		VoteDistribution<CANDIDATE_TYPE> originalVoteDistribution = new VoteDistribution<>(
			originalCandidateStates.getHopefulCandidates(), originalVoteStates);

		CandidateStates<CANDIDATE_TYPE> newCandidateStates = originalCandidateStates;
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStatesAfterRedistribution = originalVoteStates;
		long newNumberOfElectedCandidates = numberOfElectedCandidates;
		for (CANDIDATE_TYPE winner : winners) {
			electionCalculationListener
				.candidateIsElected(winner,
				                    originalVoteDistribution.votesByCandidate.get(winner),
				                    quorum);
			newCandidateStates = newCandidateStates.withElected(winner);
			voteStatesAfterRedistribution = redistributor.recalculateExceededVoteWeight(winner, quorum,
			                                                                            voteStatesAfterRedistribution,
			                                                                            originalCandidateStates);
			newNumberOfElectedCandidates++;
		}


		ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates = createVoteStatesPointingAtNextHopefulCandidate(
			voteStatesAfterRedistribution, newCandidateStates);

		electionCalculationListener.voteWeightRedistributionCompleted(originalVoteStates, newVoteStates,
		                                                              new VoteDistribution<>(
			                                                              newCandidateStates.getHopefulCandidates(),
			                                                              newVoteStates));

		return new ElectionStepResult<>(newVoteStates, newNumberOfElectedCandidates, newCandidateStates);
	}

	private ElectionStepResult<CANDIDATE_TYPE> calculateElectionStepResultByStrikingTheWeakestCandidate(
		BigFraction quorum,
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
		CandidateStates<CANDIDATE_TYPE> oldCandidateStates) {

		VoteDistribution<CANDIDATE_TYPE> voteDistributionBeforeStriking = new VoteDistribution<>(
			oldCandidateStates.getHopefulCandidates(), voteStates);

		CANDIDATE_TYPE weakestCandidate = calculateWeakestCandidate(voteDistributionBeforeStriking);

		CandidateStates<CANDIDATE_TYPE> newCandidateStates = oldCandidateStates.withLooser(weakestCandidate);
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates = createVoteStatesPointingAtNextHopefulCandidate(
			voteStates, newCandidateStates);

		VoteDistribution<CANDIDATE_TYPE> voteDistributionAfterStriking = new VoteDistribution<>(
			newCandidateStates.getHopefulCandidates(), newVoteStates);

		electionCalculationListener.candidateDropped(voteDistributionBeforeStriking, weakestCandidate);
		electionCalculationListener.voteWeightRedistributionCompleted(voteStates, newVoteStates,
		                                                              voteDistributionAfterStriking);
		return new State<>(newCandidateStates, newVoteStates);
	}


	private CANDIDATE_TYPE calculateWeakestCandidate(VoteDistribution<CANDIDATE_TYPE> voteDistribution) {
		BigFraction numberOfVotesOfBestCandidate = new BigFraction(Integer.MAX_VALUE, 1);
		Collection<CANDIDATE_TYPE> weakestCandidates = newArrayList();

		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : voteDistribution.votesByCandidate.entrySet()) {
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
