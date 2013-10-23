package info.gehrels.voting.genderedElections;

public interface ElectionCalculationWithFemaleExclusivePositionsListener {


	void reducedNonFemaleExclusiveSeats(long numberOfOpenFemaleExclusiveSeats,
	                                    long numberOfElectedFemaleExclusiveSeats,
	                                    long numberOfOpenNonFemaleExclusiveSeats,
	                                    long numberOfElectableNonFemaleExclusiveSeats);


	void candidateNotQualified(GenderedCandidate candidate, String reason);

}
