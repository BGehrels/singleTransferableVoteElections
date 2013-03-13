package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static info.gehrels.voting.TestUtils.OFFICE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

public class ElectionCalculationWithFemaleExclusivePositionsTest {

	public static final Candidate FEMALE_CANDIDATE_1 = new Candidate("F", true);
	public static final Candidate FEMALE_CANDIDATE_2 = new Candidate("G", true);
	public static final Candidate CANDIDATE_E = new Candidate("A", false);
	public static final Candidate CANDIDATE_F = new Candidate("B", false);

	private static final ImmutableSet<Candidate> CANDIDATES = ImmutableSet.of(
		FEMALE_CANDIDATE_1,
		FEMALE_CANDIDATE_2,
		CANDIDATE_E,
		CANDIDATE_F
	);

	private final ElectionCalculationFactory electionCalculationFactory = mock(ElectionCalculationFactory.class);
	private final ImmutableCollection<Ballot> ballots = ImmutableList.of(mock(Ballot.class));

	private final STVElectionCalculation electionCalculationMock =
		mock(STVElectionCalculation.class);

	private ElectionCalculationWithFemaleExclusivePositions objectUnderTest =
		new ElectionCalculationWithFemaleExclusivePositions(
			electionCalculationFactory,
			mock(ElectionCalculationListener.class)
		);



	@Before
	public void stubElectionCalculationFactoryToReturnElectionCalculation() {
		stub(electionCalculationFactory.createElectionCalculation(any(Election.class), any(ImmutableCollection.class)))
			.toReturn(electionCalculationMock);
	}

	@Test
	public void allAndOnlyFemaleCandidatesQualifyForFemaleOnlyPositions() {
		makeSureElectionCalculationDoesNotReturnNull();

		Election election = new Election(OFFICE, 2, 0, CANDIDATES);
		objectUnderTest.calculateElectionResult(election, ballots);

		Matcher<ImmutableSet<Candidate>> containsAllAndOnlyFemaleCandidates = (Matcher) containsInAnyOrder(
			FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2);
		verify(electionCalculationMock).calculate(argThat(containsAllAndOnlyFemaleCandidates), eq(2));
	}

	@Test
	public void onlyNotElectedCanidatesQualifyForOpenPositions() {
		Election election = new Election(OFFICE, 1, 2, CANDIDATES);


		// given FEMALE_CANDIDATE_1 has already been elected in the first any female exclusive run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(1)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		Matcher<ImmutableSet<Candidate>> containsAllAndOnlyFemaleCandidates = (Matcher) containsInAnyOrder(
			FEMALE_CANDIDATE_2, CANDIDATE_E, CANDIDATE_F);
		verify(electionCalculationMock).calculate(argThat(containsAllAndOnlyFemaleCandidates), eq(2));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenOnlyALesserNumberOfOpenPositionsAreAvailable() {
		Election election = new Election(OFFICE, 3, 2, CANDIDATES);


		// given only two female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(3)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2));


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationMock);
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(3));
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(1));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenItCanAlsoHappenThatNoMalePositionsCanBeElected() {
		Election election = new Election(OFFICE, 3, 2, CANDIDATES);


		// given only two female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(3)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationMock);
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(3));
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(0));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenNumberOfMalePositionsMayNotBecomeNegative() {
		Election election = new Election(OFFICE, 3, 2, CANDIDATES);


		// given only two female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(3)))
			.toReturn(ImmutableSet.<Candidate>of());


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationMock);
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(3));
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(0));
	}

	@Test
	public void shouldReportIfNotAllNonFemaleExclusivePositionsCanBeElected() {
	    throw new UnsupportedOperationException("unimplemented");
	}



	private void makeSureElectionCalculationDoesNotReturnNull() {
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), anyInt()))
			.toReturn(ImmutableSet.<Candidate>of());
	}


}
