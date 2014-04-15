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
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Candidate;
import org.junit.Test;

import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.candidateStateFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class CandidateStatesTest {
	public static final Candidate A = new Candidate("A");
	public static final Candidate B = new Candidate("B");
	public static final Candidate C = new Candidate("C");
	public static final Candidate D = new Candidate("D");

	@Test
	public void returnsEmptyCandidateSetIfCandidateStateMapIsEmpty() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.<Candidate>of());
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();
		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsEmptyCandidateSetIfAllCandidatesAreElected() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B)).withElected(A)
			.withElected(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();
		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsEmptyCandidateSetIfAllCandidatesAreLoosers() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B)).withLooser(A)
			.withLooser(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsOnlyHopfulCandidates() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B, C, D)).withLooser(A)
			.withElected(C);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, containsInAnyOrder(B, D));
	}

	@Test
	public void returnsCandidateStateForKnownCandidate() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A));

		assertThat(candidateStates.getCandidateState(A).getCandidate(), is(A));
	}

	@Test
	public void returnsNullForUnknownCandidate() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A));

		CandidateState<Candidate> candidateState = candidateStates.getCandidateState(B);

		assertThat(candidateState, is(nullValue()));
	}


	@Test
	public void withLooserReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsLooser() {
		CandidateStates<Candidate> oldCandidateStates = new CandidateStates<>(ImmutableSet.of(A, B));

		CandidateStates<Candidate> result = oldCandidateStates.withLooser(B);

		assertThat(result.getCandidateState(B).isLooser(), is(true));
	}


	@Test
	public void withElectedReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsElected() {
		CandidateStates<Candidate> oldCandidateStates = new CandidateStates<>(ImmutableSet.of(A, B));

		CandidateStates<Candidate> result = oldCandidateStates.withElected(B);

		assertThat(result.getCandidateState(B).isElected(), is(true));
	}

	@Test
	public void returnsEmptyIteratorForEmptyState() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.<Candidate>of());
		assertThat(candidateStates, is(emptyIterable()));
	}

	@Test
	public void returnsAllCandidateStatesInArbitraryOrder() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B));
		assertThat(candidateStates, containsInAnyOrder(candidateStateFor(A), candidateStateFor(B)));
	}


}
