package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;

public final class VotesByCandidateCalculation {
	static <CANDIDATE extends info.gehrels.voting.Candidate> Map<CANDIDATE, BigFraction> calculateVotesByCandidate(ImmutableSet<CANDIDATE> candidates,
	                                                             ImmutableCollection<BallotState<CANDIDATE>> ballotStates) {
		Map<CANDIDATE, BigFraction> votesByCandidateDraft = new HashMap<>();
		for (BallotState<CANDIDATE> ballotState : ballotStates) {
			CANDIDATE preferredHopefulCandidate = ballotState.getPreferredCandidate();
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

		Builder<CANDIDATE, BigFraction> builder = ImmutableMap.builder();
		for (CANDIDATE candidate : candidates) {
			BigFraction votes = votesByCandidateDraft.get(candidate);
			builder.put(candidate, votes == null ? BigFraction.ZERO : votes);
		}

		return builder.build();
	}
}
