/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
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
import java.util.Map;

import static info.gehrels.voting.singleTransferableVote.VotesByCandidateCalculation.calculateVotesByCandidate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

public final class VotesByCandidateCalculationTest {
	public static final ImmutableList<VoteState<Candidate>> EMPTY_VOTE_STATE_LIST = ImmutableList.of();
	public static final ImmutableSet<Candidate> EMPTY_CANDIDATE_SET = ImmutableSet.of();
	public static final Candidate CANDIDATE_PETER = new Candidate("Peter");
	public static final Candidate CANDIDATE_JOHN = new Candidate("John");
	public static final Candidate CANDIDATE_MARTA = new Candidate("Marta");


	public static final ImmutableSet<Candidate> ALL_CANDIDATES = ImmutableSet
		.of(CANDIDATE_PETER, CANDIDATE_JOHN, CANDIDATE_MARTA);

	public static final Election<Candidate> ELECTION = new Election<>("arbitraryOfiice", ALL_CANDIDATES);

	@Test
	public void returnsEmtpyMapIfCandidateSetAndVoteStatesAreEmpty() {
		Map<Candidate, BigFraction> votesByCandidate = calculateVotesByCandidate(EMPTY_CANDIDATE_SET,
		                                                                         EMPTY_VOTE_STATE_LIST);

		assertThat(votesByCandidate, is(equalTo(Collections.<Candidate, BigFraction>emptyMap())));
	}

	@Test
	public void returnsZeroVotesForCandidatesWhoAreNotMentionedByAnyVoteState() {
		Map<Candidate, BigFraction> votesByCandidate = calculateVotesByCandidate(ImmutableSet.of(CANDIDATE_PETER),
		                                                                         EMPTY_VOTE_STATE_LIST);

		assertThat(votesByCandidate.size(), is(1));
		assertThat(votesByCandidate, hasEntry(CANDIDATE_PETER, BigFraction.ZERO));
	}

	@Test
	public void returnsCorrectNumberOfVotesForMultipleCandidates() {
		Map<Candidate, BigFraction> votesByCandidate =
			calculateVotesByCandidate(ALL_CANDIDATES, ImmutableList.of(
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA),
				createVoteStateFor(1, CANDIDATE_MARTA, CANDIDATE_JOHN),
				createVoteStateFor(2),
				createVoteStateFor(3, CANDIDATE_PETER, CANDIDATE_MARTA)
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
				createVoteStateFor(0, CANDIDATE_JOHN, CANDIDATE_MARTA),
				createVoteStateFor(1, CANDIDATE_JOHN, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_FIFTH),
				createVoteStateFor(2),
				createVoteStateFor(3, CANDIDATE_PETER, CANDIDATE_MARTA).withVoteWeight(BigFraction.ONE_THIRD)
			)
			);

		assertThat(votesByCandidate.size(), is(3));
		assertThat(votesByCandidate, allOf(
			hasEntry(CANDIDATE_PETER, BigFraction.ONE_THIRD),
			hasEntry(CANDIDATE_JOHN, new BigFraction(6, 5)),
			hasEntry(CANDIDATE_MARTA, BigFraction.ZERO)
		));
	}

	private VoteState<Candidate> createVoteStateFor(int id, Candidate... candidates) {
		Vote<Candidate> preferenceVote;
		if (candidates.length == 0) {
			preferenceVote = Vote.createNoVote(ELECTION);
		} else {
			preferenceVote = Vote.createPreferenceVote(ELECTION, ImmutableSet.copyOf(candidates));
		}

		return VoteState.forBallotAndElection(new Ballot<>(id, ImmutableSet.of(preferenceVote)), ELECTION).get();
	}


}
