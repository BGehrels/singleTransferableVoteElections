package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class VoteDistributionMatchers {
	private VoteDistributionMatchers() {
	}

	@SafeVarargs
    public static <CANDIDATE extends Candidate> Matcher<VoteDistribution<CANDIDATE>> aVoteDistribution(Matcher<? super VoteDistribution<? super CANDIDATE>>... subMatcher) {
		return new DelegatingMatcher<>(allOf(subMatcher), "a vote distribution");
	}

	public static <CANDIDATE extends Candidate> Matcher<VoteDistribution<? super CANDIDATE>> withVotesForCandidate(CANDIDATE candidate, BigFraction numberOfVotes) {
		String featureDescription = "number of votes for " + candidate;
		return new FeatureMatcher<>(is(equalTo(numberOfVotes)), featureDescription, featureDescription) {

            @Override
            protected BigFraction featureValueOf(VoteDistribution<? super CANDIDATE> actual) {
                return actual.votesByCandidate.get(candidate);
            }
        };
	}

	public static Matcher<VoteDistribution<?>> withNoVotes(BigFraction numberOfVotes) {
		String featureDescription = "number of no votes";
		return new FeatureMatcher<>(is(equalTo(numberOfVotes)), featureDescription, featureDescription) {

            @Override
            protected BigFraction featureValueOf(VoteDistribution<?> actual) {
                return actual.noVotes;
            }
        };
	}
	public static Matcher<VoteDistribution<?>> withInvalidVotes(BigFraction numberOfVotes) {
		String featureDescription = "number of invalid votes";
		return new FeatureMatcher<>(is(equalTo(numberOfVotes)), featureDescription, featureDescription) {

            @Override
            protected BigFraction featureValueOf(VoteDistribution<?> actual) {
                return actual.invalidVotes;
            }
        };
	}


}
