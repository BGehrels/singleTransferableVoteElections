package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static info.gehrels.voting.singleTransferableVote.VotesByCandidateCalculation.calculateVotesByCandidate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class VotesByCandidateCalculationTest {

	public static final ImmutableList<BallotState<Candidate>> EMPTY_BALLOT_LIST = ImmutableList.of();
	public static final ImmutableSet<Candidate> EMPTY_CANDIDATE_SET = ImmutableSet.of();
	public static final Candidate CANDIDATE_PETER = new Candidate("Peter");
	public static final Candidate CANDIDATE_JOHN = new Candidate("John");
	public static final Candidate CANDIDATE_MARTA = new Candidate("Marta");

	public static final ImmutableSet<Candidate> ALL_CANDIDATES = ImmutableSet
		.of(CANDIDATE_PETER, CANDIDATE_JOHN, CANDIDATE_MARTA);

	public static final Election<Candidate> ELECTION = new Election<>("arbitraryOfiice", ALL_CANDIDATES);

	@Test
	public void returnsEmtpyMapIfCandidateSetAndBallotStatesAreEmpty() {
		Map<Candidate, BigFraction> votesByCandidate = calculateVotesByCandidate(EMPTY_CANDIDATE_SET,
		                                                                         EMPTY_BALLOT_LIST);

		assertThat(votesByCandidate, is(equalTo(Collections.<Candidate, BigFraction>emptyMap())));
	}

	@Test
	public void returnsZeroVotesForCandidatesWhoAreNotMentionedOnAnyBallot() {
		Map<Candidate, BigFraction> votesByCandidate = calculateVotesByCandidate(ImmutableSet.of(CANDIDATE_PETER),
		                                                                         EMPTY_BALLOT_LIST);

		assertThat(votesByCandidate.size(), is(1));
		assertThat(votesByCandidate, hasEntry(CANDIDATE_PETER, BigFraction.ZERO));
	}

	@Test
	public void returnsCorrectNumberOfVotesForMultipleCandidates() {
		Map<Candidate, BigFraction> votesByCandidate =
			calculateVotesByCandidate(ALL_CANDIDATES, ImmutableList.of(
				createBallotStateFor(CANDIDATE_JOHN, CANDIDATE_MARTA),
				createBallotStateFor(CANDIDATE_MARTA, CANDIDATE_JOHN),
				createBallotStateFor(),
				createBallotStateFor(CANDIDATE_PETER, CANDIDATE_MARTA)
			)
		);

		assertThat(votesByCandidate.size(), is(3));
		assertThat(votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ONE),
			hasEntry(CANDIDATE_JOHN, BigFraction.ONE),
			hasEntry(CANDIDATE_MARTA, BigFraction.ONE)
		));
	}

	@Test
	public void returnsCorrectNumberOfVotesForMultipleCandidatesAndFractionalVoteWeights() {
		Map<Candidate, BigFraction> votesByCandidate =
			calculateVotesByCandidate(ALL_CANDIDATES, ImmutableList.of(
				createBallotStateFor(CANDIDATE_JOHN, CANDIDATE_MARTA),
				createBallotStateFor(CANDIDATE_JOHN, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_FIFTH),
				createBallotStateFor(),
				createBallotStateFor(CANDIDATE_PETER, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_THIRD)
			)
		);

		assertThat(votesByCandidate.size(), is(3));
		assertThat(votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ONE_THIRD),
			hasEntry(CANDIDATE_JOHN, new BigFraction(6,5)),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));
	}

	private BallotState<Candidate> createBallotStateFor(Candidate... candidates) {
		return new BallotState<>(new Ballot<>(id, ImmutableSet.of(new ElectionCandidatePreference<>(
			ELECTION, ImmutableSet.copyOf(candidates)))), ELECTION
		);
	}


}
