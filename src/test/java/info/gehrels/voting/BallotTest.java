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
import org.junit.jupiter.api.Test;

import static info.gehrels.voting.OptionalMatchers.anEmptyOptional;
import static info.gehrels.voting.Vote.createPreferenceVote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class BallotTest {
	private static final GenderedCandidate CANDIDATE_1 = new GenderedCandidate("Peter", true);
	private static final GenderedCandidate CANDIDATE_2 = new GenderedCandidate("Petra", false);

	private static final GenderedElection ELECTION_1 = new GenderedElection("Office1", 1, 1, ImmutableSet.of(CANDIDATE_1, CANDIDATE_2));
	private static final GenderedElection ELECTION_2 = new GenderedElection("Office2", 1, 1, ImmutableSet.of(CANDIDATE_2));

	private static final Vote<GenderedCandidate> VOTE_FOR_ELECTION_1 = createPreferenceVote(ELECTION_1,
	        ImmutableList.of(CANDIDATE_1));
	private static final Vote<GenderedCandidate> VOTE_FOR_ELECTION_2 = createPreferenceVote(ELECTION_2,
			ImmutableList.of(CANDIDATE_2));

	@Test
	public void returnsAbsentOptionalIfBallotContainsNoVoteForThisElection() {
		ImmutableSet<Vote<GenderedCandidate>> voteOnlyForElection1
			= ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<GenderedCandidate> ballot = new Ballot<>(0, voteOnlyForElection1);

		assertThat(ballot.getVote(ELECTION_2), is(anEmptyOptional()));
	}

	@Test
	public void returnsTheVoteIfBallotContainsOneThisElection() {
		ImmutableSet<Vote<GenderedCandidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<GenderedCandidate> ballot = new Ballot<>(0, votes);

		assertThat(ballot.getVote(ELECTION_1).get().getElection(), is(ELECTION_1));
	}

	@Test
	public void withReplacedElectionReturnsNewBallotWithMigratedVotes() {
		ImmutableSet<Vote<GenderedCandidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<GenderedCandidate> originalBallot = new Ballot<>(0, votes);

        GenderedElection migratedElection1 = ELECTION_1.withOfficeName("Office1 migrated");
        Ballot<GenderedCandidate> newBallot = originalBallot.withReplacedElection(ELECTION_1.getOfficeName(), migratedElection1);

		assertThat(newBallot, is(not(sameInstance(originalBallot))));
		assertThat(newBallot.getVote(ELECTION_1).isPresent(), is(false));
		assertThat(newBallot.getVote(migratedElection1).isPresent(), is(true));
		assertThat(newBallot.getVote(migratedElection1).get().getElection(), is(migratedElection1));
		assertThat(newBallot.getVote(migratedElection1).get().getRankedCandidates().iterator().next(), is(sameInstance(CANDIDATE_1)));
	}

	@Test
	public void withReplacedElectionWontTouchUnrelatedElectionVotes() {
		ImmutableSet<Vote<GenderedCandidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1, VOTE_FOR_ELECTION_2);
		Ballot<GenderedCandidate> originalBallot = new Ballot<>(0, votes);

        Ballot<GenderedCandidate> newBallot = originalBallot.withReplacedElection(ELECTION_1.getOfficeName(), ELECTION_1.withOfficeName("Office1 migrated"));

		assertThat(newBallot, is(not(sameInstance(originalBallot))));
		assertThat(newBallot.getVote(ELECTION_2).isPresent(), is(true));
		assertThat(newBallot.getVote(ELECTION_2).get(), is(sameInstance(VOTE_FOR_ELECTION_2)));
	}

	@Test()
	public void withReplacedElectionThrowsIfCandidatesGotChanged() {
		ImmutableSet<Vote<GenderedCandidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1, VOTE_FOR_ELECTION_2);
		Ballot<GenderedCandidate> originalBallot = new Ballot<>(0, votes);

		GenderedElection electionWithChangedCandidate = ELECTION_1.withReplacedCandidate(CANDIDATE_1.withIsFemale(false));
		assertThrows(
				IllegalArgumentException.class,
				() -> originalBallot.withReplacedElection(ELECTION_1.getOfficeName(), electionWithChangedCandidate)
		);
	}

	@Test
	public void withReplacedCandidateVersionReturnsNewBallotWithMigratedVotes() {
		ImmutableSet<Vote<GenderedCandidate>> votes = ImmutableSet.of(createPreferenceVote(ELECTION_1, ImmutableList.of(CANDIDATE_1, CANDIDATE_2)));
		Ballot<GenderedCandidate> originalBallot = new Ballot<>(0, votes);

		GenderedCandidate newVersionOfCandidate1 = CANDIDATE_1.withIsFemale(false);
        GenderedElection adaptedElection = ELECTION_1.withReplacedCandidate(newVersionOfCandidate1);
        Ballot<GenderedCandidate> newBallot = originalBallot.withReplacedCandidateVersion(adaptedElection, newVersionOfCandidate1);

		assertThat(newBallot, is(not(sameInstance(originalBallot))));
		assertThat(newBallot.getVote(ELECTION_1).isPresent(), is(false));
		assertThat(newBallot.getVote(adaptedElection).isPresent(), is(true));
		assertThat(newBallot.getVote(adaptedElection).get().getElection(), is(adaptedElection));
		assertThat(newBallot.getVote(adaptedElection).get().getRankedCandidates(), contains(sameInstance(newVersionOfCandidate1), sameInstance(CANDIDATE_2)));
	}

	@Test
	public void withReplacedCandidateVersionWontTouchUnrelatedElectionVotes() {
		ImmutableSet<Vote<GenderedCandidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1, VOTE_FOR_ELECTION_2);
		Ballot<GenderedCandidate> originalBallot = new Ballot<>(0, votes);


        GenderedCandidate newVersionOfCandidate1 = CANDIDATE_1.withIsFemale(false);
        GenderedElection adaptedElection = ELECTION_1.withReplacedCandidate(newVersionOfCandidate1);
		Ballot<GenderedCandidate> newBallot = originalBallot.withReplacedCandidateVersion(adaptedElection, newVersionOfCandidate1);

		assertThat(newBallot, is(not(sameInstance(originalBallot))));
		assertThat(newBallot.getVote(ELECTION_2).isPresent(), is(true));
		assertThat(newBallot.getVote(ELECTION_2).get(), is(sameInstance(VOTE_FOR_ELECTION_2)));
	}
}
