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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.QuorumCalculation;
import org.junit.jupiter.api.Test;

import static info.gehrels.voting.Vote.createInvalidVote;
import static info.gehrels.voting.Vote.createNoVote;
import static info.gehrels.voting.Vote.createPreferenceVote;
import static org.apache.commons.math3.fraction.BigFraction.TWO;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class STVElectionCalculationTest {
	private static final Candidate CANDIDATE_1_A = new Candidate("1a");
	private static final Candidate CANDIDATE_1_B = new Candidate("1b");
	private static final Candidate CANDIDATE_2_A = new Candidate("2a");
	private static final Election<Candidate> ELECTION_1 = new Election<>("office1",
	                                                                     ImmutableSet.of(CANDIDATE_1_A, CANDIDATE_1_B));
	private static final Election<Candidate> ELECTION_2 = new Election<>("office2", ImmutableSet.of(CANDIDATE_2_A));

	private final QuorumCalculation quorumCalculationMock = mock(QuorumCalculation.class);
	private final STVElectionCalculationListener<Candidate> electionCalculationListenerMock =
		mock(STVElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolverMock =
		mock(AmbiguityResolver.class);
	private final VoteWeightRecalculationMethod<Candidate> redistributionMethodMock =
		mock(VoteWeightRecalculationMethod.class);

	@Test
	public void callsQuorumCalculatorWithTheCorrectParameters() {
		doReturn(TWO).when(quorumCalculationMock).calculateQuorum(anyLong(), anyLong());

		new STVElectionCalculation<>(setupBallotsFixture(), quorumCalculationMock, electionCalculationListenerMock,
		                             ELECTION_1, ambiguityResolverMock, redistributionMethodMock)
			.calculate(ImmutableSet.of(CANDIDATE_1_A), 2);

		verify(quorumCalculationMock).calculateQuorum(3, 2);
	}

	@Test
	public void callsListenerWithCorrectValuesAfterQuorumCalculation() {
		doReturn(TWO).when(quorumCalculationMock).calculateQuorum(anyLong(), anyLong());

		new STVElectionCalculation<>(setupBallotsFixture(), quorumCalculationMock, electionCalculationListenerMock,
		                             ELECTION_1, ambiguityResolverMock, redistributionMethodMock)
			.calculate(ImmutableSet.of(CANDIDATE_1_A), 2);

		verify(electionCalculationListenerMock).quorumHasBeenCalculated(3, 2, TWO);
	}

	// TODO: Dieser Test ist als White Box Test noch deutlich ausbaubar
    // TODO: Aufruf von electionCalculationListener.calculationStarted,
    // TODO: electionCalculationListener.electedCandidates(electedCandidates),
    // TODO: electionCalculationListener.numberOfElectedPositions,
    // TODO: electionCalculationListener.noCandidatesAreLeft() testen

	private ImmutableList<Ballot<Candidate>> setupBallotsFixture() {
		return ImmutableList.of(
			new Ballot<>(1, ImmutableSet.of(createPreferenceVote(ELECTION_2, ImmutableList.of(CANDIDATE_2_A)))),
			new Ballot<>(2, ImmutableSet.of(createInvalidVote(ELECTION_1))),
			new Ballot<>(3, ImmutableSet.of(createPreferenceVote(ELECTION_1, ImmutableList.of(CANDIDATE_1_A)))),
			new Ballot<>(4, ImmutableSet.of(createPreferenceVote(ELECTION_1, ImmutableList.of(CANDIDATE_1_B)))),
			new Ballot<>(5, ImmutableSet.of(createNoVote(ELECTION_1)))
		);
	}


}
