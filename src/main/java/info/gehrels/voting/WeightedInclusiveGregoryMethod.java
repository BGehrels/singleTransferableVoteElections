package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import java.util.Collection;

public class WeightedInclusiveGregoryMethod implements VoteWeightRedistributionMethod {
	private final ElectionCalculationListener electionCalculationListener;

	public WeightedInclusiveGregoryMethod(ElectionCalculationListener electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public VoteWeightRedistributor redistributorFor() {
		return new VoteWeightRedistributor();
	}

	private class VoteWeightRedistributor implements VoteWeightRedistributionMethod.VoteWeightRedistributor {
		@Override
		public ImmutableList<BallotState> redistributeExceededVoteWeight(Candidate winner, double quorum,
		                                                                       ImmutableCollection<BallotState> ballotStates) {
			Builder<BallotState> resultBuilder = ImmutableList.builder();

			double votesForCandidate = calculateVotesForCandidate(winner, ballotStates);
			double excessiveVotes = votesForCandidate - quorum;
			double excessiveFractionOfVoteWeight = excessiveVotes / votesForCandidate;

			for (BallotState ballotState : ballotStates) {
				if (ballotState.getPreferredCandidate() == winner) {
					BallotState newBallotState = ballotState.withReducedVoteWeight(excessiveFractionOfVoteWeight);
					electionCalculationListener.voteWeightRedistributed(excessiveFractionOfVoteWeight,
					                                                    newBallotState.ballot.id,
					                                                    newBallotState.getVoteWeight());
					resultBuilder.add(newBallotState);
				} else {
					resultBuilder.add(ballotState);
				}
			}

			return resultBuilder.build();

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
	}
}
