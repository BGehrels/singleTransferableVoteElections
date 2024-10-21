/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Vote;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

public final class StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener
	implements ElectionCalculationWithFemaleExclusivePositionsListener {
	private final StringBuilder builder;

	public StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener(StringBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void reducedNotFemaleExclusiveSeats(long numberOfOpenFemaleExclusiveSeats,
											   long numberOfElectedFemaleExclusiveSeats,
											   long numberOfOpenNotFemaleExclusiveSeats,
											   long numberOfElectableNotFemaleExclusiveSeats) {
		formatLine(
			"Es wurden nur %d von %d Frauenplätzen besetzt. Daher können auch nur %d von %d offenen Plätzen gewählt werden.",
			numberOfElectedFemaleExclusiveSeats, numberOfOpenFemaleExclusiveSeats,
				numberOfElectableNotFemaleExclusiveSeats, numberOfOpenNotFemaleExclusiveSeats);
	}

	@Override
	public void candidateNotQualified(GenderedCandidate candidate, NonQualificationReason reason) {
		formatLine("%s kann in diesem Wahlgang nicht antreten, Grund: %s", candidate.name,
		           getReasonAsGermanString(reason));
	}

	@Override
	public void startElectionCalculation(GenderedElection election,
	                                     ImmutableCollection<Ballot<GenderedCandidate>> ballots) {
		formatLine("Starte die Wahlberechnungen für %s.", election);
		formatLine("Abgegebene Stimmen:");
		List<Long> ballotIdsWithoutAVote = new ArrayList<>();
		long numberOfCastVotes = 0;
		for (Ballot<GenderedCandidate> ballot : ballots) {
			Optional<Vote<GenderedCandidate>> vote = ballot.getVote(election);
			if (vote.isPresent()) {
				formatLine("Stimmzettel %d: %s", ballot.id, vote.get());
				numberOfCastVotes++;
			} else {
				ballotIdsWithoutAVote.add(ballot.id);
			}
		}
		formatLine("Insgesamt wurden für diese Wahl %d Stimmen abgegeben.", numberOfCastVotes);

		if (!ballotIdsWithoutAVote.isEmpty()) {
			formatLine("Keine Stimmabgabe auf den Stimmzetteln %s.", ballotIdsWithoutAVote);
		}
	}

	@Override
	public void startFemaleExclusiveElectionRun() {
		formatLine("Starte die Berechnung der Frauenplätze.");
	}

	@Override
	public void startNotFemaleExclusiveElectionRun() {
		formatLine("Starte die Berechnung der offenen Plätze.");
	}

	private String getReasonAsGermanString(NonQualificationReason reason) {
        return switch (reason) {
            case NOT_FEMALE -> "Nicht weiblich";
            case ALREADY_ELECTED -> "Bereits gewählt";
        };

    }

	private void formatLine(String formatString, Object... objects) {
		builder.append(format(formatString, objects)).append('\n');
	}
}
