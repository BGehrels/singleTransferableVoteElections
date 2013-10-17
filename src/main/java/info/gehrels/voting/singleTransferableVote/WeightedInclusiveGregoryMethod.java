package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.voting.singleTransferableVote.VotesForCandidateCalculation.calculateVotesForCandidate;

public class WeightedInclusiveGregoryMethod<CANDIDATE_TYPE extends Candidate> implements
	VoteWeightRedistributionMethod<CANDIDATE_TYPE> {
	private final STVElectionCalculationListener<?> electionCalculationListener;

	public WeightedInclusiveGregoryMethod(STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public final VoteWeightRedistributor<CANDIDATE_TYPE> redistributorFor() {
		return new WigmVoteWeightRedistributor<CANDIDATE_TYPE>();
	}

	private final class WigmVoteWeightRedistributor<CANDIDATE_TYPE extends Candidate> implements VoteWeightRedistributor<CANDIDATE_TYPE> {
		@Override
		public ImmutableList<VoteState<CANDIDATE_TYPE>> redistributeExceededVoteWeight(CANDIDATE_TYPE winner, BigFraction quorum,
		                                                                       ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates) {
			Builder<VoteState<CANDIDATE_TYPE>> resultBuilder = ImmutableList.builder();

			BigFraction votesForCandidate = calculateVotesForCandidate(winner, voteStates);
			BigFraction excessiveVotes = votesForCandidate.subtract(quorum);
			BigFraction excessiveFractionOfVoteWeight = excessiveVotes.divide(votesForCandidate);

			for (VoteState<CANDIDATE_TYPE> voteState : voteStates) {
				if (voteState.getPreferredCandidate().orNull() == winner) {
					BigFraction newVoteWeight = voteState.getVoteWeight().multiply(excessiveFractionOfVoteWeight);
					VoteState<CANDIDATE_TYPE> newVoteState = voteState.withVoteWeight(newVoteWeight);
					electionCalculationListener.voteWeightRedistributed(excessiveFractionOfVoteWeight,
					                                                    newVoteState.getBallotId(),
					                                                    newVoteState.getVoteWeight());
					resultBuilder.add(newVoteState);
				} else {
					resultBuilder.add(voteState);
				}
			}

			return resultBuilder.build();

		}

	}
}
