/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
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
