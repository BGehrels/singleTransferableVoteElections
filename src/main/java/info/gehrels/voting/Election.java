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

import com.google.common.collect.ImmutableSet;

import java.util.Optional;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.*;

/**
 * An Election is defined by one or more Candidates running for an Office. It also defines, how many Candidates may at
 * most be successful.
 */
public class Election<CANDIDATE_TYPE extends Candidate> {
	private final String officeName;
	private final ImmutableSet<CANDIDATE_TYPE> candidates;

	public Election(String officeName, ImmutableSet<CANDIDATE_TYPE> candidates) {
		this.officeName = validateThat(officeName, not(is(emptyOrNullString())));
		this.candidates = validateThat(candidates, is(notNullValue()));
	}

	public final String getOfficeName() {
		return officeName;
	}

	public final ImmutableSet<CANDIDATE_TYPE> getCandidates() {
		return candidates;
	}

	public final Optional<CANDIDATE_TYPE> getCandidate(String name) {
		return candidates.stream().filter(c -> c.getName().equals(name)).findAny();
	}
}
