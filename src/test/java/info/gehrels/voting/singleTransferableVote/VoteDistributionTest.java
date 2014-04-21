package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.Vote;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public class VoteDistributionTest {
	public static final ImmutableList<VoteState<Candidate>> EMPTY_VOTE_STATE_LIST = ImmutableList.of();
	public static final ImmutableSet<Candidate> EMPTY_CANDIDATE_SET = ImmutableSet.of();
	public static final Candidate CANDIDATE_PETER = new Candidate("Peter");
	public static final Candidate CANDIDATE_JOHN = new Candidate("John");
	public static final Candidate CANDIDATE_MARTA = new Candidate("Marta");


	public static final ImmutableSet<Candidate> ALL_CANDIDATES = ImmutableSet
		.of(CANDIDATE_PETER, CANDIDATE_JOHN, CANDIDATE_MARTA);

	public static final Election<Candidate> ELECTION = new Election<>("arbitraryOffice", ALL_CANDIDATES);

	@Test
	public void returnsEmtpyVotesByCandidateMapIfCandidateSetAndVoteStatesAreEmpty() {
		VoteDistribution<Candidate> voteDistribution = new VoteDistribution<>(EMPTY_CANDIDATE_SET,
		                                                                         EMPTY_VOTE_STATE_LIST);

		assertThat(voteDistribution.votesByCandidate, is(equalTo(Collections.<Candidate, BigFraction>emptyMap())));
	}

	@Test
	public void returnsZeroVotesForCandidatesWhoAreNotMentionedByAnyVoteState() {
		VoteDistribution<Candidate> voteDistribution = new VoteDistribution<>(ImmutableSet.of(CANDIDATE_PETER),
		                                                                         EMPTY_VOTE_STATE_LIST);

		assertThat(voteDistribution.votesByCandidate.size(), is(1));
		assertThat(voteDistribution.votesByCandidate, hasEntry(CANDIDATE_PETER, BigFraction.ZERO));
		assertThat(voteDistribution.noVotes, is(equalTo(BigFraction.ZERO)));
		assertThat(voteDistribution.invalidVotes, is(equalTo(BigFraction.ZERO)));
	}

	@Test
	public void returnsCorrectNumberOfVotesForMultipleCandidates() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA),
				createVoteStateFor(1, CANDIDATE_MARTA, CANDIDATE_JOHN),
				createNoVoteState(2),
				createVoteStateFor(3, CANDIDATE_PETER, CANDIDATE_MARTA),
				createNoVoteState(4),
				createInvalidVoteState(5)
			)
			);

		assertThat(voteDistribution.votesByCandidate.size(), is(3));
		assertThat(voteDistribution.votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ONE),
			hasEntry(CANDIDATE_JOHN, BigFraction.ONE),
			hasEntry(CANDIDATE_MARTA, BigFraction.ONE)
		));
		assertThat(voteDistribution.noVotes, is(equalTo(BigFraction.TWO)));
		assertThat(voteDistribution.invalidVotes, is(equalTo(BigFraction.ONE)));
	}

	@Test
	public void returnsCorrectNumberOfVotesForMultipleCandidatesAndFractionalVoteWeights() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA),
				createVoteStateFor(1, CANDIDATE_JOHN, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_FIFTH),
				createVoteStateFor(3, CANDIDATE_PETER, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_THIRD)
			)
			);

		assertThat(voteDistribution.votesByCandidate.size(), is(3));
		assertThat(voteDistribution.votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ONE_THIRD),
			hasEntry(CANDIDATE_JOHN, new BigFraction(6, 5)),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));
	}

	@Test
	public void countsInvalidVoteAsInvalid() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createInvalidVoteState(0)
			)
			);

		assertThat(voteDistribution.votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ZERO),
			hasEntry(CANDIDATE_JOHN, BigFraction.ZERO),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));

		assertThat(voteDistribution.invalidVotes, is(equalTo(BigFraction.ONE)));
		assertThat(voteDistribution.noVotes, is(equalTo(BigFraction.ZERO)));
	}

	@Test
	public void countsNoVoteAsNo() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createNoVoteState(0)
			)
			);

		assertThat(voteDistribution.votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ZERO),
			hasEntry(CANDIDATE_JOHN, BigFraction.ZERO),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));

		assertThat(voteDistribution.invalidVotes, is(equalTo(BigFraction.ZERO)));
		assertThat(voteDistribution.noVotes, is(equalTo(BigFraction.ONE)));
	}

	@Test
	public void countsVoteForTheFirstHopefulCandidateInThePreference() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA).withFirstHopefulCandidate(new CandidateStates<>(ALL_CANDIDATES).withElected(CANDIDATE_JOHN))
			)
			);

		assertThat(voteDistribution.votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ZERO),
			hasEntry(CANDIDATE_JOHN, BigFraction.ZERO),
			hasEntry(CANDIDATE_MARTA, BigFraction.ONE)
		));

		assertThat(voteDistribution.invalidVotes, is(equalTo(BigFraction.ZERO)));
		assertThat(voteDistribution.noVotes, is(equalTo(BigFraction.ZERO)));
	}

	@Test
	public void countsVoteAsNoIfPreferenceIsDepleted() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA).withFirstHopefulCandidate(new CandidateStates<>(ALL_CANDIDATES).withElected(CANDIDATE_JOHN).withLooser(CANDIDATE_MARTA))
			)
			);

		assertThat(voteDistribution.votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ZERO),
			hasEntry(CANDIDATE_JOHN, BigFraction.ZERO),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));

		assertThat(voteDistribution.invalidVotes, is(equalTo(BigFraction.ZERO)));
		assertThat(voteDistribution.noVotes, is(equalTo(BigFraction.ONE)));
	}

	private VoteState<Candidate> createVoteStateFor(int id, Candidate... candidates) {
		Vote<Candidate> preferenceVote = Vote.createPreferenceVote(ELECTION, ImmutableSet.copyOf(candidates));
		return VoteState.forBallotAndElection(new Ballot<>(id, ImmutableSet.of(preferenceVote)), ELECTION).get();
	}

	private VoteState<Candidate> createInvalidVoteState(int id) {
		Vote<Candidate> preferenceVote = Vote.createInvalidVote(ELECTION);

		return VoteState.forBallotAndElection(new Ballot<>(id, ImmutableSet.of(preferenceVote)), ELECTION).get();
	}

	private VoteState<Candidate> createNoVoteState(int id) {
		Vote<Candidate> preferenceVote = Vote.createNoVote(ELECTION);

		return VoteState.forBallotAndElection(new Ballot<>(id, ImmutableSet.of(preferenceVote)), ELECTION).get();
	}


}
