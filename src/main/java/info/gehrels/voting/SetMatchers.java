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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.Collection;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;

public final class SetMatchers {
	private SetMatchers() {
	}

	public static  <SUPERSET extends Collection<?>, SUBSET extends Collection<?>> Matcher<SUBSET> isSubSetOf(final SUPERSET potentialSuperset) {
		validateThat(potentialSuperset, is(not(nullValue())));

		return new TypeSafeDiagnosingMatcher<SUBSET>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("a collection completly contained in ").appendValue(potentialSuperset);
			}

			@Override
			protected boolean matchesSafely(SUBSET potentialSubset, Description mismatchDescription) {
				boolean matched = potentialSuperset.containsAll(potentialSubset);
				if (!matched) {
					Collection<?> badElements = new ArrayList<Object>(potentialSubset);
					badElements.removeAll(potentialSuperset);
					mismatchDescription.appendText("also found ").appendValue(badElements);
				}
				return matched;
			}
		};
	}
}
