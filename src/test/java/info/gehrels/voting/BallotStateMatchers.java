package info.gehrels.voting;

import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class BallotStateMatchers {
	static <CANDIDATE_TYPE extends Candidate> Matcher<BallotState<CANDIDATE_TYPE>> withPreferredCandidate(Matcher<? super CANDIDATE_TYPE> subMatcher) {
		return new FeatureMatcher<BallotState<CANDIDATE_TYPE>, CANDIDATE_TYPE>(subMatcher, "with preferred candidate",
		                                                  "prefered candidate") {
			@Override
			protected CANDIDATE_TYPE featureValueOf(BallotState<CANDIDATE_TYPE> actual) {
				return actual.getPreferredCandidate();
			}
		};
	}

	static Matcher<BallotState<?>> withVoteWeight(Matcher<? super BigFraction> bigFractionMatcher) {
		return new FeatureMatcher<BallotState<?>, BigFraction>(bigFractionMatcher, "with vote weight", "vote Weight") {
			@Override
			protected BigFraction featureValueOf(BallotState<?> actual) {
				return actual.getVoteWeight();
			}
		};
	}

	static <T extends Candidate> Matcher<BallotState<T>> aBallotState(Matcher<BallotState<T>> stateMatcher) {
		return new DelegatingMatcher<>(stateMatcher, "a ballot state");
	}

	static Matcher<BallotState<?>> withVoteWeight(BigFraction voteWeight) {
		return new FeatureMatcher<BallotState<?>, BigFraction>(is(equalTo(voteWeight)), "with vote weight",
		                                                    "vote weight") {

			@Override
			protected BigFraction featureValueOf(BallotState<?> ballotState) {
				return ballotState.getVoteWeight();
			}
		};
	}

	static Matcher<BallotState<?>> withBallotId(int ballotId) {
		return new FeatureMatcher<BallotState<?>, Integer>(is(ballotId), "with ballot id", "ballot id") {

			@Override
			protected Integer featureValueOf(BallotState ballotState) {
				return ballotState.getBallotId();
			}
		};
	}
}
