package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

public interface VoteWeightRedistributor<CANDIDATE_TYPE extends Candidate> {

	ImmutableCollection<BallotState<CANDIDATE_TYPE>> redistributeExceededVoteWeight(CANDIDATE_TYPE winner,
	                                                                                BigFraction quorum,
	                                                                                ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates);
}
