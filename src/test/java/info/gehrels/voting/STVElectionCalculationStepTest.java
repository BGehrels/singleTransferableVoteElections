package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.STVElectionCalculationStep.ElectionStepResult;
import info.gehrels.voting.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

public class STVElectionCalculationStepTest {
	public static final Candidate A = new Candidate("A", true);
	public static final Candidate B = new Candidate("B", true);
	public static final Candidate C = new Candidate("C", true);
	public static final Candidate G = new Candidate("G", true);
	public static final Candidate H = new Candidate("H", true);

	public static final Office OFFICE = new Office("arbitraryOffice");
	public static final Election ELECTION = new Election(OFFICE, 1, 2, ImmutableSet.of(A, B, C, G, H));

	public static final Ballot BC_BALLOT = createBallot("BC");
	public static final Ballot ACGH_BALLOT = createBallot("ACGH");
	public static final Ballot A_BALLOT = createBallot("A");
	public static final Ballot AC_BALLOT = createBallot("AC");
	public static final Ballot BA_BALLOT = createBallot("BA");
	private static final Ballot CA_BALLOT = createBallot("CA");

	public static final BigFraction THREE = new BigFraction(3);
	private static final ImmutableList<BallotState> stubRedistributionResult = ImmutableList.of(
		stateFor(AC_BALLOT).withVoteWeight(FOUR_FIFTHS),
		stateFor(A_BALLOT).withVoteWeight(ONE),
		stateFor(ACGH_BALLOT).withVoteWeight(ONE_FIFTH),
		stateFor(BC_BALLOT).withVoteWeight(ONE_HALF)
	);
	public static final BigFraction FIVE = new BigFraction(5);

	private final ElectionCalculationListener electionCalculationListenerMock = mock(ElectionCalculationListener.class);
	private final AmbiguityResolver ambiguityResolverMock = mock(AmbiguityResolver.class);
	private final VoteWeightRedistributor redistributorMock = mock(VoteWeightRedistributor.class);

	private final STVElectionCalculationStep step = new STVElectionCalculationStep(electionCalculationListenerMock,
	                                                                               ambiguityResolverMock);

	@Test
	public void marksOneCandidateAsWinnerAndRedistributesVoteWeightIfExactlyOneCandidateReachedTheQuorum() {
		ImmutableList<BallotState> ballotStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B, C));

		when(redistributorMock.redistributeExceededVoteWeight(A, THREE, ballotStates))
			.thenReturn(stubRedistributionResult);

		ElectionStepResult electionStepResult = step
			.declareWinnerOrStrikeCandidate(THREE, ballotStates, redistributorMock, 1, candidateStates);

		verify(electionCalculationListenerMock).candidateIsElected(A, THREE, THREE);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted((Map<Candidate, BigFraction>) argThat(
			allOf(
				hasEntry(B, ONE_HALF),
				hasEntry(C, ONE))
		));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(A)),
			withNumberOfElectedCandidates(is(2)),
			withBallotStates(containsInAnyOrder(
				aBallotState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(FOUR_FIFTHS))
				)),
				aBallotState(withPreferredCandidate(nullValue())),
				aBallotState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE_FIFTH))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(B)),
					withVoteWeight(equalTo(ONE_HALF))
				))
			))
		))));
	}

	@Test
	public void marksCandidateWithHigherNumberOfVotesAsWinnerAndRedistributesVoteWeightIfMoreThanOneCandidateReachedTheQuorum() {
		ImmutableList<BallotState> ballotStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B, C));

		when(redistributorMock.redistributeExceededVoteWeight(A, ONE, ballotStates))
			.thenReturn(stubRedistributionResult);

		ElectionStepResult electionStepResult = step
			.declareWinnerOrStrikeCandidate(ONE, ballotStates, redistributorMock, 1, candidateStates);

		verify(electionCalculationListenerMock).candidateIsElected(A, THREE, ONE);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted((Map<Candidate, BigFraction>) argThat(
			allOf(
				hasEntry(B, ONE_HALF),
				hasEntry(C, ONE))
		));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(A)),
			withNumberOfElectedCandidates(is(2)),
			withBallotStates(containsInAnyOrder(
				aBallotState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(FOUR_FIFTHS))
				)),
				aBallotState(withPreferredCandidate(nullValue())),
				aBallotState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE_FIFTH))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(B)),
					withVoteWeight(equalTo(ONE_HALF))
				))
			))
		))));
	}

	@Test
	public void marksCandidateResultingFromAbiguityResulutionAsWinnerAndRedistributesVoteWeightIfMoreThanOneCandidateWithEqualNumberOfVotesReachedWheQuorum() {
		ImmutableList<BallotState> ballotStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(BA_BALLOT),
			stateFor(BC_BALLOT)
		);
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B, C));

		when(ambiguityResolverMock.chooseOneOfMany(ImmutableSet.of(A, B)))
			.thenReturn(new AmbiguityResolverResult(B, "Fixed as Mock"));

		when(redistributorMock.redistributeExceededVoteWeight(B, TWO, ballotStates))
			.thenReturn(stubRedistributionResult);


		ElectionStepResult electionStepResult = step
			.declareWinnerOrStrikeCandidate(TWO, ballotStates, redistributorMock, 1, candidateStates);

		verify(electionCalculationListenerMock).candidateIsElected(B, TWO, TWO);
		verify(electionCalculationListenerMock).voteWeightRedistributionCompleted((Map<Candidate, BigFraction>) argThat(
			allOf(
				hasEntry(A, TWO),
				hasEntry(C, ONE_HALF))
		));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withElectedCandidate(B)),
			withNumberOfElectedCandidates(is(2)),
			withBallotStates(containsInAnyOrder(
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(FOUR_FIFTHS))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE_FIFTH))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE_HALF))
				))
			))
		))));
	}

	@Test
	public void strikesWeakestCandidateIfNoCandidateReachedTheQuorum() {
		ImmutableList<BallotState> ballotStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(ACGH_BALLOT),
			stateFor(BC_BALLOT)
		);
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B, C));

		ElectionStepResult electionStepResult = step
			.declareWinnerOrStrikeCandidate(FIVE, ballotStates, redistributorMock, 1, candidateStates);

		verify(electionCalculationListenerMock).nobodyReachedTheQuorumYet(FIVE);

		verify(electionCalculationListenerMock).candidateDropped(
			argThat(is(aMap(STVElectionCalculationStepTest.<Candidate, BigFraction>withEntries(
				Matchers.<Entry<Candidate, BigFraction>>containsInAnyOrder(
					anEntry(A, THREE),
					anEntry(B, ONE),
					anEntry(C, ZERO)
				))))),
			eq(C),
			eq(ZERO),
			argThat(is(aMap(STVElectionCalculationStepTest.<Candidate, BigFraction>withEntries(
				Matchers.<Entry<Candidate, BigFraction>>containsInAnyOrder(
					anEntry(A, THREE),
					anEntry(B, ONE)
				))))));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withLooser(C)),
			withNumberOfElectedCandidates(is(1)),
			withBallotStates(containsInAnyOrder(
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(B)),
					withVoteWeight(equalTo(ONE))
				))
			))
		))));
	}


	@Test
	public void strikesCandidateResultingFromAmbiguityResolutionIfNoCandidateReachedTheQuorumAndThereAreMoreThanOneWeakestCandidate() {
		ImmutableList<BallotState> ballotStates = ImmutableList.of(
			stateFor(AC_BALLOT),
			stateFor(A_BALLOT),
			stateFor(BA_BALLOT),
			stateFor(CA_BALLOT)
		);
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B, C));

		when(ambiguityResolverMock.chooseOneOfMany(ImmutableSet.of(B,C))).thenReturn(new AmbiguityResolverResult(B, "arbitrary message"));

		ElectionStepResult electionStepResult = step
			.declareWinnerOrStrikeCandidate(THREE, ballotStates, redistributorMock, 1, candidateStates);

		verify(electionCalculationListenerMock).nobodyReachedTheQuorumYet(THREE);

		verify(electionCalculationListenerMock).candidateDropped(
			argThat(is(aMap(STVElectionCalculationStepTest.<Candidate, BigFraction>withEntries(
				Matchers.<Entry<Candidate, BigFraction>>containsInAnyOrder(
					anEntry(A, TWO),
					anEntry(B, ONE),
					anEntry(C, ONE)
				))))),
			eq(B),
			eq(ONE),
			argThat(is(aMap(STVElectionCalculationStepTest.<Candidate, BigFraction>withEntries(
				Matchers.<Entry<Candidate, BigFraction>>containsInAnyOrder(
					anEntry(A, THREE),
					anEntry(C, ONE)
				))))));

		assertThat(electionStepResult, is(anElectionStepResult(allOf(
			withCandidateStates(withLooser(B)),
			withNumberOfElectedCandidates(is(1)),
			withBallotStates(containsInAnyOrder(
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(A)),
					withVoteWeight(equalTo(ONE))
				)),
				aBallotState(allOf(
					withPreferredCandidate(is(C)),
					withVoteWeight(equalTo(ONE))
				))
			))
		))));
	}

	private Matcher<CandidateStates> withLooser(final Candidate a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates>() {
			@Override
			protected boolean matchesSafely(CandidateStates candidateStates, Description description) {
				CandidateState candidateState = candidateStates.getCandidateState(a);
				if (candidateState == null) {
					description.appendText("candidate ").appendValue(a).appendText(" had no state");
					return false;
				}

				if (candidateState.isElected()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is elected");
					return false;
				}

				if (candidateState.isHopeful()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is still hopeful");
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with looser candidate ").appendValue(a);
			}
		};
	}

	private <K, V> Matcher<Entry<? super K, ? super V>> anEntry(final K key, final V value) {
		return new TypeSafeDiagnosingMatcher<Entry<? super K, ? super V>>() {
			@Override
			protected boolean matchesSafely(Entry<? super K, ? super V> actual, Description mismatchDescription) {
				if (!actual.getKey().equals(key)) {
					mismatchDescription.appendText("key was ").appendValue(key);
					return false;
				}

				if (!actual.getValue().equals(value)) {
					mismatchDescription.appendText("value was ").appendValue(value);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an entry ").appendValue(key + "=" + value);
			}
		};
	}

	private Matcher<CandidateStates> withElectedCandidate(final Candidate a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates>() {
			@Override
			protected boolean matchesSafely(CandidateStates candidateStates, Description description) {
				CandidateState candidateState = candidateStates.getCandidateState(a);
				if (candidateState == null) {
					description.appendText("candidate ").appendValue(a).appendText(" had no state");
					return false;
				}

				if (candidateState.isLooser()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is a looser");
					return false;
				}

				if (candidateState.isHopeful()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is still hopeful");
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with elected candidate ").appendValue(a);
			}
		};
	}

	private Matcher<ElectionStepResult> withNumberOfElectedCandidates(Matcher<Integer> subMatcher) {
		return new FeatureMatcher<ElectionStepResult, Integer>(subMatcher, "with number of elected candidates",
		                                                       "number of elected candidates") {

			@Override
			protected Integer featureValueOf(ElectionStepResult electionStepResult) {
				return electionStepResult.newNumberOfElectedCandidates;
			}
		};
	}

	private Matcher<? super ElectionStepResult> withBallotStates(
		Matcher<? super ImmutableCollection<BallotState>> subMatcher) {
		return new FeatureMatcher<ElectionStepResult, ImmutableCollection<BallotState>>(subMatcher,
		                                                                                "with ballot states",
		                                                                                "ballot states") {

			@Override
			protected ImmutableCollection<BallotState> featureValueOf(ElectionStepResult electionStepResult) {
				return electionStepResult.newBallotStates;
			}
		};
	}

	private FeatureMatcher<ElectionStepResult, CandidateStates> withCandidateStates(
		final Matcher<CandidateStates> candidateStatesMatcher) {
		return new FeatureMatcher<ElectionStepResult, CandidateStates>(candidateStatesMatcher, "with candidateStates",
		                                                               "") {
			@Override
			protected CandidateStates featureValueOf(ElectionStepResult actual) {
				return actual.newCandidateStates;
			}
		};
	}

	private TypeSafeDiagnosingMatcher<ElectionStepResult> anElectionStepResult(
		final Matcher<ElectionStepResult> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "an election step result");
	}

	private Matcher<? super BallotState> withVoteWeight(Matcher<? super BigFraction> bigFractionMatcher) {
		return new FeatureMatcher<BallotState, BigFraction>(bigFractionMatcher, "with vote weight", "vote Weight") {
			@Override
			protected BigFraction featureValueOf(BallotState actual) {
				return actual.getVoteWeight();
			}
		};
	}

	private Matcher<BallotState> withPreferredCandidate(Matcher<? super Candidate> subMatcher) {
		return new FeatureMatcher<BallotState, Candidate>(subMatcher, "with preferred candidate",
		                                                  "prefered candidate") {
			@Override
			protected Candidate featureValueOf(BallotState actual) {
				return actual.getPreferredCandidate();
			}
		};
	}

	private Matcher<BallotState> aBallotState(Matcher<? super BallotState> stateMatcher) {
		return new DelegatingMatcher<>(stateMatcher, "a ballot state");
	}


	private static class DelegatingMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
		private final Matcher<? super T> subMatcher;
		private String descriptionText;

		public DelegatingMatcher(Matcher<? super T> subMatcher, String descriptionText) {
			this.subMatcher = subMatcher;
			this.descriptionText = descriptionText;
		}

		@Override
		protected boolean matchesSafely(T item, Description mismatchDescription) {
			if (!subMatcher.matches(item)) {
				subMatcher.describeMismatch(item, mismatchDescription);
				return false;
			}

			return true;
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(descriptionText + " ").appendDescriptionOf(subMatcher);
		}
	}

	private static BallotState stateFor(Ballot ballot) {
		return new BallotState(ballot, ELECTION);
	}

	private static Ballot createBallot(String preference) {
		return TestUtils.createBallot(preference, ELECTION);
	}

	private static <K, V> Matcher<Map<K, V>> aMap(Matcher<? super Map<K, V>> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "a Map");
	}

	private static <K, V> FeatureMatcher<Map<K, V>, Set<Entry<K, V>>> withEntries(
		Matcher<? super Set<Entry<K, V>>> subMatcher) {
		return new FeatureMatcher<Map<K, V>, Set<Entry<K, V>>>(subMatcher, "with entries", "entries") {
			@Override
			protected Set<Entry<K, V>> featureValueOf(Map<K, V> actual) {
				return actual.entrySet();
			}
		};
	}

}
