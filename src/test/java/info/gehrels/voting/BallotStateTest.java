package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public class BallotStateTest {

	public static final Candidate A = new Candidate("A", true);
	public static final Candidate B = new Candidate("B", true);
	public static final Election ELECTION = new Election(TestUtils.OFFICE, 1, 1, ImmutableSet.of(A, B));

	@Test
	public void returnsNewBallotStateWithSameValuesButUpdatedVoteWeight() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState ballotState = new BallotState(ballot, ELECTION);

		BallotState newBallotState = ballotState.withReducedVoteWeight(0.33);

		assertThat(newBallotState, is(not(sameInstance(ballotState))));
		assertThat(newBallotState.getPreferredCandidate(), is(equalTo(ballotState.getPreferredCandidate())));
		assertThat(newBallotState.getVoteWeight(), is(not(equalTo(ballotState.getVoteWeight()))));
		assertThat(newBallotState.getVoteWeight(), is(equalTo(0.33)));
	}

	@Test
	public void returnsNewBallotStateWithSameValuesButUpdatedPreferredCandidate() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState ballotState = new BallotState(ballot, ELECTION);

		BallotState newBallotState = ballotState.withNextPreference();

		assertThat(newBallotState, is(not(sameInstance(ballotState))));
		assertThat(newBallotState.getVoteWeight(), is(equalTo(ballotState.getVoteWeight())));
		assertThat(newBallotState.getPreferredCandidate(), is(not(equalTo(ballotState.getPreferredCandidate()))));
		assertThat(newBallotState.getPreferredCandidate(), is(equalTo(B)));
	}
}
