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
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapMatchers {
	public static <K, V> Matcher<Entry<? extends K, ? extends V>> anEntry(final K key, final V value) {
		return new TypeSafeDiagnosingMatcher<Entry<? extends K, ? extends V>>() {
			@Override
			protected boolean matchesSafely(Entry<? extends K, ? extends V> actual, Description mismatchDescription) {
				if (!actual.getKey().equals(key)) {
					mismatchDescription.appendText("key was ").appendValue(key);
					return false;
				}

				if (!actual.getValue().equals(value)) {
					mismatchDescription.appendText("value was ").appendValue(value);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("an entry ").appendValue(key + "=" + value);
			}
		};
	}

	public static <K, V> Matcher<Map<K, V>> aMap(Matcher<Map<K, V>> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "a Map");
	}

	public static <K, V> FeatureMatcher<Map<K, V>, Set<Entry<K, V>>> withEntries(
		Matcher<? super Set<Entry<K, V>>> subMatcher) {
		return new FeatureMatcher<Map<K, V>, Set<Entry<K, V>>>(subMatcher, "with entries", "entries") {
			@Override
			protected Set<Entry<K, V>> featureValueOf(Map<K, V> actual) {
				return actual.entrySet();
			}
		};
	}
}
