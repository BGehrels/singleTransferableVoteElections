package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public final class BallotState {
	private final ImmutableList<Candidate> rankedCandidates;
	private final int ballotId;
	private final BigFraction voteWeight;
	private final int currentPositionInRankedCandidatesList;

	public BallotState(Ballot ballot, Election election) {
		validateThat(ballot, is(notNullValue()));
		validateThat(election, is(notNullValue()));

		this.ballotId = ballot.id;
		rankedCandidates = ballot.getRankedCandidatesByElection(election).asList();

		voteWeight = BigFraction.ONE;
		currentPositionInRankedCandidatesList = 0;
	}

	private BallotState(int ballotId, ImmutableList<Candidate> rankedCandidates, BigFraction voteWeight,
	                    int currentPositionInRankedCandidatesList) {
		this.ballotId = ballotId;
		this.rankedCandidates = rankedCandidates;
		this.voteWeight = voteWeight;
		this.currentPositionInRankedCandidatesList = currentPositionInRankedCandidatesList;
	}

	public BallotState withFirstHopefulCandidate(CandidateStates candidateStates) {
		BallotState result = this;

		Candidate preferredCandidate = result.getPreferredCandidate();
		while (preferredCandidate != null) {
			CandidateState candidateState = candidateStates.getCandidateState(preferredCandidate);
			if (candidateState != null && candidateState.isHopeful()) {
				return result;
			}

			result = result.withNextPreference();
			preferredCandidate = result.getPreferredCandidate();
		}

		return result;
	}

	public Candidate getPreferredCandidate() {
		if (currentPositionInRankedCandidatesList >= rankedCandidates.size()) {
			return null;
		}

		return rankedCandidates.asList().get(currentPositionInRankedCandidatesList);
	}

	public BigFraction getVoteWeight() {
		return voteWeight;
	}

	private BallotState withNextPreference() {
		return new BallotState(ballotId, rankedCandidates, voteWeight, currentPositionInRankedCandidatesList + 1);
	}

	public BallotState withVoteWeight(BigFraction newVoteWeight) {
		return new BallotState(ballotId, rankedCandidates, newVoteWeight, currentPositionInRankedCandidatesList);
	}

	public int getBallotId() {
		return ballotId;
	}

	@Override
	public String toString() {
		return "BallotState<" + this.ballotId + "; preferred: " + this.getPreferredCandidate() + "; " + this.getVoteWeight() + ">";
	}
}
