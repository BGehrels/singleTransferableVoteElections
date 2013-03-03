package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;

import java.util.Map;

public interface ElectionCalculationListener {
	void quorumHasBeenCalculated(boolean b, double femaleQuorum);

	void numberOfElectedPositions(boolean female, int numberOfElectedCandidates, int numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<Candidate> electedCandidates);

	void candidateDropped(Map<Candidate, Double> votesByCandidateBeforeStriking, String name, double weakestVoteCount,
	                      Map<Candidate, Double> votesByCandidateAfterStriking);

	void voteWeightRedistributed(double excessiveFractionOfVoteWeight,
	                             Ballot ballot, double voteWeight);

	void voteWeightRedistributionCompleted(Map<Candidate, Double> candidateDoubleMap);

	void delegatingToExternalAmbiguityResolution(ImmutableSet<Candidate> bestCandidates);

	void externalyResolvedAmbiguity(AmbiguityResolverResult winner);

	void candidateIsElected(Candidate winner, double v, double femaleQuorum);

	void nobodyReachedTheQuorumYet(double quorum, Map<Candidate, Double> votesByCandidate);

	void noCandidatesAreLeft();

	void calculationStarted(boolean b, Election election, Map<Candidate, Double> candidateDoubleMap);
}
