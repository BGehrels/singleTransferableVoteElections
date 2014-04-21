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

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.Vote;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

final class VoteState<CANDIDATE_TYPE extends Candidate> {
	private final ImmutableList<CANDIDATE_TYPE> rankedCandidates;
	private final long ballotId;
	private final BigFraction voteWeight;
	private final int currentPositionInRankedCandidatesList;
	private final Vote<CANDIDATE_TYPE> vote;

	public static <CANDIDATE_TYPE extends Candidate> Optional<VoteState<CANDIDATE_TYPE>> forBallotAndElection(
		Ballot<CANDIDATE_TYPE> ballot, Election<CANDIDATE_TYPE> election) {
		validateThat(ballot, is(notNullValue()));
		validateThat(election, is(notNullValue()));

		Optional<Vote<CANDIDATE_TYPE>> vote = ballot.getVote(election);
		if (!vote.isPresent()) {
			return Optional.absent();
		}

		return Optional.of(new VoteState<>(ballot.id, vote.get()));
	}

	private VoteState(long ballotId, Vote<CANDIDATE_TYPE> vote) {
		this(ballotId, vote, BigFraction.ONE, 0);
	}

	private VoteState(long ballotId, Vote<CANDIDATE_TYPE> vote, BigFraction voteWeight,
	                  int currentPositionInRankedCandidatesList) {
		this.ballotId = ballotId;
		this.vote = vote;
		this.rankedCandidates = vote.getRankedCandidates().asList();
		this.voteWeight = voteWeight;
		this.currentPositionInRankedCandidatesList = currentPositionInRankedCandidatesList;
	}

	public VoteState<CANDIDATE_TYPE> withFirstHopefulCandidate(CandidateStates<CANDIDATE_TYPE> candidateStates) {
		VoteState<CANDIDATE_TYPE> result = this;

		Optional<CANDIDATE_TYPE> preferredCandidate = result.getPreferredCandidate();
		while (preferredCandidate.isPresent()) {
			CandidateState<CANDIDATE_TYPE> candidateState = candidateStates.getCandidateState(preferredCandidate.get());
			if ((candidateState != null) && candidateState.isHopeful()) {
				return result;
			}

			result = result.withNextPreference();
			preferredCandidate = result.getPreferredCandidate();
		}

		return result;
	}

	public Optional<CANDIDATE_TYPE> getPreferredCandidate() {
		if (currentPositionInRankedCandidatesList >= rankedCandidates.size()) {
			return Optional.absent();
		}

		return Optional.of(rankedCandidates.asList().get(currentPositionInRankedCandidatesList));
	}

	public BigFraction getVoteWeight() {
		return voteWeight;
	}

	private VoteState<CANDIDATE_TYPE> withNextPreference() {
		return new VoteState<>(ballotId, vote, voteWeight, currentPositionInRankedCandidatesList + 1);
	}

	public VoteState<CANDIDATE_TYPE> withVoteWeight(BigFraction newVoteWeight) {
		return new VoteState<>(ballotId, vote, newVoteWeight, currentPositionInRankedCandidatesList);
	}

	public long getBallotId() {
		return ballotId;
	}

	@Override
	public String toString() {
		return "VoteState<" + ballotId + "; preferred: " + getPreferredCandidate() + "; " + voteWeight + ">";
	}

	public boolean isInvalid() {
		return !vote.isValid();
	}

	public boolean isNoVote() {
		return vote.isNo() || !getPreferredCandidate().isPresent();
	}
}
