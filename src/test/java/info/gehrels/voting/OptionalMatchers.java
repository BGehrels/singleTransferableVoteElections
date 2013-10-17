package info.gehrels.voting;

import com.google.common.base.Optional;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class OptionalMatchers {
	static Matcher<Optional<?>> present() {
		return new TypeSafeDiagnosingMatcher<Optional<?>>() {
			@Override
			protected boolean matchesSafely(Optional<?> item, Description mismatchDescription) {
				if (item.isPresent()) {
					return true;
				}

				mismatchDescription.appendText("absent");
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("present");
			}
		};
	}

	public static Matcher<Optional<?>> anAbsentOptional() {
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

	public static <T> Matcher<Optional<T>> anOptionalWhoseValue(final Matcher<? super T> valueMatcher) {
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
