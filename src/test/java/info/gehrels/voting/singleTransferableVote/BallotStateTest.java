package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.TestUtils;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.Matcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

public class BallotStateTest {

	public static final Candidate A = new Candidate("A");
	public static final Candidate B = new Candidate("B");
	public static final Election<Candidate> ELECTION = new Election<>(TestUtils.OFFICE, ImmutableSet.of(A, B));

	@Test
	public void returnsNewBallotStateWithSameValuesButUpdatedVoteWeight() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState<Candidate> ballotState = new BallotState<>(ballot, ELECTION);

		BallotState<Candidate> newBallotState = ballotState.withVoteWeight(BigFraction.ONE_THIRD);

		Matcher<BallotState<Candidate>> ballotStateMatcher = is(not(sameInstance(ballotState)));
		assertThat(newBallotState, ballotStateMatcher);
		assertThat(newBallotState.getPreferredCandidate(), is(equalTo(ballotState.getPreferredCandidate())));
		assertThat(newBallotState.getVoteWeight(), is(not(equalTo(ballotState.getVoteWeight()))));
		assertThat(newBallotState.getVoteWeight(), is(equalTo(BigFraction.ONE_THIRD)));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNullPreferenceIfNoCandidatesWereMarkedOnTheBallot() {
		Ballot<Candidate> ballot = TestUtils.createBallot("", ELECTION);
		BallotState<Candidate> resultingBallotState = new BallotState<>(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(A)));
		assertThat(resultingBallotState.getPreferredCandidate(), is(nullValue()));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNullPreferenceIfNoCandidatesAreInTheCandidateStateMap() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState<Candidate> resultingBallotState = new BallotState<>(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.<Candidate>of()));
		assertThat(resultingBallotState.getPreferredCandidate(), is(nullValue()));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNullPreferenceIfRemainingMarkedCandidatesAreNotHopefull() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState<Candidate> resultingBallotState = new BallotState<>(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(A,B)).withElected(A).withLooser(B));
		assertThat(resultingBallotState.getPreferredCandidate(), is(nullValue()));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsSameStateIfCurrentCandidateIsStillHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState<Candidate> resultingBallotState = new BallotState<>(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(A,B)));
		assertThat(resultingBallotState.getPreferredCandidate(), is(A));
	}

	@Test
	public void moveToNextHopefulCandidateReturnsStateWithNextHopefulPreferenceIfCurrentPreferenceIsNotHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		BallotState<Candidate> resultingBallotState = new BallotState<>(ballot, ELECTION)
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(A,B)).withLooser(A));
		assertThat(resultingBallotState.getPreferredCandidate(), is(B));
	}

}
