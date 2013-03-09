package info.gehrels.voting;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class FemaleConditionTest {
	public static final Candidate ALICE = new Candidate("Alice", true);
	public static final Candidate BOB = new Candidate("Bob", false);
	private final ElectionCalculationListener mock = mock(ElectionCalculationListener.class);
	private final FemaleCondition condition = new FemaleCondition(mock);

	@Test
	public void femaleCandidatesAreQualified() {
		assertThat(condition.isQualified(ALICE), is(true));
	}

	@Test
	public void doesNotReportToElectionCalculationListenerWhenCandidatesAreQualified() {
		condition.isQualified(ALICE);

		verify(mock, never()).candidateNotQualified(isA(Candidate.class), anyString());
	}

	@Test
	public void nonFemaleCandidatesAreNotQualified() {
		Candidate bob = new Candidate("Bob", false);
		assertThat(condition.isQualified(bob), is(false));
	}

	@Test
	public void reportsToElectionCalculationListenerWhenCandidatesAreNotQualified() {
		Candidate bob = new Candidate("Bob", false);

		condition.isQualified(bob);

		verify(mock).candidateNotQualified(bob, "The candidate is not female.");
	}
}
