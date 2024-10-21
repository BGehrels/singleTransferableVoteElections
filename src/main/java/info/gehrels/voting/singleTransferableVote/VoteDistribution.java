package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.HashMap;
import java.util.Map;

public final class VoteDistribution<CANDIDATE extends Candidate> {
	public final ImmutableMap<CANDIDATE, BigFraction> votesByCandidate;
	public final BigFraction noVotes;
	public final BigFraction invalidVotes;

	public VoteDistribution(ImmutableSet<CANDIDATE> candidates, ImmutableCollection<VoteState<CANDIDATE>> voteStates) {
		Map<CANDIDATE, BigFraction> votesByCandidateDraft = createZeroVotesMap(candidates);
		BigFraction noVotesDraft = BigFraction.ZERO;
		BigFraction invalidVotesDraft = BigFraction.ZERO;

		for (VoteState<CANDIDATE> voteState : voteStates) {
			if (voteState.isInvalid()) {
				invalidVotesDraft = invalidVotesDraft.add(voteState.getVoteWeight());
			} else if (voteState.isNoVote()) {
				noVotesDraft = noVotesDraft.add(voteState.getVoteWeight());
			} else {
				CANDIDATE candidate = voteState.getPreferredCandidate().get();
                votesByCandidateDraft.compute(candidate, (k, votes) -> votes.add(voteState.getVoteWeight()));
			}
		}

		this.votesByCandidate = ImmutableMap.copyOf(votesByCandidateDraft);
		this.noVotes = noVotesDraft;
		this.invalidVotes = invalidVotesDraft;
	}

	private Map<CANDIDATE, BigFraction> createZeroVotesMap(ImmutableSet<CANDIDATE> candidates) {
		Map<CANDIDATE, BigFraction> zeroVotesMap = new HashMap<>();
		for (CANDIDATE candidate : candidates) {
			zeroVotesMap.put(candidate, BigFraction.ZERO);
		}
		return zeroVotesMap;
	}

	@Override
	public String toString() {
		return votesByCandidate + ", No: " + noVotes + ", Invalid: " + invalidVotes;
	}
}
