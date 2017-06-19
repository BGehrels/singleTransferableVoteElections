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

public interface ElectionCalculationWithFemaleExclusivePositionsListener {


	void reducedNonFemaleExclusiveSeats(long numberOfOpenFemaleExclusiveSeats,
	                                    long numberOfElectedFemaleExclusiveSeats,
	                                    long numberOfOpenNonFemaleExclusiveSeats,
	                                    long numberOfElectableNonFemaleExclusiveSeats);


	void candidateNotQualified(GenderedCandidate candidate, NonQualificationReason reason);

	void startElectionCalculation(GenderedElection election, ImmutableCollection<Ballot<GenderedCandidate>> ballots);

	void startFemaleExclusiveElectionRun();

	void startNonFemaleExclusiveElectionRun();

	enum NonQualificationReason {
		NOT_FEMALE, ALREADY_ELECTED
	}
}
