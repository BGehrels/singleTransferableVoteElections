package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.Collection;

public final class VotesForCandidateCalculation {
	static <CANDIDATE_TYPE extends Candidate> BigFraction calculateVotesForCandidate(CANDIDATE_TYPE candidate, Collection<BallotState<CANDIDATE_TYPE>> ballotStates) {
		BigFraction votes = BigFraction.ZERO;
		for (BallotState<CANDIDATE_TYPE> ballotState : ballotStates) {
			if (ballotState.getPreferredCandidate() == candidate) {
				votes= votes.add(ballotState.getVoteWeight());
			}
		}

		return votes;
	}
}
