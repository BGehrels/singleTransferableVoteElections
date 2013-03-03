package info.gehrels.voting;

public interface ElectionCalculationListener {
	void quorumHasBeenCalculated(boolean b, double femaleQuorum);

	void numberOfElectedPositions(boolean female, int numberOfElectedCandidates, int numberOfSeatsToElect);
}
