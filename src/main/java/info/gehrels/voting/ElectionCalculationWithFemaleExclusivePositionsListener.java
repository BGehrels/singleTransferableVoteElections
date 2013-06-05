package info.gehrels.voting;

public interface ElectionCalculationWithFemaleExclusivePositionsListener extends ElectionCalculationListener<GenderedCandidate> {


	void reducedNonFemaleExclusiveSeats(int numberOfOpenFemaleExclusiveSeats, int numberOfElectedFemaleExclusiveSeats,
	                                    int numberOfOpenNonFemaleExclusiveSeats,
	                                    int numberOfElectableNonFemaleExclusiveSeats);


	void candidateNotQualified(GenderedCandidate candidate, String reason);

}
