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

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.DelegatingMatcher;
import info.gehrels.voting.singleTransferableVote.STVElectionCalculationStep.ElectionStepResult;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

public final class ElectionStepResultMatchers {
	private ElectionStepResultMatchers() {
	}

	static Matcher<ElectionStepResult<?>> withNumberOfElectedCandidates(Matcher<Long> subMatcher) {
		return new FeatureMatcher<ElectionStepResult<?>, Long>(subMatcher, "whose number of elected candidates",
		                                                          "number of elected candidates") {

			@Override
			protected Long featureValueOf(ElectionStepResult<?> electionStepResult) {
				return electionStepResult.newNumberOfElectedCandidates;
			}
		};
	}

	static Matcher<ElectionStepResult<Candidate>> withVoteStates(
		Matcher<? super ImmutableCollection<VoteState<Candidate>>> subMatcher) {
		return new FeatureMatcher<ElectionStepResult<Candidate>,ImmutableCollection<VoteState<Candidate>>>(
			subMatcher,
			"with ballot states",
			"ballot states"
		) {
			@Override
			protected ImmutableCollection<VoteState<Candidate>> featureValueOf(
				ElectionStepResult<Candidate> electionStepResult) {
				return electionStepResult.newVoteStates;
			}
		};
	}

	static <CANDIDATE_TYPE extends Candidate> Matcher<ElectionStepResult<CANDIDATE_TYPE>> withCandidateStates(
		final Matcher<CandidateStates<CANDIDATE_TYPE>> candidateStatesMatcher) {
		return new FeatureMatcher<ElectionStepResult<CANDIDATE_TYPE>, CandidateStates<CANDIDATE_TYPE>>(
			candidateStatesMatcher, "with candidateStates",
			"") {
			@Override
			protected CandidateStates<CANDIDATE_TYPE> featureValueOf(ElectionStepResult<CANDIDATE_TYPE> actual) {
				return actual.newCandidateStates;
			}
		};
	}

	static <T extends Candidate> Matcher<ElectionStepResult<T>> anElectionStepResult(Matcher<ElectionStepResult<T>> subMatcher) {
		return new DelegatingMatcher<>(subMatcher, "an election step result");
	}
}
