package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

public interface ElectionCalculationListener {
	void quorumHasBeenCalculated(boolean b, double femaleQuorum);

	void numberOfElectedPositions(boolean female, int numberOfElectedCandidates, int numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<Candidate> electedCandidates);
}
