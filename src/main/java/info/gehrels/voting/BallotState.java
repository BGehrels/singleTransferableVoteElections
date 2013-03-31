package info.gehrels.voting;

import com.google.common.collect.ImmutableList;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public final class BallotState implements Cloneable {
	public final ImmutableList<Candidate> rankedCandidatesByElection;
	public final Ballot ballot;
	private double voteWeight;
	private int currentPositionInRankedCandidatesList = 0;

	public BallotState(Ballot ballot, Election election) {
		validateThat(ballot, is(notNullValue()));
		validateThat(election, is(notNullValue()));

		this.ballot = ballot;
		rankedCandidatesByElection = ballot.getRankedCandidatesByElection(election).asList();

		voteWeight = 1;
	}

	public Candidate getPreferredCandidate() {
		if (currentPositionInRankedCandidatesList >= rankedCandidatesByElection.size()) {
			return null;
		}

		return rankedCandidatesByElection.asList().get(currentPositionInRankedCandidatesList);
	}

	public double getVoteWeight() {
		return voteWeight;
	}

	public BallotState withNextPreference() {
		BallotState result = this.clone();
		result.currentPositionInRankedCandidatesList++;
		return result;
	}

	public BallotState withReducedVoteWeight(double fractionOfExcessiveVotes) {
		BallotState result = this.clone();
		result.voteWeight *= fractionOfExcessiveVotes;
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
