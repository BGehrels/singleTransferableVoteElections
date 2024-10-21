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
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Objects.equal;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.CollectionMatchers.hasOnlyDistinctElements;
import static info.gehrels.voting.CollectionMatchers.isSubSetOf;
import static org.hamcrest.Matchers.*;

public final class Vote<CANDIDATE_TYPE extends Candidate> {
	private final Election<CANDIDATE_TYPE> election;
	private final boolean valid;
	private final boolean no;
	private final ImmutableList<CANDIDATE_TYPE> rankedCandidates;

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createInvalidVote(
		Election<CANDIDATE_TYPE> election) {
		return new Vote<>(election, false, false, ImmutableList.of());
	}

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createPreferenceVote(
		Election<CANDIDATE_TYPE> election, ImmutableList<CANDIDATE_TYPE> preference) {
		return new Vote<>(election, true, false, validateThat(preference, is(not(empty()))));
	}

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createNoVote(
		Election<CANDIDATE_TYPE> election) {
		return new Vote<>(election, true, true, ImmutableList.of());
	}

	private Vote(Election<CANDIDATE_TYPE> election, boolean valid, boolean no,
	             ImmutableList<CANDIDATE_TYPE> rankedCandidates) {
		this.election = validateThat(election, is(not(nullValue())));
		this.valid = valid;
		this.no = no;
		this.rankedCandidates = validateThat(
				rankedCandidates,
				allOf(
						isSubSetOf(election.getCandidates()),
						hasOnlyDistinctElements()
				)
		);
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

	public ImmutableList<CANDIDATE_TYPE> getRankedCandidates() {
		return rankedCandidates;
	}

	public Vote<CANDIDATE_TYPE> withReplacedElection(Election<CANDIDATE_TYPE> newElection) {
		return new Vote<>(newElection, valid, no, rankedCandidates);
	}

	public Vote<CANDIDATE_TYPE> withReplacedCandidateVersion(Election<CANDIDATE_TYPE> adaptedElection, CANDIDATE_TYPE newCandidateVersion) {
		if (!election.getOfficeName().equals(adaptedElection.getOfficeName())) {
			throw new IllegalArgumentException("the office name must not be changed. Original Election: " + election + ", new Election: " + adaptedElection);
		}

		ImmutableList.Builder<CANDIDATE_TYPE> newRankedCandidates = ImmutableList.builder();
		for (CANDIDATE_TYPE existingRankedCandidate : rankedCandidates) {
			if (existingRankedCandidate.getName().equals(newCandidateVersion.getName())) {
				newRankedCandidates.add(newCandidateVersion);
			} else {
				newRankedCandidates.add(existingRankedCandidate);
			}
		}

		return new Vote<>(adaptedElection, valid, no, newRankedCandidates.build());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vote<?> otherVote)) {
			return false;
		}

        return equal(election, otherVote.election) && equal(valid, otherVote.valid) && equal(no, otherVote.no) &&
				equal(rankedCandidates, otherVote.rankedCandidates);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(election, valid, no, rankedCandidates);
	}

	@Override
	public String toString() {
		String prefix = election.getOfficeName() + ": ";
		if (!valid) {
			return prefix + "invalid";
		} else if (no) {
			return prefix + "no";
		} else {
			return prefix + rankedCandidates.toString();
		}
	}
}
