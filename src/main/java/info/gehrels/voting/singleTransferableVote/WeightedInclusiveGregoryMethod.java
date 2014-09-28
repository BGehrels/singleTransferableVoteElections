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
import com.google.common.collect.ImmutableList.Builder;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

public class WeightedInclusiveGregoryMethod<CANDIDATE_TYPE extends Candidate> implements
	VoteWeightRecalculationMethod<CANDIDATE_TYPE> {
	private final STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;

	public WeightedInclusiveGregoryMethod(STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public final VoteWeightRecalculator<CANDIDATE_TYPE> recalculatorFor() {
		return new WigmVoteWeightRecalculator<CANDIDATE_TYPE>(electionCalculationListener);
	}

	private final class WigmVoteWeightRecalculator<CANDIDATE_TYPE extends Candidate>
		implements VoteWeightRecalculator<CANDIDATE_TYPE> {
		private final STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;

		private WigmVoteWeightRecalculator(STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener) {
			this.electionCalculationListener = electionCalculationListener;
		}

		@Override
		public ImmutableList<VoteState<CANDIDATE_TYPE>> recalculateExceededVoteWeight(CANDIDATE_TYPE winner,
		                                                                              BigFraction quorum,
		                                                                              ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
		                                                                              CandidateStates<CANDIDATE_TYPE> candidateStates) {
			Builder<VoteState<CANDIDATE_TYPE>> resultBuilder = ImmutableList.builder();
			VoteDistribution<CANDIDATE_TYPE> voteDistribution = new VoteDistribution<>(
				candidateStates.getHopefulCandidates(), voteStates);

			BigFraction votesForCandidate = voteDistribution.votesByCandidate.get(winner);
			BigFraction excessiveVotes = votesForCandidate.subtract(quorum);
			BigFraction excessiveFractionOfVoteWeight = excessiveVotes.divide(votesForCandidate);

			electionCalculationListener
				.redistributingExcessiveFractionOfVoteWeight(winner, excessiveFractionOfVoteWeight);

			for (VoteState<CANDIDATE_TYPE> voteState : voteStates) {
				if (voteState.getPreferredCandidate().orNull() == winner) {
					BigFraction newVoteWeight = voteState.getVoteWeight().multiply(excessiveFractionOfVoteWeight);
					VoteState<CANDIDATE_TYPE> newVoteState = voteState.withVoteWeight(newVoteWeight);
					resultBuilder.add(newVoteState);
				} else {
					resultBuilder.add(voteState);
				}
			}

			return resultBuilder.build();

		}

	}
}
