package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class NotElectedBeforePredicateTest {
	public static final GenderedCandidate ALICE = new GenderedCandidate("Alice", true);
	public static final GenderedCandidate BOB = new GenderedCandidate("Bob", false);
	public static final GenderedCandidate EVE = new GenderedCandidate("Eve", true);
	public static final ImmutableSet<GenderedCandidate> ELECTED_CANDIDATES = ImmutableSet.of(BOB, EVE);

	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener =
		mock(ElectionCalculationWithFemaleExclusivePositionsListener.class);
	private final NotElectedBeforePredicate condition = new NotElectedBeforePredicate(ELECTED_CANDIDATES,
	                                                                                  electionCalculationListener);

	@Test
	public void candidatesAreQualifiedIfTheyHaveNotBeenElectedBefore() {
		assertThat(condition.apply(ALICE), is(true));
	}

	@Test
	public void doNotCallListenerWhenCandidateIsQualified() {
		condition.apply(ALICE);
		verify(electionCalculationListener, never()).candidateNotQualified(isA(GenderedCandidate.class), anyString());
	}


	@Test
	public void candidatesAreNotQualifiedIfTheyHaveBeenElectedBefore() {
		assertThat(condition.apply(EVE), is(false));
	}

	@Test
	public void callListenerWhenCandidateIsNotQualified() {
		condition.apply(EVE);
		verify(electionCalculationListener).candidateNotQualified(EVE, "The candidate has already been elected.");
	}


}
