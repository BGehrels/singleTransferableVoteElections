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

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * A Candidate is a person running for one office.
 */
public class Candidate {
    public final String name;

	public Candidate(String name) {
	    this.name = validateThat(name, not(isEmptyOrNullString()));
    }

	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
