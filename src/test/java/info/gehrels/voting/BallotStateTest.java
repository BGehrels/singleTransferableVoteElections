package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class BallotStateTest {

	public static final Candidate A = new Candidate("A", true);
	public static final Candidate B = new Candidate("B", true);
	public static final Election ELECTION = new Election(TestUtils.OFFICE, 1, 1, ImmutableSet.of(A, B));

	@Test
	public void returnsNewBallotStateWithSameValuesButUpdatedVoteWeight() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState ballotState = new BallotState(ballot, ELECTION);

		BallotState newBallotState = ballotState.withVoteWeight(BigFraction.ONE_THIRD);

		assertThat(newBallotState, is(not(sameInstance(ballotState))));
		assertThat(newBallotState.getPreferredCandidate(), is(equalTo(ballotState.getPreferredCandidate())));
		assertThat(newBallotState.getVoteWeight(), is(not(equalTo(ballotState.getVoteWeight()))));
		assertThat(newBallotState.getVoteWeight(), is(equalTo(BigFraction.ONE_THIRD)));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNullPreferenceIfNoCandidatesWereMarkedOnTheBallot() {
		Ballot ballot = TestUtils.createBallot("", ELECTION);
		BallotState resultingBallotState = new BallotState(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates(ImmutableSet.of(A)));
		assertThat(resultingBallotState.getPreferredCandidate(), is(nullValue()));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNullPreferenceIfNoCandidatesAreInTheCandidateStateMap() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState resultingBallotState = new BallotState(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates(ImmutableSet.<Candidate>of()));
		assertThat(resultingBallotState.getPreferredCandidate(), is(nullValue()));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNullPreferenceIfRemainingMarkedCandidatesAreNotHopefull() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState resultingBallotState = new BallotState(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates(ImmutableSet.of(A,B)).withElected(A).withLooser(B));
		assertThat(resultingBallotState.getPreferredCandidate(), is(nullValue()));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsSameStateIfCurrentCandidateIsStillHopeful() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState resultingBallotState = new BallotState(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates(ImmutableSet.of(A,B)));
		assertThat(resultingBallotState.getPreferredCandidate(), is(A));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNextHopefulPreferenceIfCurrentPreferenceIsNotHopeful() {
		Ballot ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState resultingBallotState = new BallotState(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates(ImmutableSet.of(A,B)).withLooser(A));
		assertThat(resultingBallotState.getPreferredCandidate(), is(B));
	}

}
