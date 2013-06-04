package info.gehrels.voting;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class CandidateStateMatchers {
	static Matcher<CandidateStates> withLooser(final Candidate a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates>() {
			@Override
			protected boolean matchesSafely(CandidateStates candidateStates, Description description) {
				CandidateState candidateState = candidateStates.getCandidateState(a);
				if (candidateState == null) {
					description.appendText("candidate ").appendValue(a).appendText(" had no state");
					return false;
				}

				if (candidateState.isElected()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is elected");
					return false;
				}

				if (candidateState.isHopeful()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is still hopeful");
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with looser candidate ").appendValue(a);
			}
		};
	}

	static Matcher<CandidateStates> withElectedCandidate(final Candidate a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates>() {
			@Override
			protected boolean matchesSafely(CandidateStates candidateStates, Description description) {
				CandidateState candidateState = candidateStates.getCandidateState(a);
				if (candidateState == null) {
					description.appendText("candidate ").appendValue(a).appendText(" had no state");
					return false;
				}

				if (candidateState.isLooser()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is a looser");
					return false;
				}

				if (candidateState.isHopeful()) {
					description.appendText("candidate ").appendValue(a).appendText(" is is still hopeful");
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("with elected candidate ").appendValue(a);
			}
		};
	}
}
