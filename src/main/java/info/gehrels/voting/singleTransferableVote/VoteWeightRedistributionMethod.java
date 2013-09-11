package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;

public interface VoteWeightRedistributionMethod<CANDIDATE_TYPE extends Candidate> {
	VoteWeightRedistributor<CANDIDATE_TYPE> redistributorFor();

}
