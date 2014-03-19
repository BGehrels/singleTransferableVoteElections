package info.gehrels.voting.singleTransferableVote;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.Matchers;
import org.junit.Test;

import static info.gehrels.voting.TestUtils.createBallot;
import static info.gehrels.voting.singleTransferableVote.VoteStateMatchers.aVoteState;
import static info.gehrels.voting.singleTransferableVote.VoteStateMatchers.withBallotId;
import static info.gehrels.voting.singleTransferableVote.VoteStateMatchers.withVoteWeight;
import static org.apache.commons.math3.fraction.BigFraction.ONE_HALF;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class WeightedInclusiveGregoryMethodTest {
	public static final Candidate CANDIDATE_A = new Candidate("A");
	public static final Candidate CANDIDATE_B = new Candidate("B");
	public static final Candidate CANDIDATE_C = new Candidate("C");
	public static final Candidate CANDIDATE_D = new Candidate("D");

	public static final ImmutableSet<Candidate> CANDIDATES = ImmutableSet
		.of(CANDIDATE_A, CANDIDATE_B, CANDIDATE_C,
		    CANDIDATE_D);
	public static final Election<Candidate> ELECTION = new Election<>("Example Office",    CANDIDATES);
	public static final Ballot<Candidate> BALLOT_ABCD = createBallot("ABCD", ELECTION);
	public static final Ballot<Candidate> BALLOT_ACD = createBallot("ACD", ELECTION);
	public static final Ballot<Candidate> BALLOT_BCDA = createBallot("BCDA", ELECTION);
	public static final Ballot<Candidate> BALLOT_NO_VOTES = createBallot("", ELECTION);

	public static final ImmutableList<VoteState<Candidate>> VOTE_STATES_FIXTURE = ImmutableList.of(
		VoteState.forBallotAndElection(BALLOT_ABCD, ELECTION).get(),
		VoteState.forBallotAndElection(BALLOT_ACD, ELECTION).get().withVoteWeight(BigFraction.THREE_QUARTERS),
		VoteState.forBallotAndElection(BALLOT_BCDA, ELECTION).get(),
		VoteState.forBallotAndElection(BALLOT_NO_VOTES, ELECTION).get()
	);

	private final STVElectionCalculationListener<Candidate> listenerMock = mock(STVElectionCalculationListener.class);
	private final WeightedInclusiveGregoryMethod<Candidate> wigm = new WeightedInclusiveGregoryMethod<>(
		listenerMock);

	private static final CandidateStates<Candidate> ALL_HOPEFULL_CANDIDATE_STATE = new CandidateStates<>(CANDIDATES);

	@Test
	public void reducesVoteWeightOfThoseBallotsThatHadTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRedistributor<Candidate> voteWeightRedistributor = wigm.redistributorFor();

		ImmutableCollection<VoteState<Candidate>> voteStates = voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, new BigFraction(7, 8), VOTE_STATES_FIXTURE, ALL_HOPEFULL_CANDIDATE_STATE);

		assertThat(voteStates, hasItems(
			aVoteState(Matchers.<VoteState<Candidate>>allOf(
				withVoteWeight(ONE_HALF),
				withBallotId(BALLOT_ABCD.id)
			)),
			aVoteState(Matchers.<VoteState<Candidate>>allOf(
				withBallotId(BALLOT_ACD.id),
				withVoteWeight(new BigFraction(3, 8))
			))
		));
	}

	@Test
	public void reportsEachVoteWeightRedistribution() {
		VoteWeightRedistributor<Candidate> voteWeightRedistributor = wigm.redistributorFor();

		voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, new BigFraction(7, 8), VOTE_STATES_FIXTURE, ALL_HOPEFULL_CANDIDATE_STATE);

		verify(listenerMock).voteWeightRedistributed(BALLOT_ABCD.id, CANDIDATE_A, Optional.of(CANDIDATE_B), ONE_HALF,
		                                             ONE_HALF);
		verify(listenerMock).voteWeightRedistributed(BALLOT_ACD.id, CANDIDATE_A, Optional.of(CANDIDATE_C), ONE_HALF,
		                                             new BigFraction(3, 8));
	}


	@Test
	public void doesNotReduceVoteWeightOfThoseVotesThatDidNotHaveTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRedistributor<Candidate> voteWeightRedistributor = wigm.redistributorFor();

		ImmutableCollection<VoteState<Candidate>> voteStates = voteWeightRedistributor
			.redistributeExceededVoteWeight(CANDIDATE_A, BigFraction.ONE, VOTE_STATES_FIXTURE, ALL_HOPEFULL_CANDIDATE_STATE);

		assertThat(voteStates, hasItems(
			aVoteState(Matchers.<VoteState<Candidate>>allOf(
				withBallotId(BALLOT_BCDA.id),
				withVoteWeight(BigFraction.ONE))),
			aVoteState(Matchers.<VoteState<Candidate>>allOf(
				withBallotId(BALLOT_NO_VOTES.id),
				withVoteWeight(BigFraction.ONE)))
		));
	}


}
