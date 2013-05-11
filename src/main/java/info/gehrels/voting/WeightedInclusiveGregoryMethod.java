package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.voting.VotesForCandidateCalculation.calculateVotesForCandidate;

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
		public ImmutableList<BallotState> redistributeExceededVoteWeight(Candidate winner, BigFraction quorum,
		                                                                       ImmutableCollection<BallotState> ballotStates) {
			Builder<BallotState> resultBuilder = ImmutableList.builder();

			BigFraction votesForCandidate = calculateVotesForCandidate(winner, ballotStates);
			BigFraction excessiveVotes = votesForCandidate.subtract(quorum);
			BigFraction excessiveFractionOfVoteWeight = excessiveVotes.divide(votesForCandidate);

			for (BallotState ballotState : ballotStates) {
				if (ballotState.getPreferredCandidate() == winner) {
					BigFraction newVoteWeight = ballotState.getVoteWeight().multiply(excessiveFractionOfVoteWeight);
					BallotState newBallotState = ballotState.withVoteWeight(newVoteWeight);
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

	}
}
