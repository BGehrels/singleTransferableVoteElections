package info.gehrels.voting.singleTransferableVote;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

final class BallotState<CANDIDATE_TYPE extends Candidate> {
	private final ImmutableList<CANDIDATE_TYPE> rankedCandidates;
	private final int ballotId;
	private final BigFraction voteWeight;
	private final int currentPositionInRankedCandidatesList;

	BallotState(Ballot<CANDIDATE_TYPE> ballot, Election<CANDIDATE_TYPE> election) {
		validateThat(ballot, is(notNullValue()));
		validateThat(election, is(notNullValue()));

		ballotId = ballot.id;
		rankedCandidates = ballot.getRankedCandidatesByElection(election).asList();

		voteWeight = BigFraction.ONE;
		currentPositionInRankedCandidatesList = 0;
	}

	private BallotState(int ballotId, ImmutableList<CANDIDATE_TYPE> rankedCandidates, BigFraction voteWeight,
	                    int currentPositionInRankedCandidatesList) {
		this.ballotId = ballotId;
		this.rankedCandidates = rankedCandidates;
		this.voteWeight = voteWeight;
		this.currentPositionInRankedCandidatesList = currentPositionInRankedCandidatesList;
	}

	public BallotState<CANDIDATE_TYPE> withFirstHopefulCandidate(CandidateStates<CANDIDATE_TYPE> candidateStates) {
		BallotState<CANDIDATE_TYPE> result = this;

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

	private BallotState<CANDIDATE_TYPE> withNextPreference() {
		return new BallotState<>(ballotId, rankedCandidates, voteWeight, currentPositionInRankedCandidatesList + 1);
	}

	public BallotState<CANDIDATE_TYPE> withVoteWeight(BigFraction newVoteWeight) {
		return new BallotState<>(ballotId, rankedCandidates, newVoteWeight, currentPositionInRankedCandidatesList);
	}

	public int getBallotId() {
		return ballotId;
	}

	@Override
	public String toString() {
		return "BallotState<" + ballotId + "; preferred: " + getPreferredCandidate() + "; " + voteWeight + ">";
	}
}
