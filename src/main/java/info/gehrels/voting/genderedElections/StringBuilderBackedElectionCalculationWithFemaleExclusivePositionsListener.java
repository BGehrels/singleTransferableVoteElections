package info.gehrels.voting.genderedElections;

import static java.lang.String.format;

public final class StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener
	implements ElectionCalculationWithFemaleExclusivePositionsListener {
	private final StringBuilder builder;

	public StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener(StringBuilder builder) {
		this.builder = builder;
	}

	// TODO: Am Anfang einmal Die Anzahl der Abgegebenen, Gültigen, ungültigen, Nein-Stimmen und Präferenz-Stimmen ausgeben
	@Override
	public void reducedNonFemaleExclusiveSeats(long numberOfOpenFemaleExclusiveSeats,
	                                           long numberOfElectedFemaleExclusiveSeats,
	                                           long numberOfOpenNonFemaleExclusiveSeats,
	                                           long numberOfElectableNonFemaleExclusiveSeats) {
		formatLine(
			"Es wurden nur %d von %d Frauenplätzen besetzt. Daher können auch nur %d von %d offenen Plätzen gewählt werden.",
			numberOfElectedFemaleExclusiveSeats, numberOfOpenFemaleExclusiveSeats,
			numberOfElectableNonFemaleExclusiveSeats, numberOfOpenNonFemaleExclusiveSeats);
	}

	@Override
	public void candidateNotQualified(GenderedCandidate candidate, String reason) {
		formatLine("%s kann in diesem Wahlgang nicht antreten, Grund: %s", candidate.name, reason);
	}

	private StringBuilder formatLine(String formatString, Object... objects) {
		return builder.append(format(formatString, objects)).append('\n');
	}
}
