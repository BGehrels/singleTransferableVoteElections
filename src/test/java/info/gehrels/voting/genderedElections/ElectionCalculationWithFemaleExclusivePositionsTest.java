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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
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
	private final ElectionCalculation<GenderedCandidate> electionCalculationMock = mock(ElectionCalculation.class);
	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener =
		mock(ElectionCalculationWithFemaleExclusivePositionsListener.class);

	private final ElectionCalculationWithFemaleExclusivePositions objectUnderTest =
		new ElectionCalculationWithFemaleExclusivePositions(
			electionCalculationFactory,
			electionCalculationListener
		);


	@Before
	public void stubElectionCalculationFactoryToReturnElectionCalculation() {
		stub(electionCalculationFactory.createElectionCalculation(any(Election.class), any(ImmutableCollection.class)))
			.toReturn(electionCalculationMock);
	}

	@Test
	public void allAndOnlyFemaleCandidatesQualifyForFemaleOnlyPositions() {
		makeSureElectionCalculationDoesNotReturnNull();

		GenderedElection election = new GenderedElection("Example Office", 2, 0, CANDIDATES);
		objectUnderTest.calculateElectionResult(election, ballots);

		Matcher<ImmutableSet<GenderedCandidate>> containsAllAndOnlyFemaleCandidates = (Matcher) containsInAnyOrder(
			FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2);
		verify(electionCalculationMock).calculate(argThat(containsAllAndOnlyFemaleCandidates), eq(2L));
	}

	@Test
	public void onlyNotElectedCanidatesQualifyForOpenPositions() {
		GenderedElection election = new GenderedElection("Example Office", 1, 2, CANDIDATES);


		// given FEMALE_CANDIDATE_1 has already been elected in the first any female exclusive run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));

		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		Matcher<ImmutableSet<GenderedCandidate>> containsAllAndOnlyFemaleCandidates = (Matcher) containsInAnyOrder(
			FEMALE_CANDIDATE_2, CANDIDATE_E, CANDIDATE_F);
		verify(electionCalculationMock).calculate(argThat(containsAllAndOnlyFemaleCandidates), eq(2L));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenOnlyALesserNumberOfOpenPositionsAreAvailable() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given only two female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1, FEMALE_CANDIDATE_2));


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationMock);
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(1L));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenItCanAlsoHappenThatNoMalePositionsCanBeElected() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);


		// given only one female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.of(FEMALE_CANDIDATE_1));


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationMock);
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(0L));
	}

	@Test
	public void ifNotAllFemalePositionsHaveBeenFilledThenNumberOfMalePositionsMayNotBecomeNegative() {
		GenderedElection election = new GenderedElection("Example Office", 3, 2, CANDIDATES);

		// given no female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(3L)))
			.toReturn(ImmutableSet.<GenderedCandidate>of());


		objectUnderTest.calculateElectionResult(election, ballots);

		// Then all non elected candidates qualify for the second run.
		InOrder inOrder = inOrder(electionCalculationMock);
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(3L));
		inOrder.verify(electionCalculationMock).calculate(any(ImmutableSet.class), eq(0L));
	}

	@Test
	public void shouldReportIfNotAllNonFemaleExclusivePositionsCanBeElected() {
		GenderedElection election = new GenderedElection("Example Office", 1, 2, CANDIDATES);

		// given no female positions have been elected in the first run
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), eq(1L)))
			.toReturn(ImmutableSet.<GenderedCandidate>of());


		objectUnderTest.calculateElectionResult(election, ballots);

		verify(electionCalculationListener).reducedNonFemaleExclusiveSeats(1, 0, 2, 1);
	}


	private void makeSureElectionCalculationDoesNotReturnNull() {
		stub(electionCalculationMock.calculate(any(ImmutableSet.class), anyLong()))
			.toReturn(ImmutableSet.<GenderedCandidate>of());
	}


}
