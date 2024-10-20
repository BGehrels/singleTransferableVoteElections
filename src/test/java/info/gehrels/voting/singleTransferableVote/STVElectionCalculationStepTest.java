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
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.TestUtils;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;
import org.mockito.hamcrest.MockitoHamcrest;

import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.withElectedCandidate;
import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.withLooser;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.anElectionStepResult;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.withCandidateStates;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.withNumberOfElectedCandidates;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.withVoteStates;
import static info.gehrels.voting.singleTransferableVote.STVElectionCalculationStep.ElectionStepResult;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.aVoteDistribution;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.withInvalidVotes;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.withNoVotes;
import static info.gehrels.voting.singleTransferableVote.VoteDistributionMatchers.withVotesForCandidate;
import static info.gehrels.voting.singleTransferableVote.VoteStateMatchers.aVoteState;
import static info.gehrels.voting.singleTransferableVote.VoteStateMatchers.withPreferredCandidate;
import static info.gehrels.voting.singleTransferableVote.VoteStateMatchers.withVoteWeight;
import static org.apache.commons.math3.fraction.BigFraction.FOUR_FIFTHS;
import static org.apache.commons.math3.fraction.BigFraction.ONE;
import static org.apache.commons.math3.fraction.BigFraction.ONE_FIFTH;
import static org.apache.commons.math3.fraction.BigFraction.ONE_HALF;
import static org.apache.commons.math3.fraction.BigFraction.TWO;
import static org.apache.commons.math3.fraction.BigFraction.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public final class STVElectionCalculationStepTest {
	private static final Candidate A = new Candidate("A");
	private static final Candidate B = new Candidate("B");
	private static final Candidate C = new Candidate("C");
	private static final Candidate G = new Candidate("G");
	private static final Candidate H = new Candidate("H");

	private static final Election<Candidate> ELECTION = new Election<>("arbitraryOffice",
	                                                                  ImmutableSet.of(A, B, C, G, H));

	private static final Ballot<Candidate> BC_BALLOT = createBallot("BC");
	private static final Ballot<Candidate> ACGH_BALLOT = createBallot("ACGH");
	private static final Ballot<Candidate> A_BALLOT = createBallot("A");
	private static final Ballot<Candidate> AC_BALLOT = createBallot("AC");
	private static final Ballot<Candidate> BA_BALLOT = createBallot("BA");
	private static final Ballot<Candidate> CA_BALLOT = createBallot("CA");

	private static final BigFraction THREE = new BigFraction(3);
	private static final ImmutableList<VoteState<Candidate>> STUB_REDISTRIBUTION_RESULT = ImmutableList.of(
		stateFor(AC_BALLOT).withVoteWeight(FOUR_FIFTHS),
		stateFor(A_BALLOT).withVoteWeight(ONE),
		stateFor(ACGH_BALLOT).withVoteWeight(ONE_FIFTH),
		stateFor(BC_BALLOT).withVoteWeight(ONE_HALF)
	);
	private static final BigFraction FIVE = new BigFraction(5);

	private final STVElectionCalculationListener<Candidate> electionCalculationListenerMock = mock(
		STVElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolverMock = mock(AmbiguityResolver.class);
	private final VoteWeightRecalculator<Candidate> redistributorMock = mock(VoteWeightRecalculator.class);

	private final STVElectionCalculationStep<Candidate> step = new STVElectionCalculationStep<>(
		electionCalculationListenerMock, ambiguityResolverMock);
	private static final CandidateStates<Candidate> CANDIDATE_STATES = new CandidateStates<>(ImmutableSet.of(A, B, C));

	@Test
	public void marksOneCandidateAsWinnerAndRedistributesVoteWeightIfExactlyOneCandidateReachedTheQuorum() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);

		when(redistributorMock.recalculateExceededVoteWeight(A, THREE, voteStates, CANDIDATE_STATES))
			.thenReturn(STUB_REDISTRIBUTION_RESULT);

		ElectionStepResult<Candidate> electionStepResult =
			step.declareWinnerOrStrikeCandidate(THREE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		Matcher<Iterable<? extends VoteState<Candidate>>> newVoteStatesMatcher = containsInAnyOrder(
			aVoteState(allOf(
				withPreferredCandidate(C),
				withVoteWeight(FOUR_FIFTHS)
			)),
			aVoteState(withPreferredCandidate(null)),
			aVoteState(allOf(
				withPreferredCandidate(C),
				withVoteWeight(ONE_FIFTH)
			)),
			aVoteState(allOf(
				withPreferredCandidate(B),
				withVoteWeight(ONE_HALF)
			))
		);
		verify(electionCalculationListenerMock).candidateIsElected(A, THREE, THREE);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted(eq(voteStates),
		                                                                          (ImmutableCollection<VoteState<Candidate>>) argThat(
			                                                                          newVoteStatesMatcher),
		                                                                          argThat(is(aVoteDistribution(
			                                                                          withVotesForCandidate(B,
			                                                                                                ONE_HALF),
			                                                                          withVotesForCandidate(C, ONE),
			                                                                          withNoVotes(ONE),
			                                                                          withInvalidVotes(ZERO)
		                                                                          ))));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(A)),
			withNumberOfElectedCandidates(is(2L)),
			withVoteStates(newVoteStatesMatcher)
		))));
	}

	@Test
	public void marksAllSuchCandidatesWinnerAndRedistributesVoteWeightIfMoreThanOneCandidateReachedTheQuorum() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);

		when(redistributorMock.recalculateExceededVoteWeight(A, ONE, voteStates, CANDIDATE_STATES))
			.thenReturn(STUB_REDISTRIBUTION_RESULT);
		when(redistributorMock.recalculateExceededVoteWeight(B, ONE, STUB_REDISTRIBUTION_RESULT, CANDIDATE_STATES))
			.thenReturn(STUB_REDISTRIBUTION_RESULT);

		ElectionStepResult<Candidate> electionStepResult = step
			.declareWinnerOrStrikeCandidate(ONE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		Matcher newVoteStateMatcher = containsInAnyOrder(
			aVoteState(allOf(
				withPreferredCandidate(C),
				withVoteWeight(FOUR_FIFTHS)
			)),
			aVoteState(withPreferredCandidate(null)),
			aVoteState(allOf(
				withPreferredCandidate(C),
				withVoteWeight(ONE_FIFTH)
			)),
			aVoteState(allOf(
				withPreferredCandidate(C),
				withVoteWeight(ONE_HALF)
			))
		);
		verify(electionCalculationListenerMock).candidateIsElected(A, THREE, ONE);
		verify(electionCalculationListenerMock).candidateIsElected(B, ONE, ONE);
		verify(electionCalculationListenerMock)
			.voteWeightRedistributionCompleted(eq(voteStates), (ImmutableCollection) argThat(newVoteStateMatcher),
			                                   argThat(is(aVoteDistribution(withVotesForCandidate(C, new BigFraction(3,2)),
			                                                                withNoVotes(ONE),
			                                                                withInvalidVotes(ZERO)))));

		Matcher subMatcher = allOf(
				withCandidateStates(allOf(withElectedCandidate(A), withElectedCandidate(B))),
				withNumberOfElectedCandidates(is(3L)),
				withVoteStates(newVoteStateMatcher)
		);
		assertThat(electionStepResult, is(anElectionStepResult(subMatcher)));
	}

	@Test
	public void strikesWeakestCandidateIfNoCandidateReachedTheQuorum() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);

		ElectionStepResult<Candidate> electionStepResult = step
			.declareWinnerOrStrikeCandidate(FIVE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		verify(electionCalculationListenerMock).nobodyReachedTheQuorumYet(FIVE);

		verify(electionCalculationListenerMock).candidateDropped(
			argThat(
				is(aVoteDistribution(withVotesForCandidate(A, THREE), withVotesForCandidate(B, ONE),
				                     withVotesForCandidate(C, ZERO)))),
			eq(C)
		);

		Matcher<Iterable<? extends VoteState<Candidate>>> newVoteStatesMatcher = containsInAnyOrder(
			aVoteState(allOf(
				withPreferredCandidate(A),
				withVoteWeight(ONE)
			)),
			aVoteState(allOf(
				withPreferredCandidate(A),
				withVoteWeight(ONE)
			)),
			aVoteState(allOf(
				withPreferredCandidate(A),
				withVoteWeight(ONE)
			)),
			aVoteState(allOf(
				withPreferredCandidate(B),
				withVoteWeight(ONE)
			))
		);
		verify(electionCalculationListenerMock)
			.voteWeightRedistributionCompleted(eq(voteStates),
			                                   MockitoHamcrest.<ImmutableCollection>argThat((Matcher) newVoteStatesMatcher),
			                                   argThat(is(aVoteDistribution(
				                                   withVotesForCandidate(A, THREE),
				                                   withVotesForCandidate(B, ONE),
				                                   withNoVotes(ZERO),
				                                   withInvalidVotes(ZERO)))));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withLooser(C)),
			withNumberOfElectedCandidates(is(1L)),
			withVoteStates(newVoteStatesMatcher)
		))));
	}


	@Test
	public void strikesCandidateResultingFromAmbiguityResolutionIfNoCandidateReachedTheQuorumAndThereAreMoreThanOneWeakestCandidate() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(BA_BALLOT),
			stateFor(CA_BALLOT)
		);

		when(ambiguityResolverMock.chooseOneOfMany(ImmutableSet.of(B, C)))
			.thenReturn(new AmbiguityResolverResult<>(B, "fixed as stub"));

		ElectionStepResult<Candidate> electionStepResult = step
			.declareWinnerOrStrikeCandidate(THREE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		verify(electionCalculationListenerMock).nobodyReachedTheQuorumYet(THREE);

		verify(electionCalculationListenerMock).candidateDropped(argThat(
			is(aVoteDistribution(withVotesForCandidate(A, TWO), withVotesForCandidate(B, ONE),
			                     withVotesForCandidate(C, ONE), withNoVotes(ZERO), withInvalidVotes(ZERO)))), eq(B));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withLooser(B)),
			withNumberOfElectedCandidates(is(1L)),
			withVoteStates(containsInAnyOrder(
				aVoteState(allOf(
					withPreferredCandidate(A),
					withVoteWeight(ONE)
				)),
				aVoteState(allOf(
					withPreferredCandidate(A),
					withVoteWeight(ONE)
				)),
				aVoteState(allOf(
					withPreferredCandidate(A),
					withVoteWeight(ONE)
				)),
				aVoteState(allOf(
					withPreferredCandidate(C),
					withVoteWeight(ONE)
				))
			))
		))));
	}


	private static VoteState<Candidate> stateFor(Ballot<Candidate> ballot) {
		return VoteState.forBallotAndElection(ballot, ELECTION).get();
	}

	private static Ballot<Candidate> createBallot(String preference) {
		return TestUtils.createBallot(preference, ELECTION);
	}

}
