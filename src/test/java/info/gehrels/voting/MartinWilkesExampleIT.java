/*
 * Copyright © 2014 Martin Wilke, Benjamin Gehrels
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
package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.TestUtils.JustTakeTheFirstOneAmbiguityResolver;
import info.gehrels.voting.genderedElections.ElectionCalculationWithFemaleExclusivePositions;
import info.gehrels.voting.genderedElections.ElectionCalculationWithFemaleExclusivePositions.Result;
import info.gehrels.voting.genderedElections.GenderedCandidate;
import info.gehrels.voting.genderedElections.GenderedElection;
import info.gehrels.voting.genderedElections.StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener;
import info.gehrels.voting.singleTransferableVote.STVElectionCalculationFactory;
import info.gehrels.voting.singleTransferableVote.StringBuilderBackedSTVElectionCalculationListener;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static info.gehrels.voting.TestUtils.createBallot;
import static info.gehrels.voting.TestUtils.createInvalidBallot;
import static info.gehrels.voting.TestUtils.createNoBallot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public final class MartinWilkesExampleIT {
	private static final NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum QUORUM_CALCULATION = new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(
		new BigFraction(1, 1000));
	private static final AmbiguityResolver<GenderedCandidate> AMBIGUITY_RESOLVER = new JustTakeTheFirstOneAmbiguityResolver<>();

	private static final GenderedCandidate CANDIDATE_A = new GenderedCandidate("A", true);
	private static final GenderedCandidate CANDIDATE_B = new GenderedCandidate("B", false);
	private static final GenderedCandidate CANDIDATE_C = new GenderedCandidate("C", true);
	private static final GenderedCandidate CANDIDATE_D = new GenderedCandidate("D", false);
	private static final GenderedCandidate CANDIDATE_E = new GenderedCandidate("E", true);
	private static final GenderedCandidate CANDIDATE_F = new GenderedCandidate("F", false);
	private static final GenderedCandidate CANDIDATE_G = new GenderedCandidate("G", true);
	private static final GenderedCandidate CANDIDATE_H = new GenderedCandidate("H", false);
	private static final GenderedCandidate CANDIDATE_I = new GenderedCandidate("I", true);
	private static final GenderedCandidate CANDIDATE_J = new GenderedCandidate("J", false);

	private final ImmutableList<Ballot<GenderedCandidate>> ballotImmutableList;
	private final GenderedElection election;

	public MartinWilkesExampleIT() {
		ImmutableSet<GenderedCandidate> candidateSet = ImmutableSet.of(
			CANDIDATE_A,
			CANDIDATE_B,
			CANDIDATE_C,
			CANDIDATE_D,
			CANDIDATE_E,
			CANDIDATE_F,
			CANDIDATE_G,
			CANDIDATE_H,
			CANDIDATE_I,
			CANDIDATE_J);

		election = new GenderedElection("Example Office", 0, 4, candidateSet);

		ballotImmutableList = ImmutableList.of(
			createBallot("ABDC", election),  // 1
			createBallot("ACBDE", election), // 2
			createBallot("C", election),     // 3
			createBallot("CAE", election),   // 4
			createBallot("CBAFEDG", election),//5
			createBallot("CBDE", election),  // 6
			createBallot("CFBDEH", election),// 7
			createBallot("CDFEHA", election),// 8
			createBallot("DEC", election),   // 9
			createBallot("EBDCAF", election),// 10
			createBallot("EDCA", election),  // 11
			createBallot("F", election),     // 12
			createBallot("FCH", election),   // 13
			createBallot("FGEIHJ", election),// 14
			createBallot("FHG", election),   // 15
			createBallot("GFEI", election),  // 16
			createBallot("HFJAI", election), // 17
			createBallot("HGIF", election),  // 18
			createBallot("IJF", election),   // 19
			createBallot("IJH", election),   // 20
			createBallot("JIHFE", election), // 21
		    createNoBallot(election),        // 22
		    createInvalidBallot(election),   // 23
		    new Ballot<>(9876, ImmutableSet.<Vote<GenderedCandidate>>of())
		);
	}

	@Test
	public void exampleByMartinWilke() {
		StringBuilder builder = new StringBuilder();
		StringBuilderBackedSTVElectionCalculationListener<GenderedCandidate> listener =
			new StringBuilderBackedSTVElectionCalculationListener<>(builder);
		StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener =
			new StringBuilderBackedElectionCalculationWithFemaleExclusivePositionsListener(builder);

		ElectionCalculationWithFemaleExclusivePositions electionCalculation = new ElectionCalculationWithFemaleExclusivePositions(
			new STVElectionCalculationFactory<>(QUORUM_CALCULATION, listener, AMBIGUITY_RESOLVER),
			electionCalculationListener);

		Result electionResult = electionCalculation.calculateElectionResult(election, ballotImmutableList);
		LoggerFactory.getLogger(MartinWilkesExampleIT.class).info(builder.toString());

		assertThat(electionResult.getCandidatesElectedInNonFemaleOnlyRun(),
		           containsInAnyOrder(CANDIDATE_C, CANDIDATE_E, CANDIDATE_F, CANDIDATE_I));
	}


}
