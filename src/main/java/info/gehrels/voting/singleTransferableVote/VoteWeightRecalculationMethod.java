package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;

public interface VoteWeightRecalculationMethod<CANDIDATE_TYPE extends Candidate> {
	VoteWeightRecalculator<CANDIDATE_TYPE> recalculatorFor();

}
