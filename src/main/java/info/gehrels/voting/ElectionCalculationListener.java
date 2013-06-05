package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.Map;

public interface ElectionCalculationListener<CANDIDATE_TYPE extends Candidate> {
	void numberOfElectedPositions(int numberOfElectedCandidates, int numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<CANDIDATE_TYPE> electedCandidates);

	void candidateDropped(
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateBeforeStriking, CANDIDATE_TYPE candidate,
		BigFraction weakestVoteCount,
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateAfterStriking);

	void voteWeightRedistributed(BigFraction excessiveFractionOfVoteWeight,
	                             int ballotId, BigFraction voteWeight);

	void voteWeightRedistributionCompleted(Map<CANDIDATE_TYPE, BigFraction> candidateDoubleMap);

	void delegatingToExternalAmbiguityResolution(ImmutableSet<CANDIDATE_TYPE> bestCandidates);

	void externalyResolvedAmbiguity(AmbiguityResolverResult<CANDIDATE_TYPE> winner);

	void candidateIsElected(CANDIDATE_TYPE winner, BigFraction numberOfVotes, BigFraction quorum);

	void nobodyReachedTheQuorumYet(BigFraction quorum);

	void noCandidatesAreLeft();

	void calculationStarted(Election<CANDIDATE_TYPE> election, Map<CANDIDATE_TYPE, BigFraction> candidateDoubleMap);

	void quorumHasBeenCalculated(int numberOfValidBallots, int numberOfSeats, BigFraction quorum);
}
