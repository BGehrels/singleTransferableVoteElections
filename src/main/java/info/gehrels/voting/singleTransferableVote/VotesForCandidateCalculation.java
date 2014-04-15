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
