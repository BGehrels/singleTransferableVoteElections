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

import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.aVoteDistribution;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.withInvalidVotes;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.withNoVotes;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.withVotesForCandidate;
import static org.apache.commons.math3.fraction.BigFraction.ONE;
import static org.apache.commons.math3.fraction.BigFraction.ONE_THIRD;
import static org.apache.commons.math3.fraction.BigFraction.TWO;
import static org.apache.commons.math3.fraction.BigFraction.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class VoteDistributionTest {
	private static final ImmutableList<VoteState<Candidate>> EMPTY_VOTE_STATE_LIST = ImmutableList.of();
	private static final ImmutableSet<Candidate> EMPTY_CANDIDATE_SET = ImmutableSet.of();
	private static final Candidate CANDIDATE_PETER = new Candidate("Peter");
	private static final Candidate CANDIDATE_JOHN = new Candidate("John");
	private static final Candidate CANDIDATE_MARTA = new Candidate("Marta");


	private static final ImmutableSet<Candidate> ALL_CANDIDATES = ImmutableSet
		.of(CANDIDATE_PETER, CANDIDATE_JOHN, CANDIDATE_MARTA);

	private static final Election<Candidate> ELECTION = new Election<>("arbitraryOffice", ALL_CANDIDATES);
	private static final BigFraction SIX_FIFTHS = new BigFraction(6, 5);

	@Test
	public void returnsEmptyVotesByCandidateMapIfCandidateSetAndVoteStatesAreEmpty() {
		VoteDistribution<Candidate> voteDistribution = new VoteDistribution<>(EMPTY_CANDIDATE_SET,
		                                                                      EMPTY_VOTE_STATE_LIST);

		assertThat(voteDistribution.votesByCandidate, is(equalTo(Collections.<Candidate, BigFraction>emptyMap())));
	}

	@Test
	public void returnsZeroVotesForCandidatesWhoAreNotMentionedByAnyVoteState() {
		VoteDistribution<Candidate> voteDistribution = new VoteDistribution<>(ImmutableSet.of(CANDIDATE_PETER),
		                                                                      EMPTY_VOTE_STATE_LIST);

		assertThat(voteDistribution, is(aVoteDistribution(withVotesForCandidate(CANDIDATE_PETER, ZERO),
		                                                  withNoVotes(ZERO),
		                                                  withInvalidVotes(ZERO))));
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
			));

		assertThat(voteDistribution, is(aVoteDistribution(withVotesForCandidate(CANDIDATE_PETER, ONE),
		                                                  withVotesForCandidate(CANDIDATE_JOHN, ONE),
		                                                  withVotesForCandidate(CANDIDATE_MARTA, ONE),
		                                                  withNoVotes(TWO),
		                                                  withInvalidVotes(ONE))));
	}

	@Test
	public void returnsCorrectNumberOfVotesForMultipleCandidatesAndFractionalVoteWeights() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA),
				createVoteStateFor(1, CANDIDATE_JOHN, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_FIFTH),
				createVoteStateFor(3, CANDIDATE_PETER, CANDIDATE_MARTA).withVoteWeight(ONE_THIRD)
			));

		assertThat(voteDistribution, is(aVoteDistribution(withVotesForCandidate(CANDIDATE_PETER, ONE_THIRD),
		                                                  withVotesForCandidate(CANDIDATE_JOHN, SIX_FIFTHS),
		                                                  withVotesForCandidate(CANDIDATE_MARTA, ZERO)
		)));
	}

	@Test
	public void countsInvalidVoteAsInvalid() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createInvalidVoteState(0)
			));

		assertThat(voteDistribution, is(aVoteDistribution(withVotesForCandidate(CANDIDATE_PETER, ZERO),
		                                                  withVotesForCandidate(CANDIDATE_JOHN, ZERO),
		                                                  withVotesForCandidate(CANDIDATE_MARTA, ZERO),
		                                                  withInvalidVotes(ONE),
		                                                  withNoVotes(ZERO)
		)));
	}

	@Test
	public void countsNoVoteAsNo() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createNoVoteState(0)
			));

		assertThat(voteDistribution, is(aVoteDistribution(withVotesForCandidate(CANDIDATE_PETER, ZERO),
		                                                  withVotesForCandidate(CANDIDATE_JOHN, ZERO),
		                                                  withVotesForCandidate(CANDIDATE_MARTA, ZERO),
		                                                  withInvalidVotes(ZERO),
		                                                  withNoVotes(ONE)
		)));
	}

	@Test
	public void countsVoteForTheFirstHopefulCandidateInThePreference() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA).withFirstHopefulCandidate(
					new CandidateStates<>(ALL_CANDIDATES).withElected(CANDIDATE_JOHN))
			)
			);

		assertThat(voteDistribution, is(aVoteDistribution(
			withVotesForCandidate(CANDIDATE_PETER, ZERO),
			withVotesForCandidate(CANDIDATE_JOHN, ZERO),
			withVotesForCandidate(CANDIDATE_MARTA, ONE),
			withInvalidVotes(ZERO),
			withNoVotes(ZERO))));
	}

	@Test
	public void countsVoteAsNoIfPreferenceIsDepleted() {
		VoteDistribution<Candidate> voteDistribution =
			new VoteDistribution<>(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA).withFirstHopefulCandidate(
					new CandidateStates<>(ALL_CANDIDATES).withElected(CANDIDATE_JOHN).withLoser(CANDIDATE_MARTA))
			));

		assertThat(voteDistribution,
		           is(aVoteDistribution(withVotesForCandidate(CANDIDATE_PETER, ZERO),
		                                withVotesForCandidate(CANDIDATE_JOHN, ZERO),
		                                withVotesForCandidate(CANDIDATE_MARTA, ZERO),
		                                withNoVotes(ONE),
		                                withInvalidVotes(ZERO)
		           )));
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
