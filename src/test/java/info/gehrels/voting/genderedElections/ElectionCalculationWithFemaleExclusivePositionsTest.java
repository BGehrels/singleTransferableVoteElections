/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculation;
import info.gehrels.voting.ElectionCalculationFactory;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.verify;

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

	private final ImmutableCollection<Ballot<GenderedCandidate>> ballots =
		ImmutableList.of((Ballot<GenderedCandidate>) mock(Ballot.class));

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
		stub(electionCalculationFactory.createElectionCalculation(any(Election.class), any(ImmutableCollection.class)))
			.toReturn(plainElectionCalculationMock);
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
	public void onlyNotYetElectedCanidatesQualifyForOpenPositions() {
		GenderedElection election = new GenderedElection("Example Office", 1, 2, CANDIDATES);


		// given FEMALE_CANDIDATE_1 has already been elected in the first any female exclusive run
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));

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
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2));


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
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));


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
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.<GenderedCandidate>of());


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		// Then the second run has zero seats to fill.
		InOrder inOrder = inOrder(plainElectionCalculationMock);
		inOrder.verify(plainElectionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		inOrder.verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), longThat(is(lessThan(0L))));
	}

	@Test
	public void shouldReportIfNotAllNonFemaleExclusivePositionsCanBeElected() {
		GenderedElection election = new GenderedElection("Example Office", 1, 2, CANDIDATES);

		// given no female positions have been elected in the first run
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.toReturn(ImmutableSet.<GenderedCandidate>of());


		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(electionCalculationListener).reducedNonFemaleExclusiveSeats(1, 0, 2, 1);
	}

	@Test
	public void shouldReportTheStartOfTheElectionRunAndTheStartOfTheIndividualGenderedCalculations() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given only two female positions have been elected in the first run
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2));


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
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.toReturn(ImmutableSet.<GenderedCandidate>of(FEMALE_CANDIDATE_1));

		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), eq(0));
	}

	@Test
	public void shouldNotCallTheRespectiveElectionCalculationIfNoNonFemaleExclusiveSeatsAreElectable() {
	    GenderedElection election = new GenderedElection("Example Office", 1, 1, CANDIDATES);

		// given none of the female positions have been elected in the first run
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.toReturn(ImmutableSet.<GenderedCandidate>of());

		genderedElectionCalculation.calculateElectionResult(election, ballots);

		verify(plainElectionCalculationMock, never()).calculate(any(ImmutableSet.class), eq(0));
	}




	private void makeSureElectionCalculationDoesNotReturnNull() {
		stub(plainElectionCalculationMock.calculate(any(ImmutableSet.class), anyLong()))
			.toReturn(ImmutableSet.<GenderedCandidate>of());
	}


}
