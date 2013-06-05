package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;
import info.gehrels.voting.singleTransferableVote.CandidateState;
import info.gehrels.voting.singleTransferableVote.CandidateStates;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class CandidateStateMatchers {
	static <CANDIDATE_TYPE extends Candidate> Matcher<CandidateStates<CANDIDATE_TYPE>> withLooser(final CANDIDATE_TYPE a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates<CANDIDATE_TYPE>>() {
			@Override
			protected boolean matchesSafely(CandidateStates<CANDIDATE_TYPE> candidateStates, Description description) {
				CandidateState<CANDIDATE_TYPE> candidateState = candidateStates.getCandidateState(a);
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

	static <CANDIDATE_TYPE>  Matcher<CandidateStates<CANDIDATE_TYPE>> withElectedCandidate(final CANDIDATE_TYPE a) {
		return new TypeSafeDiagnosingMatcher<CandidateStates<CANDIDATE_TYPE>>() {
			@Override
			protected boolean matchesSafely(CandidateStates<CANDIDATE_TYPE> candidateStates, Description description) {
				CandidateState<CANDIDATE_TYPE> candidateState = candidateStates.getCandidateState(a);
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

	static <CANDIDATE_TYPE> Matcher<CandidateState<CANDIDATE_TYPE>> candidateStateFor(final CANDIDATE_TYPE candidate) {
		return new TypeSafeDiagnosingMatcher<CandidateState<CANDIDATE_TYPE>>() {
			@Override
			protected boolean matchesSafely(CandidateState<CANDIDATE_TYPE> candidateState, Description description) {
				if (candidateState.getCandidate() != candidate) {
					description.appendText("a candidate state for ").appendValue(candidateState);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("a candidateState for ").appendValue(candidate);
			}
		};
	}
}
