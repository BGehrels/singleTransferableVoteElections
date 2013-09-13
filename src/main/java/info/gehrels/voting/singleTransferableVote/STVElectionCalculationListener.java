package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.Map;

public interface STVElectionCalculationListener<CANDIDATE_TYPE extends Candidate> {
	void numberOfElectedPositions(int numberOfElectedCandidates, int numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<CANDIDATE_TYPE> electedCandidates);

	void candidateDropped(
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateBeforeStriking, CANDIDATE_TYPE candidate,
		BigFraction weakestVoteCount,
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidateAfterStriking);

	void voteWeightRedistributed(BigFraction excessiveFractionOfVoteWeight,
	                             int ballotId, BigFraction voteWeight);

	void voteWeightRedistributionCompleted(Map<CANDIDATE_TYPE, BigFraction> votesByCandidate);

	void delegatingToExternalAmbiguityResolution(ImmutableSet<CANDIDATE_TYPE> bestCandidates);

	void externalyResolvedAmbiguity(AmbiguityResolverResult<CANDIDATE_TYPE> ambiguityResolverResult);

	void candidateIsElected(CANDIDATE_TYPE winner, BigFraction numberOfVotes, BigFraction quorum);

	void nobodyReachedTheQuorumYet(BigFraction quorum);

	void noCandidatesAreLeft();

	void calculationStarted(Election<CANDIDATE_TYPE> election, Map<CANDIDATE_TYPE, BigFraction> votesByCandidate);

	void quorumHasBeenCalculated(int numberOfValidBallots, int numberOfSeats, BigFraction quorum);
}