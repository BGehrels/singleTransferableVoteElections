/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public final class VoteTest {
	private static final Candidate CANDIDATE_A = new Candidate("A");
	private static final Candidate CANDIDATE_B = new Candidate("B");
	private static final Election<Candidate> ELECTION = new Election<>("Example Office", ImmutableSet.of(CANDIDATE_A,
	                                                                                                    CANDIDATE_B));

	@Test
	public void testInvalidVoteCreation() {
		Vote<Candidate> invalidVote = Vote.createInvalidVote(ELECTION);
		assertThat(invalidVote.getRankedCandidates(), is(empty()));
		assertThat(invalidVote.getElection(), is(ELECTION));
		assertThat(invalidVote.isNo(), is(false));
		assertThat(invalidVote.isValid(), is(false));
	}

	@Test
	public void testNoVoteCreation() {
		Vote<Candidate> invalidVote = Vote.createNoVote(ELECTION);
		assertThat(invalidVote.getRankedCandidates(), is(empty()));
		assertThat(invalidVote.getElection(), is(ELECTION));
		assertThat(invalidVote.isNo(), is(true));
		assertThat(invalidVote.isValid(), is(true));
	}

	@Test
	public void testPreferenceVoteCreation() {
		Vote<Candidate> invalidVote = Vote.createPreferenceVote(ELECTION, ImmutableSet.of(CANDIDATE_B, CANDIDATE_A));

		assertThat(invalidVote.getRankedCandidates(), contains(CANDIDATE_B, CANDIDATE_A));
		assertThat(invalidVote.getElection(), is(ELECTION));
		assertThat(invalidVote.isNo(), is(false));
		assertThat(invalidVote.isValid(), is(true));
	}
}

