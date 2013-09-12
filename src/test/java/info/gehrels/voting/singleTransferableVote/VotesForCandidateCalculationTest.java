package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import static info.gehrels.voting.singleTransferableVote.VotesForCandidateCalculation.calculateVotesForCandidate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class VotesForCandidateCalculationTest {

	public static final Candidate PIVOT_CANDIDATE = new Candidate("pivotCandidate");
	public static final Candidate OTHER_CANDIDATE = new Candidate("otherCandidate");
	public static final Election<Candidate> ELECTION = new Election<>("arbitraryOffice",
	                                                                  ImmutableSet.of(PIVOT_CANDIDATE, OTHER_CANDIDATE));

	@Test
	public void candidateHasZeroVotesWhenThereAreNoBallots() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.<BallotState<Candidate>>of());
		assertThat(numberOfVotes, is(BigFraction.ZERO));
	}

	@Test
	public void candidateHasZeroVotesIfAllBallotsHaveOtherCandidatesOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       ballotStatePreferring(OTHER_CANDIDATE),
			                                                       ballotStatePreferring(OTHER_CANDIDATE)
		                                                       )
		);
		assertThat(numberOfVotes, is(BigFraction.ZERO));
	}

	@Test
	public void candidateHasOneVoteIfTheOnlyBallotHasFullVoteWeightAndShowsHerOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       ballotStatePreferring(PIVOT_CANDIDATE)
		                                                       )
		);
		assertThat(numberOfVotes, is(BigFraction.ONE));
	}

	@Test
	public void candidateHasHalfOfAVoteIfTheOnlyBallotHas50PercentVoteWeightAndShowsHerOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       ballotStatePreferring(PIVOT_CANDIDATE).withVoteWeight(
				                                                       BigFraction.ONE_HALF)
		                                                       )
		);
		assertThat(numberOfVotes, is(BigFraction.ONE_HALF));
	}

	@Test
	public void candidateHasSumOfTheVoteWeightsOfThoseBallotsShowingHerOnTop() {
		BigFraction numberOfVotes = calculateVotesForCandidate(PIVOT_CANDIDATE,
		                                                       ImmutableList.of(
			                                                       ballotStatePreferring(PIVOT_CANDIDATE).withVoteWeight(BigFraction.ONE_HALF),
			                                                       ballotStatePreferring(OTHER_CANDIDATE).withVoteWeight(BigFraction.ONE_THIRD),
			                                                       ballotStatePreferring(OTHER_CANDIDATE).withVoteWeight(BigFraction.ONE_FIFTH),
			                                                       ballotStatePreferring(PIVOT_CANDIDATE).withVoteWeight(BigFraction.ONE_THIRD)
		                                                       )
		);
		assertThat(numberOfVotes, is(new BigFraction(5,6)));
	}

	private BallotState<Candidate> ballotStatePreferring(Candidate candidate) {
		return new BallotState<>(new Ballot<>(
			id, ImmutableSet.of(new ElectionCandidatePreference<>(ELECTION, ImmutableSet.of(candidate)))),
		                       ELECTION);
	}


}
