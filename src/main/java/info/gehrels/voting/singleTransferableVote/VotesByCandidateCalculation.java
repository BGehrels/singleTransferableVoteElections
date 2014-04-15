/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
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
	                                                             ImmutableCollection<VoteState<CANDIDATE>> voteStates) {
		Map<CANDIDATE, BigFraction> votesByCandidateDraft = getVotesByVotedCandidates(voteStates);

		Builder<CANDIDATE, BigFraction> builder = ImmutableMap.builder();
		for (CANDIDATE candidate : candidates) {
			BigFraction votes = votesByCandidateDraft.get(candidate);
			builder.put(candidate, (votes == null) ? BigFraction.ZERO : votes);
		}

		return builder.build();
	}

	private static <CANDIDATE extends Candidate> Map<CANDIDATE, BigFraction> getVotesByVotedCandidates(
		ImmutableCollection<VoteState<CANDIDATE>> voteStates) {
		Map<CANDIDATE, BigFraction> votesByCandidateDraft = new HashMap<>();
		for (VoteState<CANDIDATE> voteState : voteStates) {
			Optional<CANDIDATE> preferredHopefulCandidate = voteState.getPreferredCandidate();
			if (!preferredHopefulCandidate.isPresent()) {
				continue;
			}

			CANDIDATE candidate = preferredHopefulCandidate.get();
			BigFraction votes = votesByCandidateDraft.get(candidate);
			if (votes == null) {
				votesByCandidateDraft.put(candidate, voteState.getVoteWeight());
			} else {
				votesByCandidateDraft.put(candidate, votes.add(voteState.getVoteWeight()));
			}
		}
		return votesByCandidateDraft;
	}
}
