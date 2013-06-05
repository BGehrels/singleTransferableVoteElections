package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.STVElectionCalculationStep.ElectionStepResult;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

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

	static Matcher<ElectionStepResult<Candidate>> withBallotStates(
		Matcher<? super ImmutableCollection<BallotState<Candidate>>> subMatcher) {
		return new FeatureMatcher<ElectionStepResult<Candidate>, ImmutableCollection<BallotState<Candidate>>>(
			subMatcher,
			"with ballot states",
			"ballot states") {

			@Override
			protected ImmutableCollection<BallotState<Candidate>> featureValueOf(
				ElectionStepResult electionStepResult) {
				return electionStepResult.newBallotStates;
			}
		};
	}

	static <CANDIDATE_TYPE> Matcher<ElectionStepResult<CANDIDATE_TYPE>> withCandidateStates(
		final Matcher<CandidateStates<CANDIDATE_TYPE>> candidateStatesMatcher) {
		return new FeatureMatcher<ElectionStepResult<CANDIDATE_TYPE>, CandidateStates<CANDIDATE_TYPE>>(candidateStatesMatcher, "with candidateStates",
		                                                              "") {
			@Override
			protected CandidateStates<CANDIDATE_TYPE> featureValueOf(ElectionStepResult<CANDIDATE_TYPE> actual) {
				return actual.newCandidateStates;
			}
		};
	}

	static <T extends Candidate> Matcher<ElectionStepResult<T>> anElectionStepResult(
		final Matcher<ElectionStepResult<T>> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "an election step result");
	}
}
