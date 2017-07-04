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

import static info.gehrels.voting.OptionalMatchers.anEmptyOptional;
import static info.gehrels.voting.Vote.createPreferenceVote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public final class BallotTest {
	private static final Candidate CANDIDATE_1 = new Candidate("Peter");
	private static final Candidate CANDIDATE_2 = new Candidate("Peter");

	private static final Election<Candidate> ELECTION_1 = new Election<>("Office1", ImmutableSet.of(CANDIDATE_1));
	private static final Election<Candidate> NEW_ELECTION_1 = new Election<>("Office 1 migrated", ImmutableSet.of(CANDIDATE_1));
	private static final Election<Candidate> ELECTION_2 = new Election<>("Office2", ImmutableSet.of(CANDIDATE_2));

	private static final Vote<Candidate> VOTE_FOR_ELECTION_1 = createPreferenceVote(ELECTION_1,
	                                                                               ImmutableSet.of(CANDIDATE_1));
	private static final Vote<Candidate> VOTE_FOR_ELECTION_2 = createPreferenceVote(ELECTION_2,
	                                                                               ImmutableSet.of(CANDIDATE_2));

	@Test
	public void returnsAbsentOptionalIfBallotContainsNoVoteForThisElection() {
		ImmutableSet<Vote<Candidate>> voteOnlyForElection1
			= ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<Candidate> ballot = new Ballot<>(0, voteOnlyForElection1);

		assertThat(ballot.getVote(ELECTION_2), is(anEmptyOptional()));
	}

	@Test
	public void returnsTheVoteIfBallotContainsOneThisElection() {
		ImmutableSet<Vote<Candidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<Candidate> ballot = new Ballot<>(0, votes);

		assertThat(ballot.getVote(ELECTION_1).get().getElection(), is(ELECTION_1));
	}

	@Test
	public void withReplacedElectionReturnsNewBallotWithMigratedVotes() {
		ImmutableSet<Vote<Candidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<Candidate> originalBallot = new Ballot<>(0, votes);

		Ballot<Candidate> newBallot = originalBallot.withReplacedElection(ELECTION_1.getOfficeName(), NEW_ELECTION_1);

		assertThat(newBallot, is(not(sameInstance(originalBallot))));
		assertThat(newBallot.getVote(ELECTION_1).isPresent(), is(false));
		assertThat(newBallot.getVote(NEW_ELECTION_1).isPresent(), is(true));
		assertThat(newBallot.getVote(NEW_ELECTION_1).get().getElection(), is(NEW_ELECTION_1));
		assertThat(newBallot.getVote(NEW_ELECTION_1).get().getRankedCandidates().iterator().next(), is(sameInstance(CANDIDATE_1)));
	}

	@Test
	public void withReplacedElectionWontTouchUnrelatedElectionVotes() {
		ImmutableSet<Vote<Candidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1, VOTE_FOR_ELECTION_2);
		Ballot<Candidate> originalBallot = new Ballot<>(0, votes);

		Ballot<Candidate> newBallot = originalBallot.withReplacedElection(ELECTION_1.getOfficeName(), NEW_ELECTION_1);

		assertThat(newBallot, is(not(sameInstance(originalBallot))));
		assertThat(newBallot.getVote(ELECTION_2).isPresent(), is(true));
		assertThat(newBallot.getVote(ELECTION_2).get(), is(sameInstance(VOTE_FOR_ELECTION_2)));
	}
}
