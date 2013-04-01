package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

public interface VoteWeightRedistributionMethod {
	VoteWeightRedistributor redistributorFor();

	public interface VoteWeightRedistributor {

		ImmutableCollection<BallotState> redistributeExceededVoteWeight(Candidate winner, BigFraction quorum,
		                                                                ImmutableCollection<BallotState> ballotStates);
	}
}
