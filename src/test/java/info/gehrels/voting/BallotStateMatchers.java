package info.gehrels.voting;

import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public class BallotStateMatchers {
	static Matcher<BallotState> aBallotState(Matcher<? super BallotState> stateMatcher) {
		return new DelegatingMatcher<>(stateMatcher, "a ballot state");
	}

	static Matcher<BallotState> withPreferredCandidate(Matcher<? super Candidate> subMatcher) {
		return new FeatureMatcher<BallotState, Candidate>(subMatcher, "with preferred candidate",
		                                                  "prefered candidate") {
			@Override
			protected Candidate featureValueOf(BallotState actual) {
				return actual.getPreferredCandidate();
			}
		};
	}

	static Matcher<? super BallotState> withVoteWeight(Matcher<? super BigFraction> bigFractionMatcher) {
		return new FeatureMatcher<BallotState, BigFraction>(bigFractionMatcher, "with vote weight", "vote Weight") {
			@Override
			protected BigFraction featureValueOf(BallotState actual) {
				return actual.getVoteWeight();
			}
		};
	}
}
