package info.gehrels.voting;

import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapMatchers {
	static <K, V> Matcher<Entry<? extends K, ? extends V>> anEntry(final K key, final V value) {
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

	static <K, V> Matcher<Map<K, V>> aMap(Matcher<Map<K, V>> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "a Map");
	}

	static <K, V> FeatureMatcher<Map<K, V>, Set<Entry<K, V>>> withEntries(
		Matcher<? super Set<Entry<K, V>>> subMatcher) {
		return new FeatureMatcher<Map<K, V>, Set<Entry<K, V>>>(subMatcher, "with entries", "entries") {
			@Override
			protected Set<Entry<K, V>> featureValueOf(Map<K, V> actual) {
				return actual.entrySet();
			}
		};
	}
}
