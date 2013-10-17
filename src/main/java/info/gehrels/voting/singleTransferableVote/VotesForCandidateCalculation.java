package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.Collection;

public final class VotesForCandidateCalculation {
	private VotesForCandidateCalculation() {
	}

	static <CANDIDATE_TYPE extends Candidate> BigFraction calculateVotesForCandidate(CANDIDATE_TYPE candidate,
	                                                                                 Collection<VoteState<CANDIDATE_TYPE>> voteStates) {
		BigFraction votes = BigFraction.ZERO;
		for (VoteState<CANDIDATE_TYPE> voteState : voteStates) {
			if (voteState.getPreferredCandidate().orNull() == candidate) {
				votes = votes.add(voteState.getVoteWeight());
			}
		}

		return votes;
	}
}
