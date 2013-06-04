package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.STVElectionCalculationStep.ElectionStepResult;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

public class ElectionStepResultMatchers {
	static Matcher<ElectionStepResult> withNumberOfElectedCandidates(Matcher<Integer> subMatcher) {
		return new FeatureMatcher<ElectionStepResult, Integer>(subMatcher, "with number of elected candidates",
		                                                       "number of elected candidates") {

			@Override
			protected Integer featureValueOf(ElectionStepResult electionStepResult) {
				return electionStepResult.newNumberOfElectedCandidates;
			}
		};
	}

	static Matcher<? super ElectionStepResult> withBallotStates(
		Matcher<? super ImmutableCollection<BallotState>> subMatcher) {
		return new FeatureMatcher<ElectionStepResult, ImmutableCollection<BallotState>>(subMatcher,
		                                                                                "with ballot states",
		                                                                                "ballot states") {

			@Override
			protected ImmutableCollection<BallotState> featureValueOf(ElectionStepResult electionStepResult) {
				return electionStepResult.newBallotStates;
			}
		};
	}

	static FeatureMatcher<ElectionStepResult, CandidateStates> withCandidateStates(
		final Matcher<CandidateStates> candidateStatesMatcher) {
		return new FeatureMatcher<ElectionStepResult, CandidateStates>(candidateStatesMatcher, "with candidateStates",
		                                                               "") {
			@Override
			protected CandidateStates featureValueOf(ElectionStepResult actual) {
				return actual.newCandidateStates;
			}
		};
	}

	static TypeSafeDiagnosingMatcher<ElectionStepResult> anElectionStepResult(
		final Matcher<ElectionStepResult> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "an election step result");
	}
}
