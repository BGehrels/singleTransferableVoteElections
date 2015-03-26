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
package info.gehrels.voting;

import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum implements QuorumCalculation {

	private final BigFraction surplus;

	public NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(BigFraction surplus) {
		this.surplus = validateThat(surplus, is(greaterThan(BigFraction.ZERO)));
	}

	@Override
	public final BigFraction calculateQuorum(long numberOfValidVotes, long numberOfSeats) {
		BigFraction calculatedQuorum = new BigFraction(numberOfValidVotes, (numberOfSeats + 1)).add(surplus);
		return correctToBoundariesIfNecessary(numberOfValidVotes, numberOfSeats, calculatedQuorum);
	}

	private BigFraction correctToBoundariesIfNecessary(long numberOfValidVotes, long numberOfSeats,
                                                       BigFraction calculatedQuorum) {
		if ((numberOfValidVotes != 0) && (numberOfSeats != 0)) {
			return min(new BigFraction(numberOfValidVotes), calculatedQuorum);
		} else {
			return calculatedQuorum;
		}
	}

	private BigFraction min(BigFraction a, BigFraction b) {
		if (a.compareTo(b) < 0) {
			return a;
		} else {
			return b;
		}
	}


}
