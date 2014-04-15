/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.Candidate;
import info.gehrels.voting.DelegatingMatcher;
import org.apache.commons.math3.fraction.BigFraction;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public final class VoteStateMatchers {
	private VoteStateMatchers() {
	}

	static <CANDIDATE_TYPE extends Candidate> Matcher<VoteState<CANDIDATE_TYPE>> withPreferredCandidate(Matcher<? super CANDIDATE_TYPE> subMatcher) {
		return new FeatureMatcher<VoteState<CANDIDATE_TYPE>, CANDIDATE_TYPE>(subMatcher, "with preferred candidate",
		                                                  "prefered candidate") {
			@Override
			protected CANDIDATE_TYPE featureValueOf(VoteState<CANDIDATE_TYPE> actual) {
				return actual.getPreferredCandidate().orNull();
			}
		};
	}

	static Matcher<VoteState<?>> withVoteWeight(Matcher<? super BigFraction> bigFractionMatcher) {
		return new FeatureMatcher<VoteState<?>, BigFraction>(bigFractionMatcher, "with vote weight", "vote Weight") {
			@Override
			protected BigFraction featureValueOf(VoteState<?> actual) {
				return actual.getVoteWeight();
			}
		};
	}

	static <T extends Candidate> Matcher<VoteState<T>> aVoteState(Matcher<VoteState<T>> stateMatcher) {
		return new DelegatingMatcher<>(stateMatcher, "a ballot state");
	}

	static Matcher<VoteState<?>> withVoteWeight(BigFraction voteWeight) {
		return new FeatureMatcher<VoteState<?>, BigFraction>(is(equalTo(voteWeight)), "with vote weight",
		                                                    "vote weight") {

			@Override
			protected BigFraction featureValueOf(VoteState<?> voteState) {
				return voteState.getVoteWeight();
			}
		};
	}

	static Matcher<VoteState<?>> withBallotId(long ballotId) {
		return new FeatureMatcher<VoteState<?>, Long>(is(ballotId), "with ballot id", "ballot id") {

			@Override
			protected Long featureValueOf(VoteState<?> voteState) {
				return voteState.getBallotId();
			}
		};
	}
}
