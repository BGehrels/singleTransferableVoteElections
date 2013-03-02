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

public class HamcrestMatchers {
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
