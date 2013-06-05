package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class NotElectedBeforePredicateTest {

	public static final Candidate ALICE = new Candidate("Alice");
	public static final Candidate BOB = new Candidate("Bob");
	public static final Candidate EVE = new Candidate("Eve");
	public static final ImmutableSet<Candidate> ELECTED_CANDIDATES = ImmutableSet.of(BOB, EVE);
	private final ElectionCalculationListener electionCalculationListener = mock(ElectionCalculationListener.class);
	private final NotElectedBeforePredicate condition = new NotElectedBeforePredicate(ELECTED_CANDIDATES,
	                                                                               electionCalculationListener);

	@Test
	public void candidatesAreQualifiedIfTheyHaveNotBeenElectedBefore() {
		assertThat(condition.apply(ALICE), is(true));
	}

	@Test
	public void doNotCallListenerWheneverCandidateIsNotQualified() {
		condition.apply(ALICE);
		verify(electionCalculationListener, never()).candidateNotQualified(isA(Candidate.class), anyString());
	}


	@Test
	public void candidatesAreNotQualifiedIfTheyHaveBeenElectedBefore() {
		assertThat(condition.apply(EVE), is(false));
	}

	@Test
	public void callListenerWheneverCandidateIsNotQualified() {
		condition.apply(EVE);
		verify(electionCalculationListener).candidateNotQualified(EVE, "The candidate has already been elected.");
	}


}
