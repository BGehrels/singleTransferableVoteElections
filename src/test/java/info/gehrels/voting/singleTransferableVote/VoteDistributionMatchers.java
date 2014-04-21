package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;
import info.gehrels.voting.DelegatingMatcher;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class VoteDistributionMatchers {
	public static Matcher<VoteDistribution<?>> aVoteDistribution(Matcher<VoteDistribution<?>>... subMatcher) {
		return new DelegatingMatcher<>(allOf(subMatcher), "a vote distribution");
	}

	public static Matcher<VoteDistribution<?>> withVotesForCandidate(final Candidate candidate, BigFraction numberOfVotes) {
		String featureDescription = "number of votes for " + candidate;
		return new FeatureMatcher<VoteDistribution<?>, BigFraction>(is(equalTo(numberOfVotes)), featureDescription, featureDescription) {

			@Override
			protected BigFraction featureValueOf(VoteDistribution<?> actual) {
				return actual.votesByCandidate.get(candidate);
			}
		};
	}

	public static Matcher<VoteDistribution<?>> withNoVotes(BigFraction numberOfVotes) {
		String featureDescription = "number of no votes";
		return new FeatureMatcher<VoteDistribution<?>, BigFraction>(is(equalTo(numberOfVotes)), featureDescription, featureDescription) {

			@Override
			protected BigFraction featureValueOf(VoteDistribution<?> actual) {
				return actual.noVotes;
			}
		};
	}
	public static Matcher<VoteDistribution<?>> withInvalidVotes(BigFraction numberOfVotes) {
		String featureDescription = "number of invalid votes";
		return new FeatureMatcher<VoteDistribution<?>, BigFraction>(is(equalTo(numberOfVotes)), featureDescription, featureDescription) {

			@Override
			protected BigFraction featureValueOf(VoteDistribution<?> actual) {
				return actual.invalidVotes;
			}
		};
	}


}
