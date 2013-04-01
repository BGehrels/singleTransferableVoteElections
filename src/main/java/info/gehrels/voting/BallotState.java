package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public final class BallotState implements Cloneable {
	public final ImmutableList<Candidate> rankedCandidatesByElection;
	public final Ballot ballot;
	private BigFraction voteWeight;
	private int currentPositionInRankedCandidatesList = 0;

	public BallotState(Ballot ballot, Election election) {
		validateThat(ballot, is(notNullValue()));
		validateThat(election, is(notNullValue()));

		this.ballot = ballot;
		rankedCandidatesByElection = ballot.getRankedCandidatesByElection(election).asList();

		voteWeight = BigFraction.ONE;
	}

	public Candidate getPreferredCandidate() {
		if (currentPositionInRankedCandidatesList >= rankedCandidatesByElection.size()) {
			return null;
		}

		return rankedCandidatesByElection.asList().get(currentPositionInRankedCandidatesList);
	}

	public BigFraction getVoteWeight() {
		return voteWeight;
	}

	public BallotState withNextPreference() {
		BallotState result = this.clone();
		result.currentPositionInRankedCandidatesList++;
		return result;
	}

	BallotState withVoteWeight(BigFraction newVoteWeight) {
		BallotState result = this.clone();
		result.voteWeight = newVoteWeight;
		return result;
	}

	@Override
	protected BallotState clone() {
		try {
			return (BallotState) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}
}
