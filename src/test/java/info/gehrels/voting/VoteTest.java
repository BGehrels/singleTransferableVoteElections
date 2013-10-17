package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public final class VoteTest {

	public static final Candidate CANDIDATE_A = new Candidate("A");
	public static final Candidate CANDIDATE_B = new Candidate("B");
	public static final Election<Candidate> ELECTION = new Election<>("Example Office", ImmutableSet.of(CANDIDATE_A,
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
		Vote<Candidate> invalidVote = Vote.createPreferenceVote(ELECTION, ImmutableSet
			.<Candidate>of(CANDIDATE_B, CANDIDATE_A));
		assertThat(invalidVote.getRankedCandidates(), contains(CANDIDATE_B, CANDIDATE_A));
		assertThat(invalidVote.getElection(), is(ELECTION));
		assertThat(invalidVote.isNo(), is(false));
		assertThat(invalidVote.isValid(), is(true));
	}
}

