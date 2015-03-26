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
import org.junit.Test;

import static org.apache.commons.math3.fraction.BigFraction.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorumTest {

	private static final BigFraction ONE_TENTH = new BigFraction(1, 10);

	@Test
	public void returnsCorrectQuorumForZeroValidVotesAndZeroPositions() {
		QuorumCalculation defaultQuorumCalculation
			= new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(0, 0);

		assertThat(quorum, is(ONE_TENTH));
	}

	@Test
	public void returnsCorrectQuorumForZeroValidVotesAndNonZeroPositions() {
		QuorumCalculation defaultQuorumCalculation
			= new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(0, 4);

		assertThat(quorum, is(ONE_TENTH));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroNumberOfValidVotesAndZeroPositions() {
		QuorumCalculation defaultQuorumCalculation
			= new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(4, 0);

		assertThat(quorum, is(new BigFraction(41, 10)));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroNumberOfValidVotesAndNonZeroNumberOfPositions() {
		QuorumCalculation defaultQuorumCalculation = new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(
			ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(6, 2);

		assertThat(quorum, is(new BigFraction(21, 10)));
	}

	@Test
	public void quorumMayNotBeHigherThanNumberOfValidVotesIfThereArePositionsToElect() {
		QuorumCalculation quorumCalculation = new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE);
		BigFraction quorum = quorumCalculation.calculateQuorum(1, 1);
		assertThat(quorum, is(ONE));
	}


}
