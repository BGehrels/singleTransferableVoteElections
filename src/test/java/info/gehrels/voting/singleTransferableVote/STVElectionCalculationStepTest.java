package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.MapMatchers;
import info.gehrels.voting.TestUtils;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import java.util.Map;

import static info.gehrels.voting.MapMatchers.aMap;
import static info.gehrels.voting.MapMatchers.anEntry;
import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.withElectedCandidate;
import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.withLooser;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.anElectionStepResult;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.withCandidateStates;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.withNumberOfElectedCandidates;
import static info.gehrels.voting.singleTransferableVote.ElectionStepResultMatchers.withVoteStates;
import static info.gehrels.voting.singleTransferableVote.STVElectionCalculationStep.ElectionStepResult;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class STVElectionCalculationStepTest {
	public static final Candidate A = new Candidate("A");
	public static final Candidate B = new Candidate("B");
	public static final Candidate C = new Candidate("C");
	public static final Candidate G = new Candidate("G");
	public static final Candidate H = new Candidate("H");

	public static final Election<Candidate> ELECTION = new Election<>("arbitraryOffice",
	                                                                  ImmutableSet.of(A, B, C, G, H));

	public static final Ballot<Candidate> BC_BALLOT = createBallot("BC");
	public static final Ballot<Candidate> ACGH_BALLOT = createBallot("ACGH");
	public static final Ballot<Candidate> A_BALLOT = createBallot("A");
	public static final Ballot<Candidate> AC_BALLOT = createBallot("AC");
	public static final Ballot<Candidate> BA_BALLOT = createBallot("BA");
	private static final Ballot<Candidate> CA_BALLOT = createBallot("CA");

	public static final BigFraction THREE = new BigFraction(3);
	private static final ImmutableList<VoteState<Candidate>> STUB_REDISTRIBUTION_RESULT = ImmutableList.of(
		stateFor(AC_BALLOT).withVoteWeight(FOUR_FIFTHS),
		stateFor(A_BALLOT).withVoteWeight(ONE),
		stateFor(ACGH_BALLOT).withVoteWeight(ONE_FIFTH),
		stateFor(BC_BALLOT).withVoteWeight(ONE_HALF)
	);
	public static final BigFraction FIVE = new BigFraction(5);

	private final STVElectionCalculationListener<Candidate> electionCalculationListenerMock = mock(
		STVElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolverMock = mock(AmbiguityResolver.class);
	private final VoteWeightRedistributor<Candidate> redistributorMock = mock(VoteWeightRedistributor.class);

	private final STVElectionCalculationStep<Candidate> step = new STVElectionCalculationStep<>(
		electionCalculationListenerMock, ambiguityResolverMock);
	public static final CandidateStates<Candidate> CANDIDATE_STATES = new CandidateStates<>(ImmutableSet.of(A, B, C));

	@Test
	public void marksOneCandidateAsWinnerAndRedistributesVoteWeightIfExactlyOneCandidateReachedTheQuorum() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);

		when(redistributorMock.redistributeExceededVoteWeight(A, THREE, voteStates, CANDIDATE_STATES))
			.thenReturn(STUB_REDISTRIBUTION_RESULT);

		ElectionStepResult<Candidate> electionStepResult =
			step.declareWinnerOrStrikeCandidate(THREE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		verify(electionCalculationListenerMock).candidateIsElected(A, THREE, THREE);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted((Map<Candidate, BigFraction>) argThat(
			allOf(
				hasEntry(B, ONE_HALF),
				hasEntry(C, ONE))
		));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(A)),
			withNumberOfElectedCandidates(is(2L)),
			withVoteStates(containsInAnyOrder(
				aVoteState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(FOUR_FIFTHS))
				)),
				aVoteState(VoteStateMatchers.<Candidate>withPreferredCandidate(nullValue())),
				aVoteState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE_FIFTH))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(B)),
					withVoteWeight(equalTo(ONE_HALF))
				))
			))
		))));
	}

	@Test
	public void marksCandidateWithHigherNumberOfVotesAsWinnerAndRedistributesVoteWeightIfMoreThanOneCandidateReachedTheQuorum() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);

		when(redistributorMock.redistributeExceededVoteWeight(A, ONE, voteStates, CANDIDATE_STATES))
			.thenReturn(STUB_REDISTRIBUTION_RESULT);

		ElectionStepResult<Candidate> electionStepResult = step
			.declareWinnerOrStrikeCandidate(ONE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		verify(electionCalculationListenerMock).candidateIsElected(A, THREE, ONE);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted((Map<Candidate, BigFraction>) argThat(
			allOf(
				hasEntry(B, ONE_HALF),
				hasEntry(C, ONE))
		));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(A)),
			withNumberOfElectedCandidates(is(2L)),
			withVoteStates(containsInAnyOrder(
				aVoteState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(FOUR_FIFTHS))
				)),
				aVoteState(VoteStateMatchers.<Candidate>withPreferredCandidate(nullValue())),
				aVoteState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE_FIFTH))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(B)),
					withVoteWeight(equalTo(ONE_HALF))
				))
			))
		))));
	}

	@Test
	public void marksCandidateResultingFromAbiguityResulutionAsWinnerAndRedistributesVoteWeightIfMoreThanOneCandidateWithEqualNumberOfVotesReachedWheQuorum() {
		ImmutableList<VoteState<Candidate>> voteStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(BA_BALLOT),
			stateFor(BC_BALLOT)
		);

		when(ambiguityResolverMock.chooseOneOfMany(ImmutableSet.of(A, B)))
			.thenReturn(new AmbiguityResolverResult<>(B, "Fixed as Mock"));

		when(redistributorMock.redistributeExceededVoteWeight(B, TWO, voteStates, CANDIDATE_STATES))
			.thenReturn(STUB_REDISTRIBUTION_RESULT);

		ElectionStepResult<Candidate> electionStepResult = step
			.declareWinnerOrStrikeCandidate(TWO, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		verify(electionCalculationListenerMock).candidateIsElected(B, TWO, TWO);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted((Map<Candidate, BigFraction>) argThat(
			allOf(
				hasEntry(A, TWO),
				hasEntry(C, ONE_HALF))
		));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(B)),
			withNumberOfElectedCandidates(is(2L)),
			withVoteStates(containsInAnyOrder(
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(FOUR_FIFTHS))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE_FIFTH))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE_HALF))
				))
			))
		))));
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
			argThat(is(aMap(MapMatchers.<Candidate, BigFraction>withEntries(
				containsInAnyOrder(
					anEntry(A, THREE),
					anEntry(B, ONE),
					anEntry(C, ZERO)
				))))),
			eq(C),
			eq(ZERO),
			argThat(is(aMap(MapMatchers.<Candidate, BigFraction>withEntries(
				containsInAnyOrder(
					anEntry(A, THREE),
					anEntry(B, ONE)
				))))));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withLooser(C)),
			withNumberOfElectedCandidates(is(1L)),
			withVoteStates(containsInAnyOrder(
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(B)),
					withVoteWeight(equalTo(ONE))
				))
			))
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
			.thenReturn(new AmbiguityResolverResult<>(B, "arbitrary message"));

		ElectionStepResult<Candidate> electionStepResult = step
			.declareWinnerOrStrikeCandidate(THREE, voteStates, redistributorMock, 1, CANDIDATE_STATES);

		verify(electionCalculationListenerMock).nobodyReachedTheQuorumYet(THREE);

		verify(electionCalculationListenerMock).candidateDropped(
			argThat(is(aMap(MapMatchers.<Candidate, BigFraction>withEntries(
				containsInAnyOrder(
					anEntry(A, TWO),
					anEntry(B, ONE),
					anEntry(C, ONE)
				))))),
			eq(B),
			eq(ONE),
			argThat(is(aMap(MapMatchers.<Candidate, BigFraction>withEntries(
				containsInAnyOrder(
					anEntry(A, THREE),
					anEntry(C, ONE)
				))))));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withLooser(B)),
			withNumberOfElectedCandidates(is(1L)),
			withVoteStates(containsInAnyOrder(
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aVoteState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE))
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
