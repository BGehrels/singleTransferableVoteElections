package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculationListener;
import info.gehrels.voting.singleTransferableVote.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.Matcher;
import org.junit.Test;

import static info.gehrels.voting.TestUtils.createBallot;
import static info.gehrels.voting.singleTransferableVote.BallotStateMatchers.aBallotState;
import static info.gehrels.voting.singleTransferableVote.BallotStateMatchers.withBallotId;
import static info.gehrels.voting.singleTransferableVote.BallotStateMatchers.withVoteWeight;
import static org.apache.commons.math3.fraction.BigFraction.ONE_HALF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class WeightedInclusiveGregoryMethodTest {
	public static final Candidate CANDIDATE_A = new Candidate("A");
	public static final Candidate CANDIDATE_B = new Candidate("B");
	public static final Candidate CANDIDATE_C = new Candidate("C");
	public static final Candidate CANDIDATE_D = new Candidate("D");

	public static final Election<Candidate> ELECTION = new Election<>("Example Office",
	                                                                  ImmutableSet
		                                                     .of(CANDIDATE_A, CANDIDATE_B, CANDIDATE_C, CANDIDATE_D));
	public static final Ballot<Candidate> BALLOT_ABCD = createBallot("ABCD", ELECTION);
	public static final Ballot<Candidate> BALLOT_ACD = createBallot("ACD", ELECTION);
	public static final Ballot<Candidate> BALLOT_BCDA = createBallot("BCDA", ELECTION);
	public static final Ballot<Candidate> BALLOT_NO_VOTES = createBallot("", ELECTION);

	public static final ImmutableList<BallotState<Candidate>> BALLOT_STATES_FIXTURE = ImmutableList.of(
		new BallotState<>(BALLOT_ABCD, ELECTION),
		new BallotState<>(BALLOT_ACD, ELECTION).withVoteWeight(BigFraction.THREE_QUARTERS),
		new BallotState<>(BALLOT_BCDA, ELECTION),
		new BallotState<>(BALLOT_NO_VOTES, ELECTION)
	);

	private ElectionCalculationListener<Candidate> mock = mock(ElectionCalculationListener.class);
	private WeightedInclusiveGregoryMethod<Candidate> weightedInclusiveGregoryMethod = new WeightedInclusiveGregoryMethod<>(mock);

	@Test
	public void reducesVoteWeightOfThoseBallotsThatHadTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRedistributor<Candidate> voteWeightRedistributor = weightedInclusiveGregoryMethod.redistributorFor();

		ImmutableCollection<BallotState<Candidate>> ballotStates = voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, new BigFraction(7, 8), BALLOT_STATES_FIXTURE);

		Matcher<BallotState<Candidate>> stateMatcher = allOf(
			withVoteWeight(ONE_HALF),
			withBallotId(BALLOT_ABCD.id)
		);
		Matcher<BallotState<Candidate>> stateMatcher1 = allOf(
			withBallotId(BALLOT_ACD.id),
			withVoteWeight(new BigFraction(3, 8))
		);
		assertThat(ballotStates, hasItems(
			aBallotState(stateMatcher),
			aBallotState(stateMatcher1)
		));
	}

	@Test
	public void reportsEachVoteWeightRedistribution() {
		VoteWeightRedistributor<Candidate> voteWeightRedistributor = weightedInclusiveGregoryMethod.redistributorFor();

		voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, new BigFraction(7, 8), BALLOT_STATES_FIXTURE);

		verify(mock).voteWeightRedistributed(ONE_HALF, BALLOT_ABCD.id, ONE_HALF);
		verify(mock).voteWeightRedistributed(ONE_HALF, BALLOT_ACD.id, new BigFraction(3, 8));
	}


	@Test
	public void doesNotReduceVoteWeightOfThoseBallotsThatDidNotHaveTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRedistributor<Candidate> voteWeightRedistributor = weightedInclusiveGregoryMethod.redistributorFor();

		ImmutableCollection<BallotState<Candidate>> ballotStates = voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, BigFraction.ONE, BALLOT_STATES_FIXTURE);

		Matcher<BallotState<?>> first = withBallotId(BALLOT_BCDA.id);
		Matcher<BallotState<?>> first1 = withBallotId(BALLOT_NO_VOTES.id);
		Matcher<BallotState<?>> second = withVoteWeight(BigFraction.ONE);
		Matcher<BallotState<?>> second1 = withVoteWeight(BigFraction.ONE);
		Matcher<BallotState<Candidate>> ballotStateMatcher2 = allOf(first, second);
		Matcher<BallotState<Candidate>> ballotStateMatcher = aBallotState(ballotStateMatcher2);
		Matcher<BallotState<Candidate>> ballotStateMatcher3 = allOf(first1, second1);
		Matcher<BallotState<Candidate>> ballotStateMatcher1 = aBallotState(ballotStateMatcher3);
		Matcher<Iterable<BallotState<Candidate>>> iterableMatcher =  hasItems(
			ballotStateMatcher,
			ballotStateMatcher1
		);
		assertThat(ballotStates, iterableMatcher);
	}


}
