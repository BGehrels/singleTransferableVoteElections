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
package info.gehrels.voting;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Objects.equal;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.SetMatchers.isSubSetOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public final class Vote<CANDIDATE_TYPE extends Candidate> {
	private final Election<CANDIDATE_TYPE> election;
	private final boolean valid;
	private final boolean no;
	private final ImmutableSet<CANDIDATE_TYPE> rankedCandidates;

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createInvalidVote(
		Election<CANDIDATE_TYPE> election) {
		return new Vote<>(election, false, false, ImmutableSet.<CANDIDATE_TYPE>of());
	}

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createPreferenceVote(
		Election<CANDIDATE_TYPE> election, ImmutableSet<CANDIDATE_TYPE> preference) {
		return new Vote<>(election, true, false, validateThat(preference, is(not(empty()))));
	}

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createNoVote(
		Election<CANDIDATE_TYPE> election) {
		return new Vote<>(election, true, true, ImmutableSet.<CANDIDATE_TYPE>of());
	}

	private Vote(Election<CANDIDATE_TYPE> election, boolean valid, boolean no,
	             ImmutableSet<CANDIDATE_TYPE> rankedCandidates) {
		this.election = validateThat(election, is(not(nullValue())));
		this.valid = valid;
		this.no = no;
		this.rankedCandidates = validateThat(rankedCandidates, isSubSetOf(election.getCandidates()));
	}

	public Election<CANDIDATE_TYPE> getElection() {
		return election;
	}

	public boolean isNo() {
		return no;
	}

	public boolean isValid() {
		return valid;
	}

	public ImmutableSet<CANDIDATE_TYPE> getRankedCandidates() {
		return rankedCandidates;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vote)) {
			return false;
		}

		Vote<?> otherVote = (Vote<?>) obj;

		return equal(election, otherVote.election) && equal(valid, otherVote.valid) && equal(no, otherVote.no) && equal(
			rankedCandidates, otherVote.rankedCandidates);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(election, valid, no, rankedCandidates);
	}

	@Override
	public String toString() {
		if (!valid) {
			return "invalid";
		} else if (no) {
			return "No";
		} else {
			return rankedCandidates.toString();
		}
	}

	public Vote<CANDIDATE_TYPE> withReplacedElection(Election<CANDIDATE_TYPE> newElection) {
		if (!newElection.getCandidates().containsAll(rankedCandidates)) {
			throw new IllegalArgumentException();
		}
		return new Vote<>(newElection, valid, no, rankedCandidates);
	}
}
