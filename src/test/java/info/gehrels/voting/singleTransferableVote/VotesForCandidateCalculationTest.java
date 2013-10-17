package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.Vote;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import static info.gehrels.voting.singleTransferableVote.VotesForCandidateCalculation.calculateVotesForCandidate;
import static org.apache.commons.math3.fraction.BigFraction.ONE_FIFTH;
import static org.apache.commons.math3.fraction.BigFraction.ONE_HALF;
import static org.apache.commons.math3.fraction.BigFraction.ONE_THIRD;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class VotesForCandidateCalculationTest {

	public static final Candidate PIVOT_CANDIDATE = new Candidate("pivotCandidate");
	public static final Candidate OTHER_CANDIDATE = new Candidate("otherCandidate");
	public static final Election<Candidate> ELECTION = new Election<>("arbitraryOffice",
	                                                                  ImmutableSet
		                                                                  .of(PIVOT_CANDIDATE, OTHER_CANDIDATE));

	@Test
	public void candidateHasZeroVotesWhenThereAreNoVoteStates() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.<VoteState<Candidate>>of());
		assertThat(numberOfVotes, is(BigFraction.ZERO));
	}

	@Test
	public void candidateHasZeroVotesIfAllVoteStatesHaveOtherCandidatesOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       voteStatePreferring(0, OTHER_CANDIDATE),
			                                                       voteStatePreferring(1, OTHER_CANDIDATE)
		                                                       )
		);
		assertThat(numberOfVotes, is(BigFraction.ZERO));
	}

	@Test
	public void candidateHasOneVoteIfTheOnlyVoteStateHasFullVoteWeightAndShowsHerOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       voteStatePreferring(0, PIVOT_CANDIDATE)
		                                                       )
		);
		assertThat(numberOfVotes, is(BigFraction.ONE));
	}

	@Test
	public void candidateHasHalfOfAVoteIfTheOnlyVoteStateHas50PercentVoteWeightAndShowsHerOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       voteStatePreferring(0, PIVOT_CANDIDATE)
				                                                       .withVoteWeight(ONE_HALF)
		                                                       )
		);
		assertThat(numberOfVotes, is(ONE_HALF));
	}

	@Test
	public void candidateHasSumOfTheVoteWeightsOfThoseVoteStatesShowingHerOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       voteStatePreferring(0, PIVOT_CANDIDATE)
				                                                       .withVoteWeight(ONE_HALF),
			                                                       voteStatePreferring(1, OTHER_CANDIDATE)
				                                                       .withVoteWeight(ONE_THIRD),
			                                                       voteStatePreferring(2, OTHER_CANDIDATE)
				                                                       .withVoteWeight(ONE_FIFTH),
			                                                       voteStatePreferring(3, PIVOT_CANDIDATE)
				                                                       .withVoteWeight(ONE_THIRD)
		                                                       )
		);
		assertThat(numberOfVotes, is(new BigFraction(5, 6)));
	}

	private VoteState<Candidate> voteStatePreferring(int id, Candidate candidate) {
		return VoteState.forBallotAndElection(new Ballot<>(id, ImmutableSet
			.of(Vote.createPreferenceVote(ELECTION, ImmutableSet.of(candidate)))), ELECTION).get();
	}


}
