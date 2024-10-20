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

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;

public final class CollectionMatchers {
	public static  <SUPERSET extends Collection<?>, SUBSET extends Collection<?>> Matcher<SUBSET> isSubSetOf(SUPERSET potentialSuperset) {
		validateThat(potentialSuperset, is(not(nullValue())));

		return new TypeSafeDiagnosingMatcher<>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("a collection completely contained in ").appendValue(potentialSuperset);
            }

            @Override
            protected boolean matchesSafely(SUBSET potentialSubset, Description mismatchDescription) {
                boolean matched = potentialSuperset.containsAll(potentialSubset);
                if (!matched) {
                    Collection<Object> badElements = new ArrayList<>(potentialSubset);
                    badElements.removeAll(potentialSuperset);
                    mismatchDescription.appendText("also found ").appendValue(badElements);
                }
                return matched;
            }
        };
	}

	public static  <COLLECTION extends Collection<?>> Matcher<COLLECTION> hasOnlyDistinctElements() {
		return new TypeSafeDiagnosingMatcher<>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("a collection with only distinct elements");
            }

            @Override
            protected boolean matchesSafely(COLLECTION potentiallyDistinctCollection, Description mismatchDescription) {
                boolean matched = ImmutableSet.copyOf(potentiallyDistinctCollection).size() == potentiallyDistinctCollection.size();
                if (!matched) {
                    Collection<Object> badElements = ImmutableMultiset.copyOf(potentiallyDistinctCollection).entrySet().stream().filter(objectEntry -> objectEntry.getCount() > 1).collect(Collectors.toSet());
                    mismatchDescription.appendValue(badElements).appendText(" appeared more than once");
                }
                return matched;
            }
        };
	}
}
