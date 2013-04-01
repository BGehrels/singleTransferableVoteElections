package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.Test;

import static info.gehrels.voting.TestUtils.OFFICE;
import static info.gehrels.voting.TestUtils.createBallot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WeightedInclusiveGregoryMethodTest {
	public static final Candidate CANDIDATE_A = new Candidate("A", true);
	public static final Candidate CANDIDATE_B = new Candidate("B", true);
	public static final Candidate CANDIDATE_C = new Candidate("C", true);
	public static final Candidate CANDIDATE_D = new Candidate("D", true);

	public static final Election ELECTION = new Election(OFFICE, 0, 1, ImmutableSet.of(CANDIDATE_A, CANDIDATE_B, CANDIDATE_C, CANDIDATE_D));
	public static final Ballot BALLOT_ABCD = createBallot("ABCD", ELECTION);
	public static final Ballot BALLOT_BACD = createBallot("BACD", ELECTION);
	public static final Ballot BALLOT_BCDA = createBallot("BCDA", ELECTION);
	public static final Ballot BALLOT_NO_VOTES = createBallot("", ELECTION);

	public static final ImmutableList<BallotState> BALLOT_STATES_FIXTURE = ImmutableList.of(
				new BallotState(BALLOT_ABCD, ELECTION),
				new BallotState(BALLOT_BACD, ELECTION).withNextPreference().withVoteWeight(BigFraction.THREE_QUARTERS),
				new BallotState(BALLOT_BCDA, ELECTION),
				new BallotState(BALLOT_NO_VOTES, ELECTION)
			);

	private ElectionCalculationListener mock = mock(ElectionCalculationListener.class);
	private WeightedInclusiveGregoryMethod weightedInclusiveGregoryMethod = new WeightedInclusiveGregoryMethod(mock);

	@Test
	public void reducesVoteWeightOfThoseBallotsThatHadTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRedistributor voteWeightRedistributor = weightedInclusiveGregoryMethod.redistributorFor();

		ImmutableCollection<BallotState> ballotStates = voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, new BigFraction(7,8), BALLOT_STATES_FIXTURE);

		assertThat(ballotStates, hasItems(
			aBallotState(allOf(withBallot(BALLOT_ABCD), withVoteWeight(BigFraction.ONE_HALF))),
			aBallotState(allOf(withBallot(BALLOT_BACD), withVoteWeight(new BigFraction(3,8))))
		));
	}

	@Test
	public void reportsEachVoteWeightRedistribution() {
		VoteWeightRedistributor voteWeightRedistributor = weightedInclusiveGregoryMethod.redistributorFor();

		voteWeightRedistributor.redistributeExceededVoteWeight(CANDIDATE_A, new BigFraction(7,8), BALLOT_STATES_FIXTURE);

		verify(mock).voteWeightRedistributed(BigFraction.ONE_HALF, BALLOT_ABCD.id, BigFraction.ONE_HALF);
		verify(mock).voteWeightRedistributed(BigFraction.ONE_HALF, BALLOT_BACD.id, new BigFraction(3,8));
	}


	@Test
	public void doesNotReduceVoteWeightOfThoseBallotsThatDidNotHaveTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRedistributor voteWeightRedistributor = weightedInclusiveGregoryMethod.redistributorFor();

		ImmutableCollection<BallotState> ballotStates = voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, BigFraction.ONE, BALLOT_STATES_FIXTURE);

		assertThat(ballotStates, hasItems(
			aBallotState(allOf(withBallot(BALLOT_BCDA), withVoteWeight(BigFraction.ONE))),
			aBallotState(allOf(withBallot(BALLOT_NO_VOTES), withVoteWeight(BigFraction.ONE)))
		));
	}

	private Matcher<BallotState> aBallotState(Matcher<BallotState> ballotStateMatcher) {
		return new FeatureMatcher<BallotState, BallotState>(ballotStateMatcher, "a BallotState", "") {

			@Override
			protected BallotState featureValueOf(BallotState ballotState) {
				return ballotState;
			}
		};
	}

	private Matcher<BallotState> withVoteWeight(BigFraction voteWeight) {
		return new FeatureMatcher<BallotState, BigFraction>(is(equalTo(voteWeight)), "with vote weight", "vote weight") {

			@Override
			protected BigFraction featureValueOf(BallotState ballotState) {
				return ballotState.getVoteWeight();
			}
		};
	}

	private Matcher<BallotState> withBallot(Ballot ballot) {
		return new FeatureMatcher<BallotState, Ballot>(is(sameInstance(ballot)), "with Ballot", "Ballot") {

			@Override
			protected Ballot featureValueOf(BallotState ballotState) {
				return ballotState.ballot;
			}
		};
	}




}
