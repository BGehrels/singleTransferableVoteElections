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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.genderedElections.GenderedCandidate;
import info.gehrels.voting.genderedElections.GenderedElection;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public final class VoteTest {
	private static final GenderedCandidate CANDIDATE_A = new GenderedCandidate("A", true);
	private static final GenderedCandidate CANDIDATE_B = new GenderedCandidate("B", true);
	private static final GenderedElection ELECTION = new GenderedElection(
															"Example Office",
                                                            1,
                                                            1,
															ImmutableSet.of(CANDIDATE_A, CANDIDATE_B));

	@Test
	public void testInvalidVoteCreation() {
		Vote<GenderedCandidate> invalidVote = Vote.createInvalidVote(ELECTION);
		assertThat(invalidVote.getRankedCandidates(), is(empty()));
		assertThat(invalidVote.getElection(), is(ELECTION));
		assertThat(invalidVote.isNo(), is(false));
		assertThat(invalidVote.isValid(), is(false));
	}

	@Test
	public void testNoVoteCreation() {
		Vote<GenderedCandidate> invalidVote = Vote.createNoVote(ELECTION);
		assertThat(invalidVote.getRankedCandidates(), is(empty()));
		assertThat(invalidVote.getElection(), is(ELECTION));
		assertThat(invalidVote.isNo(), is(true));
		assertThat(invalidVote.isValid(), is(true));
	}

	@Test
	public void testPreferenceVoteCreation() {
		Vote<GenderedCandidate> preferenceVote = Vote.createPreferenceVote(ELECTION, ImmutableList.of(CANDIDATE_B, CANDIDATE_A));

		assertThat(preferenceVote.getRankedCandidates(), contains(CANDIDATE_B, CANDIDATE_A));
		assertThat(preferenceVote.getElection(), is(ELECTION));
		assertThat(preferenceVote.isNo(), is(false));
		assertThat(preferenceVote.isValid(), is(true));
	}

	@Test(expected = IllegalArgumentException.class)
	public void withReplacedCandidateVersionThrowsIfNewElectionVersionDoesNotContainAllCandidates() {
		Vote<GenderedCandidate> vote = Vote.createPreferenceVote(ELECTION, ImmutableList.of(CANDIDATE_B, CANDIDATE_A));

		vote.withReplacedCandidateVersion(
				new Election<>("Example Office", ImmutableSet.of(CANDIDATE_A)),
				CANDIDATE_B.withIsFemale(false)
		);
	}

	@Test(expected = IllegalArgumentException.class)
	public void withReplacedCandidateVersionThrowsIfNewElectionChangesTheOfficeName() {
		Vote<GenderedCandidate> vote = Vote.createPreferenceVote(ELECTION, ImmutableList.of(CANDIDATE_B, CANDIDATE_A));

        GenderedCandidate newCandidateVersion = CANDIDATE_B.withIsFemale(false);
        vote.withReplacedCandidateVersion(
				ELECTION.withOfficeName("New Office Name").withReplacedCandidate(newCandidateVersion),
                newCandidateVersion
		);
	}

	@Test
	public void withReplacedCandidateMigratesElectionAndPreference() {
		Vote<GenderedCandidate> vote = Vote.createPreferenceVote(ELECTION, ImmutableList.of(CANDIDATE_B, CANDIDATE_A));

        GenderedCandidate newCandidateVersion = CANDIDATE_B.withIsFemale(false);
        Vote<GenderedCandidate> migratedVote = vote.withReplacedCandidateVersion(
                ELECTION.withReplacedCandidate(newCandidateVersion),
                newCandidateVersion
        );

        assertThat(migratedVote.getElection(), is(not(sameInstance(ELECTION))));
        assertThat(migratedVote.getElection().getOfficeName(), is(ELECTION.getOfficeName()));
        assertThat(migratedVote.getRankedCandidates(), contains(newCandidateVersion, CANDIDATE_A));
	}

	@Test
	public void testVotesAreNotEqualIfThePreferenceOrderIsDifferent() {
		Vote<GenderedCandidate> voteA = Vote.createPreferenceVote(ELECTION, ImmutableList.of(CANDIDATE_B, CANDIDATE_A));
		Vote<GenderedCandidate> voteB = Vote.createPreferenceVote(ELECTION, ImmutableList.of(CANDIDATE_A, CANDIDATE_B));

		assertThat(voteA.equals(voteB), not(true));
	}
}

