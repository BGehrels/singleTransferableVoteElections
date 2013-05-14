package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;

import static info.gehrels.voting.VotesByCandidateCalculation.calculateVotesByCandidate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class VotesByCandidateCalculationTest {

	public static final ImmutableList<BallotState> EMPTY_BALLOT_LIST = ImmutableList.of();
	public static final ImmutableSet<Candidate> EMPTY_CANDIDATE_SET = ImmutableSet.of();
	public static final Candidate CANDIDATE_PETER = new Candidate("Peter", true);
	public static final Candidate CANDIDATE_JOHN = new Candidate("John", false);
	public static final Candidate CANDIDATE_MARTA = new Candidate("Marta", true);

	public static final ImmutableSet<Candidate> ALL_CANDIDATES = ImmutableSet
		.of(CANDIDATE_PETER, CANDIDATE_JOHN, CANDIDATE_MARTA);

	public static final Election ELECTION = new Election(new Office("arbitraryOfiice"), 1, 1, ALL_CANDIDATES);

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
				createBallotStateFor(CANDIDATE_MARTA, CANDIDATE_PETER).withNextPreference()
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
				createBallotStateFor(CANDIDATE_MARTA, CANDIDATE_JOHN).withNextPreference().withVoteWeight(BigFraction.ONE_FIFTH),
				createBallotStateFor(),
				createBallotStateFor(CANDIDATE_MARTA, CANDIDATE_PETER).withNextPreference().withVoteWeight(BigFraction.ONE_THIRD)
			)
		);

		assertThat(votesByCandidate.size(), is(3));
		assertThat(votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ONE_THIRD),
			hasEntry(CANDIDATE_JOHN, new BigFraction(6,5)),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));
	}

	private BallotState createBallotStateFor(Candidate... candidates) {
		return new BallotState(new Ballot(ImmutableSet.of(new ElectionCandidatePreference(
			ELECTION, ImmutableSet.copyOf(candidates)))), ELECTION);
	}


}
