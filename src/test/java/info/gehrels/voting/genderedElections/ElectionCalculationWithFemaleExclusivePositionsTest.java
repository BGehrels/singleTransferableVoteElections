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
package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.*;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.hamcrest.MockitoHamcrest.longThat;
import static org.mockito.Mockito.*;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

public final class ElectionCalculationWithFemaleExclusivePositionsTest {
    private static final GenderedCandidate FEMALE_CANDIDATE_1 = new GenderedCandidate("F", true);
	private static final GenderedCandidate FEMALE_CANDIDATE_2 = new GenderedCandidate("G", true);
	private static final GenderedCandidate CANDIDATE_E = new GenderedCandidate("A", false);
	private static final GenderedCandidate CANDIDATE_F = new GenderedCandidate("B", false);

	private static final ImmutableSet<GenderedCandidate> CANDIDATES = ImmutableSet.of(
		FEMALE_CANDIDATE_1,
		FEMALE_CANDIDATE_2,
		CANDIDATE_E,
		CANDIDATE_F
	);
    public static final Ballot<GenderedCandidate> BALLOT = new Ballot<>(1, ImmutableSet.of());

    private final ImmutableCollection<Ballot<GenderedCandidate>> ballots =
		ImmutableList.of(BALLOT);

	private final ElectionCalculationFactory<GenderedCandidate> electionCalculationFactory =
		mock(ElectionCalculationFactory.class);
	private final ElectionCalculation<GenderedCandidate> plainElectionCalculationMock = mock(ElectionCalculation.class);
	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener =
		mock(ElectionCalculationWithFemaleExclusivePositionsListener.class);

	private final ElectionCalculationWithFemaleExclusivePositions genderedElectionCalculation =
		new ElectionCalculationWithFemaleExclusivePositions(
			electionCalculationFactory,
			electionCalculationListener
		);


	@Before
	public void stubElectionCalculationFactoryToReturnElectionCalculation() {
		when(electionCalculationFactory.createElectionCalculation(any(Election.class), any(ImmutableCollection.class)))
			.thenReturn(plainElectionCalculationMock);
	}

	@Test
	public void allAndOnlyFemaleCandidatesQualifyForFemaleOnlyPositions() {
		makeSureElectionCalculationDoesNotReturnNull();

		GenderedElection election = new GenderedElection("Example Office", 2, 0, CANDIDATES);
		genderedElectionCalculation.calculateElectionResult(election, ballots);

		Matcher<ImmutableSet<GenderedCandidate>> containsAllAndOnlyFemaleCandidates = (Matcher) containsInAnyOrder(
			FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2);
		verify(plainElectionCalculationMock).calculate(argThat(containsAllAndOnlyFemaleCandidates), eq(2L));
	}

	@Test
	public void onlyNotYetElectedCandidatesQualifyForOpenPositions() {
		GenderedElection election = new GenderedElection("Example Office", 1, 2, CANDIDATES);


		// given FEMALE_CANDIDATE_1 has already been elected in the first any female exclusive run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.thenReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));

		genderedElectionCalculation.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		Matcher<ImmutableSet<GenderedCandidate>> containsAllAndOnlyFemaleCandidates = (Matcher) containsInAnyOrder(
			FEMALE_CANDIDATE_2, CANDIDATE_E, CANDIDATE_F);
		verify(plainElectionCalculationMock).calculate(argThat(containsAllAndOnlyFemaleCandidates), eq(2L));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenOnlyALesserNumberOfOpenPositionsAreAvailable() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given only two female positions have been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.thenReturn(ImmutableSet.of(FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2));


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(plainElectionCalculationMock);
		inOrder.verify(plainElectionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		inOrder.verify(plainElectionCalculationMock).calculate(any(ImmutableSet.class), eq(1L));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenItCanAlsoHappenThatNoMalePositionsCanBeElected() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given only one female position has been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.thenReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		// Then the second run has zero seats to fill
		InOrder inOrder = inOrder(plainElectionCalculationMock);
		inOrder.verify(plainElectionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		inOrder.verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), anyLong());
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenNumberOfMalePositionsMayNotBecomeNegative() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given no female positions have been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.thenReturn(ImmutableSet.<GenderedCandidate>of());


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		// Then the second run has zero seats to fill.
		InOrder inOrder = inOrder(plainElectionCalculationMock);
		inOrder.verify(plainElectionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		Matcher<Long> tMatcher = is(lessThan(0L));
		inOrder.verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), longThat(tMatcher));
	}

	@Test
	public void shouldReportIfNotAllNonFemaleExclusivePositionsCanBeElected() {
		GenderedElection election = new GenderedElection("Example Office", 1, 2, CANDIDATES);

		// given no female positions have been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.thenReturn(ImmutableSet.<GenderedCandidate>of());


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(electionCalculationListener).reducedNonFemaleExclusiveSeats(1, 0, 2, 1);
	}

	@Test
	public void shouldReportTheStartOfTheElectionRunAndTheStartOfTheIndividualGenderedCalculations() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given only two female positions have been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.thenReturn(ImmutableSet.of(FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2));


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationListener);
		inOrder.verify(electionCalculationListener).startElectionCalculation(election, ballots);
		inOrder.verify(electionCalculationListener).startFemaleExclusiveElectionRun();
		inOrder.verify(electionCalculationListener).startNonFemaleExclusiveElectionRun();
	}

	@Test
	public void shouldNotCallTheRespectiveElectionCalculationIfThereAreNoFemaleExclusiveSeats() {
	    GenderedElection election = new GenderedElection("Example Office", 0, 1, CANDIDATES);

		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), eq(0));
	}

	@Test
	public void shouldNotCallTheRespectiveElectionCalculationIfThereAreNoNonFemaleExclusiveSeats() {
	    GenderedElection election = new GenderedElection("Example Office", 1, 0, CANDIDATES);

		// given all female positions have been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.thenReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));

		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), eq(0));
	}

	@Test
	public void shouldNotCallTheRespectiveElectionCalculationIfNoNonFemaleExclusiveSeatsAreElectable() {
	    GenderedElection election = new GenderedElection("Example Office", 1, 1, CANDIDATES);

		// given none of the female positions have been elected in the first run
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.thenReturn(ImmutableSet.<GenderedCandidate>of());

		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), eq(0));
	}

	private void makeSureElectionCalculationDoesNotReturnNull() {
		when(plainElectionCalculationMock.calculate(any(ImmutableSet.class), anyLong()))
			.thenReturn(ImmutableSet.<GenderedCandidate>of());
	}


}
