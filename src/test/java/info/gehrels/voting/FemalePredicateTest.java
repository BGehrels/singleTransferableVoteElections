package info.gehrels.voting;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class FemalePredicateTest {
	private static final GenderedCandidate ALICE = new GenderedCandidate("Alice", true);
	private static final GenderedCandidate BOB = new GenderedCandidate("Bob", false);
	private final ElectionCalculationListener<GenderedCandidate> mock = mock(ElectionCalculationListener.class);
	private final FemalePredicate condition = new FemalePredicate(mock);

	@Test
	public void femaleCandidatesAreQualified() {
		assertThat(condition.apply(ALICE), is(true));
	}

	@Test
	public void doesNotReportToElectionCalculationListenerWhenCandidatesAreQualified() {
		condition.apply(ALICE);

		verify(mock, never()).candidateNotQualified(isA(GenderedCandidate.class), anyString());
	}

	@Test
	public void nonFemaleCandidatesAreNotQualified() {
		assertThat(condition.apply(BOB), is(false));
	}

	@Test
	public void reportsToElectionCalculationListenerWhenCandidatesAreNotQualified() {
		condition.apply(BOB);

		verify(mock).candidateNotQualified(BOB, "The candidate is not female.");
	}
}
