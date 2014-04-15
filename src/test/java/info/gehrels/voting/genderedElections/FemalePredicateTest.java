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

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class FemalePredicateTest {
	private static final GenderedCandidate ALICE = new GenderedCandidate("Alice", true);
	private static final GenderedCandidate BOB = new GenderedCandidate("Bob", false);

	private final ElectionCalculationWithFemaleExclusivePositionsListener mock = mock(ElectionCalculationWithFemaleExclusivePositionsListener.class);
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
