package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

public interface VoteWeightRecalculator<CANDIDATE_TYPE extends Candidate> {

	ImmutableCollection<VoteState<CANDIDATE_TYPE>> recalculateExceededVoteWeight(CANDIDATE_TYPE winner,
	                                                                             BigFraction quorum,
	                                                                             ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
	                                                                             CandidateStates<CANDIDATE_TYPE> candidateStates);
}
