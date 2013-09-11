package info.gehrels.voting.singleTransferableVote;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;

public final class VotesByCandidateCalculation {
	private VotesByCandidateCalculation() {
	}

	static <CANDIDATE extends Candidate> Map<CANDIDATE, BigFraction> calculateVotesByCandidate(ImmutableSet<CANDIDATE> candidates,
	                                                             ImmutableCollection<BallotState<CANDIDATE>> ballotStates) {
		Map<CANDIDATE, BigFraction> votesByCandidateDraft = getVotesByVotedCandidates(ballotStates);

		Builder<CANDIDATE, BigFraction> builder = ImmutableMap.builder();
		for (CANDIDATE candidate : candidates) {
			BigFraction votes = votesByCandidateDraft.get(candidate);
			builder.put(candidate, (votes == null) ? BigFraction.ZERO : votes);
		}

		return builder.build();
	}

	private static <CANDIDATE extends Candidate> Map<CANDIDATE, BigFraction> getVotesByVotedCandidates(
		ImmutableCollection<BallotState<CANDIDATE>> ballotStates) {
		Map<CANDIDATE, BigFraction> votesByCandidateDraft = new HashMap<>();
		for (BallotState<CANDIDATE> ballotState : ballotStates) {
			Optional<CANDIDATE> preferredHopefulCandidate = ballotState.getPreferredCandidate();
			if (!preferredHopefulCandidate.isPresent()) {
				continue;
			}

			CANDIDATE candidate = preferredHopefulCandidate.get();
			BigFraction votes = votesByCandidateDraft.get(candidate);
			if (votes == null) {
				votesByCandidateDraft.put(candidate, ballotState.getVoteWeight());
			} else {
				votesByCandidateDraft.put(candidate, votes.add(ballotState.getVoteWeight()));
			}
		}
		return votesByCandidateDraft;
	}
}
