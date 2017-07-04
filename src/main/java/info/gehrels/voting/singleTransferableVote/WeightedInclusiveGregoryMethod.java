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
		return new WigmVoteWeightRecalculator<>(electionCalculationListener);
	}

	private static final class WigmVoteWeightRecalculator<CANDIDATE extends Candidate>
		implements VoteWeightRecalculator<CANDIDATE> {
		private final STVElectionCalculationListener<CANDIDATE> electionCalculationListener;

		private WigmVoteWeightRecalculator(STVElectionCalculationListener<CANDIDATE> electionCalculationListener) {
			this.electionCalculationListener = electionCalculationListener;
		}

		@Override
		public ImmutableList<VoteState<CANDIDATE>> recalculateExceededVoteWeight(CANDIDATE winner,
																				 BigFraction quorum,
																				 ImmutableCollection<VoteState<CANDIDATE>> originalVoteStates,
																				 CandidateStates<CANDIDATE> candidateStates) {
			Builder<VoteState<CANDIDATE>> resultBuilder = ImmutableList.builder();
			VoteDistribution<CANDIDATE> voteDistribution = new VoteDistribution<>(
				candidateStates.getHopefulCandidates(), originalVoteStates);

			BigFraction votesForCandidate = voteDistribution.votesByCandidate.get(winner);
			BigFraction excessiveVotes = votesForCandidate.subtract(quorum);
			BigFraction excessiveFractionOfVoteWeight = excessiveVotes.divide(votesForCandidate);

			electionCalculationListener
				.redistributingExcessiveFractionOfVoteWeight(winner, excessiveFractionOfVoteWeight);

			for (VoteState<CANDIDATE> voteState : originalVoteStates) {
				if (voteState.getPreferredCandidate().orElse(null) == winner) {
					BigFraction newVoteWeight = voteState.getVoteWeight().multiply(excessiveFractionOfVoteWeight);
					VoteState<CANDIDATE> newVoteState = voteState.withVoteWeight(newVoteWeight);
					resultBuilder.add(newVoteState);
				} else {
					resultBuilder.add(voteState);
				}
			}

			return resultBuilder.build();

		}

	}
}
