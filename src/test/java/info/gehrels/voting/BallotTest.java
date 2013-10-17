package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static info.gehrels.voting.OptionalMatchers.anAbsentOptional;
import static info.gehrels.voting.Vote.createPreferenceVote;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class BallotTest {
	private static final Candidate CANDIDATE = new Candidate("Peter");

	private static final Election<Candidate> ELECTION_1 = new Election<>("Office1", ImmutableSet.of(CANDIDATE));
	private static final Election<Candidate> ELECTION_2 = new Election<>("Office2", ImmutableSet.of(CANDIDATE));
	public static final Vote<Candidate> VOTE_FOR_ELECTION_1 = createPreferenceVote(ELECTION_1,
	                                                                               ImmutableSet.of(CANDIDATE));

	@Test
	public void returnsAbsentOptionalIfBallotContainsNoVoteForThisElection() {
		ImmutableSet<Vote<Candidate>> voteOnlyForElection1
			= ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<Candidate> ballot = new Ballot<>(0, voteOnlyForElection1);

		assertThat(ballot.getVote(ELECTION_2), is(anAbsentOptional()));
	}


	@Test
	public void returnsTheVoteIfBallotContainsOneThisElection() {
		ImmutableSet<Vote<Candidate>> votes = ImmutableSet.of(VOTE_FOR_ELECTION_1);
		Ballot<Candidate> ballot = new Ballot<>(0, votes);

		assertThat(ballot.getVote(ELECTION_1).get().getElection(), is(ELECTION_1));
	}
}
