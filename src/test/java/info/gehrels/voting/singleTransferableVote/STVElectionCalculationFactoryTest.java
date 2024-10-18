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
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.QuorumCalculation;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public final class STVElectionCalculationFactoryTest {
	private final QuorumCalculation quorumCalculation = mock(QuorumCalculation.class);
	private final STVElectionCalculationListener<Candidate> electionCalculationListener = mock(STVElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolver = mock(AmbiguityResolver.class);

	@Test
	public void returnsASTVElectionCalculationInstance() {
		STVElectionCalculationFactory<Candidate> factoryUnderTest =
			new STVElectionCalculationFactory<>(quorumCalculation, electionCalculationListener, ambiguityResolver);

		ImmutableCollection<Ballot<Candidate>> candidates= ImmutableList.of();
		Election<Candidate> election = mock(Election.class);
		STVElectionCalculation<Candidate> electionCalculation = factoryUnderTest.createElectionCalculation(election, candidates);

		assertThat(electionCalculation, is(notNullValue()));

	}
}
