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
