package info.gehrels.voting;

public interface ElectionCalculationWithFemaleExclusivePositionsListener {


	void reducedNonFemaleExclusiveSeats(int numberOfOpenFemaleExclusiveSeats, int numberOfElectedFemaleExclusiveSeats,
	                                    int numberOfOpenNonFemaleExclusiveSeats,
	                                    int numberOfElectableNonFemaleExclusiveSeats);


	void candidateNotQualified(GenderedCandidate candidate, String reason);

}
