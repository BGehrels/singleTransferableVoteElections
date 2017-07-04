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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Optional;

public final class OptionalMatchers {
	public static Matcher<Optional<?>> anEmptyOptional() {
		return new TypeSafeDiagnosingMatcher<Optional<?>>() {
			@Override
			protected boolean matchesSafely(Optional<?> item, Description mismatchDescription) {
				if (item.isPresent()) {
					mismatchDescription.appendText("an Optional whose value was ").appendValue(item.get());
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an absent Optional");
			}
		};
	}

	public static <T> Matcher<Optional<T>> anOptionalWhoseValue(Matcher<? super T> valueMatcher) {
		return new TypeSafeDiagnosingMatcher<Optional<T>>() {
			@Override
			protected boolean matchesSafely(Optional<T> optional, Description mismatchDescription) {
				if (!optional.isPresent()) {
					mismatchDescription.appendText("an absent Optional");
					return false;
				}

				T value = optional.get();
				if (!valueMatcher.matches(value)) {
					mismatchDescription.appendText("an Optional whose value ");
					valueMatcher.describeMismatch(value, mismatchDescription);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an Optional whose value ").appendDescriptionOf(valueMatcher);
			}
		};
	}
}
