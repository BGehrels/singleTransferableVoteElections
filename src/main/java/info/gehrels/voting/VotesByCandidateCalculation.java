package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;

public class VotesByCandidateCalculation {
	static Map<Candidate, BigFraction> calculateVotesByCandidate(ImmutableSet<Candidate> candidates,
	                                                             ImmutableCollection<BallotState> ballotStates) {
		Map<Candidate, BigFraction> votesByCandidateDraft = new HashMap<>();
		for (BallotState ballotState : ballotStates) {
			Candidate preferredHopefulCandidate = ballotState.getPreferredCandidate();
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

		Builder<Candidate, BigFraction> builder = ImmutableMap.builder();
		for (Candidate candidate : candidates) {
			BigFraction votes = votesByCandidateDraft.get(candidate);
			builder.put(candidate, votes == null ? BigFraction.ZERO : votes);
		}

		return builder.build();
	}
}
