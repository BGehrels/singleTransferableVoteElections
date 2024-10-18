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

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.TestUtils;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.jupiter.api.Test;

import static info.gehrels.voting.OptionalMatchers.anEmptyOptional;
import static info.gehrels.voting.OptionalMatchers.anOptionalWhoseValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public final class VoteStateTest {
	private static final Candidate CANDIDATE_A = new Candidate("A");
	private static final Candidate CANDIDATE_B = new Candidate("B");
	private static final Election<Candidate> ELECTION = new Election<>("Example Office", ImmutableSet.of(CANDIDATE_A,
	                                                                                                    CANDIDATE_B));

	@Test
	public void returnsNewVoteStateWithSameValuesButUpdatedVoteWeight() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> voteState = VoteState.forBallotAndElection(ballot, ELECTION).get();

		VoteState<Candidate> newVoteState = voteState.withVoteWeight(BigFraction.ONE_THIRD);

		assertThat(newVoteState, is(not(sameInstance(voteState))));
		assertThat(newVoteState.getPreferredCandidate(), is(equalTo(voteState.getPreferredCandidate())));
		assertThat(newVoteState.isInvalid(), is(false));
		assertThat(newVoteState.isNoVote(), is(false));
		assertThat(newVoteState.getVoteWeight(), is(not(equalTo(voteState.getVoteWeight()))));
		assertThat(newVoteState.getVoteWeight(), is(equalTo(BigFraction.ONE_THIRD)));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithAbsentPreferenceIfTheBallotContainedANoVoteForThisElection() {
		Ballot<Candidate> ballot = TestUtils.createNoBallot(ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(CANDIDATE_A)));
		assertThat(resultingVoteState.getPreferredCandidate(), is(anEmptyOptional()));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNoIfTheBallotContainedANoVoteForThisElection() {
		Ballot<Candidate> ballot = TestUtils.createNoBallot(ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(CANDIDATE_A)));
		assertThat(resultingVoteState.isNoVote(), is(true));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNullPreferenceIfNoCandidatesAreInTheCandidateStateMap() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.<Candidate>of()));
		assertThat(resultingVoteState.getPreferredCandidate(), is(anEmptyOptional()));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNoVoteIfNoCandidatesAreInTheCandidateStateMap() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.<Candidate>of()));
		assertThat(resultingVoteState.isNoVote(), is(true));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNullPreferenceIfRemainingMarkedCandidatesAreNotHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(
				new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)).withElected(CANDIDATE_A).withLoser(
                        CANDIDATE_B));
		assertThat(resultingVoteState.getPreferredCandidate(), is(anEmptyOptional()));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNoVoteIfRemainingMarkedCandidatesAreNotHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(
				new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)).withElected(CANDIDATE_A).withLoser(
                        CANDIDATE_B));
		assertThat(resultingVoteState.isNoVote(), is(true));
	}

	@Test
	public void withFirstHopefulCandidateReturnsSameStateIfCurrentCandidateIsStillHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)));
		assertThat(resultingVoteState.getPreferredCandidate(), is(anOptionalWhoseValue(is(CANDIDATE_A))));
	}

	@Test
	public void withFirstHopefulCandidateReturnsNonNoVoteIfCurrentCandidateIsStillHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)));
		assertThat(resultingVoteState.isNoVote(), is(false));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNextHopefulPreferenceIfCurrentPreferenceIsNotHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(
				new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)).withLoser(CANDIDATE_A));
		assertThat(resultingVoteState.getPreferredCandidate(), is(anOptionalWhoseValue(is(CANDIDATE_B))));
	}

	@Test
	public void withFirstHopefulCandidateReturnsStateWithNextHopefulNonNoVoteIfCurrentPreferenceIsNotHopeful() {
		Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(
				new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)).withLoser(CANDIDATE_A));
		assertThat(resultingVoteState.isNoVote(), is(false));
	}

	@Test
	public void isInvalidIfVoteIsInvalid() {
	    Ballot<Candidate> ballot = TestUtils.createInvalidBallot(ELECTION);
		VoteState<Candidate> candidateVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get();
		assertThat(candidateVoteState.isInvalid(), is(true));
	}

	@Test
	public void isValidIfVoteIsNo() {
	    Ballot<Candidate> ballot = TestUtils.createNoBallot(ELECTION);
		VoteState<Candidate> candidateVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get();
		assertThat(candidateVoteState.isInvalid(), is(false));
	}

	@Test
	public void isValidIfVoteWithPreference() {
	    Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> candidateVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get();
		assertThat(candidateVoteState.isInvalid(), is(false));
	}

	@Test
	public void isValidIfVoteWithPreferenceIsDepleted() {
	    Ballot<Candidate> ballot = TestUtils.createBallot("AB", ELECTION);
		VoteState<Candidate> resultingVoteState = VoteState.forBallotAndElection(ballot, ELECTION).get()
			.withFirstHopefulCandidate(
				new CandidateStates<>(ImmutableSet.of(CANDIDATE_A, CANDIDATE_B)).withElected(CANDIDATE_A).withLoser(
                        CANDIDATE_B));
		assertThat(resultingVoteState.isInvalid(), is(false));
	}



}
