package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.fraction.Fraction;

import java.util.Map;

public interface ElectionCalculationListener {
	void numberOfElectedPositions(int numberOfElectedCandidates, int numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<Candidate> electedCandidates);

	void candidateDropped(
		Map<Candidate,BigFraction> votesByCandidateBeforeStriking, String name, BigFraction weakestVoteCount,
	                      Map<Candidate, BigFraction> votesByCandidateAfterStriking);

	void voteWeightRedistributed(BigFraction excessiveFractionOfVoteWeight,
	                             int ballotId, BigFraction voteWeight);

	void voteWeightRedistributionCompleted(Map<Candidate, BigFraction> candidateDoubleMap);

	void delegatingToExternalAmbiguityResolution(ImmutableSet<Candidate> bestCandidates);

	void externalyResolvedAmbiguity(AmbiguityResolverResult winner);

	void candidateIsElected(Candidate winner, BigFraction v, BigFraction femaleQuorum);

	void nobodyReachedTheQuorumYet(BigFraction quorum);

	void noCandidatesAreLeft();

	void calculationStarted(Election election, Map<Candidate, BigFraction> candidateDoubleMap);

	void candidateNotQualified(Candidate candidate, String reason);

	void quorumHasBeenCalculated(int numberOfValidBallots, int numberOfSeats, BigFraction quorum);

	void reducedNonFemaleExclusiveSeats(int numberOfOpenFemaleExclusiveSeats, int numberOfElectedFemaleExclusiveSeats, int numberOfOpenNonFemaleExclusiveSeats, int numberOfElectableNonFemaleExclusiveSeats);
}
