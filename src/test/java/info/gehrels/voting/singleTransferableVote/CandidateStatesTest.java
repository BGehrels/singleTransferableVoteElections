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
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Candidate;
import org.junit.jupiter.api.Test;

import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.candidateStateFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public final class CandidateStatesTest {
	private static final Candidate A = new Candidate("A");
	private static final Candidate B = new Candidate("B");
	private static final Candidate C = new Candidate("C");
	private static final Candidate D = new Candidate("D");

	@Test
	public void returnsEmptyCandidateSetIfCandidateStateMapIsEmpty() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of());
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
	public void returnsEmptyCandidateSetIfAllCandidatesAreLosers() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B)).withLoser(A)
			.withLoser(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsOnlyHopefulCandidates() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B, C, D)).withLoser(A)
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
	public void withLooserReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsNonHopeful() {
		CandidateStates<Candidate> oldCandidateStates = new CandidateStates<>(ImmutableSet.of(A, B));

		CandidateStates<Candidate> result = oldCandidateStates.withLoser(B);

		assertThat(result.getCandidateState(B).isHopeful(), is(false));
	}


	@Test
	public void withElectedReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsElected() {
		CandidateStates<Candidate> oldCandidateStates = new CandidateStates<>(ImmutableSet.of(A, B));

		CandidateStates<Candidate> result = oldCandidateStates.withElected(B);

		assertThat(result.getCandidateState(B).isElected(), is(true));
	}

	@Test
	public void returnsEmptyIteratorForEmptyState() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of());
		assertThat(candidateStates, is(emptyIterable()));
	}

	@Test
	public void returnsAllCandidateStatesInArbitraryOrder() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B));
		assertThat(candidateStates, containsInAnyOrder(candidateStateFor(A), candidateStateFor(B)));
	}


}
