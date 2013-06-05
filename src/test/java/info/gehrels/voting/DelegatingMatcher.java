package info.gehrels.voting;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class DelegatingMatcher<T> extends TypeSafeDiagnosingMatcher<T> {
	private final Matcher<? super T> subMatcher;
	private String descriptionText;

	public DelegatingMatcher(Matcher<? super T> subMatcher, String descriptionText) {
		this.subMatcher = subMatcher;
		this.descriptionText = descriptionText;
	}

	@Override
	protected boolean matchesSafely(T item, Description mismatchDescription) {
		if (!subMatcher.matches(item)) {
			subMatcher.describeMismatch(item, mismatchDescription);
			return false;
		}

		return true;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText(descriptionText + " ").appendDescriptionOf(subMatcher);
	}
}
