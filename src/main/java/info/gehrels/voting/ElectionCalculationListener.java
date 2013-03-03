package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;

public interface ElectionCalculationListener {
	void quorumHasBeenCalculated(boolean b, double femaleQuorum);

	void numberOfElectedPositions(boolean female, int numberOfElectedCandidates, int numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<Candidate> electedCandidates);

	void candidateDropped(String name, double weakestVoteCount);

	void voteWeightRedistributed(double excessiveFractionOfVoteWeight,
	                             Ballot ballot, double voteWeight);

	void delegatingToExternalAmbiguityResolution(ImmutableSet<Candidate> bestCandidates);

	void externalyResolvedAmbiguity(AmbiguityResolverResult winner);

	void candidateIsElected(Candidate winner, double v, double femaleQuorum);
}
