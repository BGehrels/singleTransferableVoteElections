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

public class NotElectedBeforeConditionTest {

	public static final Candidate ALICE = new Candidate("Alice", true);
	public static final Candidate BOB = new Candidate("Bob", true);
	public static final Candidate EVE = new Candidate("Eve", true);
	public static final ImmutableSet<Candidate> ELECTED_CANDIDATES = ImmutableSet.of(BOB, EVE);
	private final ElectionCalculationListener electionCalculationListener = mock(ElectionCalculationListener.class);
	private final QualificationCondition condition = new NotElectedBeforeCondition(ELECTED_CANDIDATES,
	                                                                               electionCalculationListener);

	@Test
	public void candidatesAreQualifiedIfTheyHaveNotBeenElectedBefore() {
		assertThat(condition.isQualified(ALICE), is(true));
	}

	@Test
	public void doNotCallListenerWheneverCandidateIsNotQualified() {
		condition.isQualified(ALICE);
		verify(electionCalculationListener, never()).candidateNotQualified(isA(Candidate.class), anyString());
	}


	@Test
	public void candidatesAreNotQualifiedIfTheyHaveBeenElectedBefore() {
		assertThat(condition.isQualified(EVE), is(false));
	}

	@Test
	public void callListenerWheneverCandidateIsNotQualified() {
		condition.isQualified(EVE);
		verify(electionCalculationListener).candidateNotQualified(EVE, "The candidate has already been elected.");
	}


}
