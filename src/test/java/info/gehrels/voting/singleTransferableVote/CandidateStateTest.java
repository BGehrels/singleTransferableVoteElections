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

import info.gehrels.voting.Candidate;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

public final class CandidateStateTest {
	public static final Candidate ARBITRARY_CANDIDATE = new Candidate("Adam");

	@Test
	public void hasNotALooserAndNotElectedAsInitialState() {
		CandidateState<?> initialState = new CandidateState<>(ARBITRARY_CANDIDATE);
		assertThat(initialState.isLooser(), is(false));
		assertThat(initialState.isElected(), is(false));
	}

	@Test
	public void returnsCandidate() {
		CandidateState<Candidate> state = new CandidateState<>(ARBITRARY_CANDIDATE);
		assertThat(state.getCandidate(), is(ARBITRARY_CANDIDATE));
	}

	@Test
	public void isInitiallyHopefull() {
		CandidateState<?> state = new CandidateState<>(ARBITRARY_CANDIDATE);
		assertThat(state.isHopeful(), is(true));
	}


	@Test
	public void createsNewStateAsElectedCandidate() {
		CandidateState<Candidate> initialState = new CandidateState<>(ARBITRARY_CANDIDATE);
		CandidateState<Candidate> electedState = initialState.asElected();

		assertThat(electedState, is(not(sameInstance(initialState))));
		assertThat(electedState.isElected(), is(true));
	}


	@Test
	public void winnersAreNotHopful() {
		CandidateState<?> state = new CandidateState<>(ARBITRARY_CANDIDATE).asElected();
		assertThat(state.isHopeful(), is(false));
	}

	@Test
	public void createsNewStateAsLooserCandidate() {
		CandidateState<Candidate> initialState = new CandidateState<>(ARBITRARY_CANDIDATE);
		CandidateState<Candidate> looserState = initialState.asLooser();

		assertThat(looserState, is(not(sameInstance(initialState))));
		assertThat(looserState.isLooser(), is(true));
	}


	@Test
	public void loosersAreNotHopeful() {
		CandidateState<?> state = new CandidateState<>(ARBITRARY_CANDIDATE).asLooser();
		assertThat(state.isHopeful(), is(false));
	}


}
