package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

public interface VoteWeightRedistributionMethod {
	VoteWeightRedistributor redistributorFor();

	public interface VoteWeightRedistributor {

		ImmutableCollection<BallotState> redistributeExceededVoteWeight(Candidate winner, double quorum,
		                                                                ImmutableCollection<BallotState> ballotStates);
	}
}
