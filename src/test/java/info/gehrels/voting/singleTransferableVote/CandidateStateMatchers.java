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
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public final class CandidateStateMatchers {
	private CandidateStateMatchers() {
	}

	static <CANDIDATE_TYPE extends Candidate> Matcher<CandidateStates<CANDIDATE_TYPE>> withLooser(final CANDIDATE_TYPE a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates<CANDIDATE_TYPE>>() {
			@Override
			protected boolean matchesSafely(CandidateStates<CANDIDATE_TYPE> candidateStates, Description description) {
				CandidateState<CANDIDATE_TYPE> candidateState = candidateStates.getCandidateState(a);
				if (candidateState == null) {
					description.appendText("candidate ").appendValue(a).appendText(" had no state");
					return false;
				}

				if (candidateState.isElected()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is elected");
					return false;
				}

				if (candidateState.isHopeful()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is still hopeful");
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with looser candidate ").appendValue(a);
			}
		};
	}

	static <CANDIDATE_TYPE>  Matcher<CandidateStates<CANDIDATE_TYPE>> withElectedCandidate(final CANDIDATE_TYPE a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates<CANDIDATE_TYPE>>() {
			@Override
			protected boolean matchesSafely(CandidateStates<CANDIDATE_TYPE> candidateStates, Description description) {
				CandidateState<CANDIDATE_TYPE> candidateState = candidateStates.getCandidateState(a);
				if (candidateState == null) {
					description.appendText("candidate ").appendValue(a).appendText(" had no state");
					return false;
				}

				if (candidateState.isLooser()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is a looser");
					return false;
				}

				if (candidateState.isHopeful()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is still hopeful");
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with elected candidate ").appendValue(a);
			}
		};
	}

	static <CANDIDATE_TYPE> Matcher<CandidateState<CANDIDATE_TYPE>> candidateStateFor(final CANDIDATE_TYPE candidate) {
		return new TypeSafeDiagnosingMatcher<CandidateState<CANDIDATE_TYPE>>() {
			@Override
			protected boolean matchesSafely(CandidateState<CANDIDATE_TYPE> candidateState, Description description) {
				if (candidateState.getCandidate() != candidate) {
					description.appendText("a candidate state for ").appendValue(candidateState);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a candidateState for ").appendValue(candidate);
			}
		};
	}
}
