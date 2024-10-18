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

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.genderedElections.ElectionCalculationWithFemaleExclusivePositionsListener.NonQualificationReason;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public final class NotElectedBeforePredicateTest {
	private static final GenderedCandidate ALICE = new GenderedCandidate("Alice", true);
	private static final GenderedCandidate BOB = new GenderedCandidate("Bob", false);
	private static final GenderedCandidate EVE = new GenderedCandidate("Eve", true);
	private static final ImmutableSet<GenderedCandidate> ELECTED_CANDIDATES = ImmutableSet.of(BOB, EVE);

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
		verify(electionCalculationListener, never()).candidateNotQualified(isA(GenderedCandidate.class), any(
			NonQualificationReason.class));
	}


	@Test
	public void candidatesAreNotQualifiedIfTheyHaveBeenElectedBefore() {
		assertThat(condition.apply(EVE), is(false));
	}

	@Test
	public void callListenerWhenCandidateIsNotQualified() {
		condition.apply(EVE);
		verify(electionCalculationListener).candidateNotQualified(EVE, NonQualificationReason.ALREADY_ELECTED);
	}


}
