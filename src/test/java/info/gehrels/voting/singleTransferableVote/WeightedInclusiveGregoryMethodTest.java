/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

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
	private static final Candidate CANDIDATE_A = new Candidate("A");
	private static final Candidate CANDIDATE_B = new Candidate("B");
	private static final Candidate CANDIDATE_C = new Candidate("C");
	private static final Candidate CANDIDATE_D = new Candidate("D");

	private static final ImmutableSet<Candidate> CANDIDATES = ImmutableSet
		.of(CANDIDATE_A, CANDIDATE_B, CANDIDATE_C,
		    CANDIDATE_D);
	private static final Election<Candidate> ELECTION = new Election<>("Example Office",    CANDIDATES);
	private static final Ballot<Candidate> BALLOT_ABCD = createBallot("ABCD", ELECTION);
	private static final Ballot<Candidate> BALLOT_ACD = createBallot("ACD", ELECTION);
	private static final Ballot<Candidate> BALLOT_BCDA = createBallot("BCDA", ELECTION);
	private static final Ballot<Candidate> BALLOT_NO_VOTES = createBallot("", ELECTION);

	private static final ImmutableList<VoteState<Candidate>> VOTE_STATES_FIXTURE = ImmutableList.of(
		VoteState.forBallotAndElection(BALLOT_ABCD, ELECTION).get(),
		VoteState.forBallotAndElection(BALLOT_ACD, ELECTION).get().withVoteWeight(BigFraction.THREE_QUARTERS),
		VoteState.forBallotAndElection(BALLOT_BCDA, ELECTION).get(),
		VoteState.forBallotAndElection(BALLOT_NO_VOTES, ELECTION).get()
	);

	private final STVElectionCalculationListener<Candidate> listenerMock = mock(STVElectionCalculationListener.class);
	private final WeightedInclusiveGregoryMethod<Candidate> wigm = new WeightedInclusiveGregoryMethod<>(
		listenerMock);

	private static final CandidateStates<Candidate> ALL_HOPEFUL_CANDIDATE_STATE = new CandidateStates<>(CANDIDATES);

	@Test
	public void reducesVoteWeightOfThoseBallotsThatHadTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRecalculator<Candidate> voteWeightRecalculator = wigm.recalculatorFor();

		ImmutableCollection<VoteState<Candidate>> voteStates = voteWeightRecalculator
			.recalculateExceededVoteWeight(CANDIDATE_A, new BigFraction(7, 8), VOTE_STATES_FIXTURE,
                    ALL_HOPEFUL_CANDIDATE_STATE);

		assertThat(voteStates, hasItems(
			aVoteState(Matchers.allOf(
				withVoteWeight(ONE_HALF),
				withBallotId(BALLOT_ABCD.id)
			)),
			aVoteState(Matchers.allOf(
				withBallotId(BALLOT_ACD.id),
				withVoteWeight(new BigFraction(3, 8))
			))
		));
	}

	@Test
	public void reportsEachVoteWeightRedistribution() {
		VoteWeightRecalculator<Candidate> voteWeightRecalculator = wigm.recalculatorFor();

		voteWeightRecalculator
			.recalculateExceededVoteWeight(CANDIDATE_A, new BigFraction(7, 8), VOTE_STATES_FIXTURE,
                    ALL_HOPEFUL_CANDIDATE_STATE);

		verify(listenerMock).redistributingExcessiveFractionOfVoteWeight(CANDIDATE_A, ONE_HALF);
	}


	@Test
	public void doesNotReduceVoteWeightOfThoseVotesThatDidNotHaveTheElectedCandidateAsCurrentPreferredCandidate() {
		VoteWeightRecalculator<Candidate> voteWeightRecalculator = wigm.recalculatorFor();

		ImmutableCollection<VoteState<Candidate>> voteStates = voteWeightRecalculator
			.recalculateExceededVoteWeight(CANDIDATE_A, BigFraction.ONE, VOTE_STATES_FIXTURE,
                    ALL_HOPEFUL_CANDIDATE_STATE);

		assertThat(voteStates, hasItems(
			aVoteState(Matchers.allOf(
				withBallotId(BALLOT_BCDA.id),
				withVoteWeight(BigFraction.ONE))),
			aVoteState(Matchers.allOf(
				withBallotId(BALLOT_NO_VOTES.id),
				withVoteWeight(BigFraction.ONE)))
		));
	}


}
