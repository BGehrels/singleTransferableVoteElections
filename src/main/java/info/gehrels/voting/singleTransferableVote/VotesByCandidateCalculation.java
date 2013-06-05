package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;

public final class VotesByCandidateCalculation {
	static <CANDIDATE_TYPE extends Candidate> Map<CANDIDATE_TYPE, BigFraction> calculateVotesByCandidate(ImmutableSet<CANDIDATE_TYPE> candidates,
	                                                             ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates) {
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateDraft = new HashMap<>();
		for (BallotState<CANDIDATE_TYPE> ballotState : ballotStates) {
			CANDIDATE_TYPE preferredHopefulCandidate = ballotState.getPreferredCandidate();
			if (preferredHopefulCandidate == null) {
				continue;
			}

			BigFraction votes = votesByCandidateDraft.get(preferredHopefulCandidate);
			if (votes == null) {
				votesByCandidateDraft.put(preferredHopefulCandidate, ballotState.getVoteWeight());
			} else {
				votesByCandidateDraft.put(preferredHopefulCandidate, votes.add(ballotState.getVoteWeight()));
			}
		}

		Builder<CANDIDATE_TYPE, BigFraction> builder = ImmutableMap.builder();
		for (CANDIDATE_TYPE candidate : candidates) {
			BigFraction votes = votesByCandidateDraft.get(candidate);
			builder.put(candidate, votes == null ? BigFraction.ZERO : votes);
		}

		return builder.build();
	}
}
