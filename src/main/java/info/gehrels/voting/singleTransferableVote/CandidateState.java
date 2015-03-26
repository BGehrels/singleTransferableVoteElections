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

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;

final class CandidateState<CANDIDATE_TYPE> {
	private final CANDIDATE_TYPE candidate;
	private boolean elected = false;
	private boolean looser = false;


	CandidateState(CANDIDATE_TYPE candidate) {
		this.candidate = candidate;
	}

	private CandidateState(CANDIDATE_TYPE candidate, boolean elected, boolean looser) {
		this.candidate = candidate;
		this.elected = elected;
		this.looser = looser;
	}

	public CANDIDATE_TYPE getCandidate() {
		return candidate;
	}

	public boolean isHopeful() {
		return !elected && !looser;
	}

	public boolean isElected() {
		return elected;
	}

	public CandidateState<CANDIDATE_TYPE> asElected() {
		validateThat("Candidate " + candidate + " may not already be a looser", looser, is(false));
		return new CandidateState<>(candidate, true, false);
	}

	public CandidateState<CANDIDATE_TYPE> asLooser() {
		validateThat("Candidate " + candidate + " may not already be elected", elected, is(false));
		return new CandidateState<>(candidate, false, true);
	}

	@Override
	public String toString() {
		return candidate.toString() + ": " + (elected ? "" : "not ") + "elected, " + (looser ? "" : "no ") + "looser";
	}
}
