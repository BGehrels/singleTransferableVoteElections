package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import org.apache.commons.math3.fraction.BigFraction;

public interface VoteWeightRedistributionMethod<CANDIDATE_TYPE extends Candidate> {
	VoteWeightRedistributor<CANDIDATE_TYPE> redistributorFor();

	public interface VoteWeightRedistributor<CANDIDATE_TYPE extends Candidate> {

		ImmutableCollection<BallotState<CANDIDATE_TYPE>> redistributeExceededVoteWeight(CANDIDATE_TYPE winner, BigFraction quorum,
		                                                                ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates);
	}
}
