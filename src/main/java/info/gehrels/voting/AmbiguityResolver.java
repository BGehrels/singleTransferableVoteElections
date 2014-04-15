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
package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public interface AmbiguityResolver<CANDIDATE_TYPE extends Candidate> {
    AmbiguityResolverResult<CANDIDATE_TYPE> chooseOneOfMany(ImmutableSet<CANDIDATE_TYPE> bestCandidates);

	class AmbiguityResolverResult<CANDIDATE_TYPE extends Candidate> {
		public final CANDIDATE_TYPE chosenCandidate;
		public final String auditLog;

		public AmbiguityResolverResult(CANDIDATE_TYPE chosenCandidate, String auditLog) {
			this.chosenCandidate = validateThat(chosenCandidate, is(not(nullValue())));
			this.auditLog = validateThat(auditLog, not(isEmptyOrNullString()));
		}
	}
}
