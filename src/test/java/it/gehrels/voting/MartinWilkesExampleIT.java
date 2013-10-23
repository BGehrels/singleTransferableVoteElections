package it.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public final class MartinWilkesExampleIT {
	public static final NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum QUORUM_CALCULATION = new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(
		new BigFraction(1, 1000));
	public static final AmbiguityResolver<GenderedCandidate> AMBIGUITY_RESOLVER = new JustTakeTheFirstOneAmbiguityResolver<>();

	public static final GenderedCandidate CANDIDATE_A = new GenderedCandidate("A", true);
	public static final GenderedCandidate CANDIDATE_B = new GenderedCandidate("B", false);
	public static final GenderedCandidate CANDIDATE_C = new GenderedCandidate("C", true);
	public static final GenderedCandidate CANDIDATE_D = new GenderedCandidate("D", false);
	public static final GenderedCandidate CANDIDATE_E = new GenderedCandidate("E", true);
	public static final GenderedCandidate CANDIDATE_F = new GenderedCandidate("F", false);
	public static final GenderedCandidate CANDIDATE_G = new GenderedCandidate("G", true);
	public static final GenderedCandidate CANDIDATE_H = new GenderedCandidate("H", false);
	public static final GenderedCandidate CANDIDATE_I = new GenderedCandidate("I", true);
	public static final GenderedCandidate CANDIDATE_J = new GenderedCandidate("J", false);

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
			createBallot("ABDC", election),
			createBallot("ACBDE", election),
			createBallot("C", election),
			createBallot("CAE", election),
			createBallot("CBAFEDG", election),
			createBallot("CBDE", election),
			createBallot("CFBDEH", election),
			createBallot("CDFEHA", election),
			createBallot("DEC", election),
			createBallot("EBDCAF", election),
			createBallot("EDCA", election),
			createBallot("F", election),
			createBallot("FCH", election),
			createBallot("FGEIHJ", election),
			createBallot("FHG", election),
			createBallot("GFEI", election),
			createBallot("HFJAI", election),
			createBallot("HGIF", election),
			createBallot("IJF", election),
			createBallot("IJH", election),
			createBallot("JIHFE", election));
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

		assertThat(electionResult.getCandidatesElectedInNonFemaleOnlyRun(),
		           containsInAnyOrder(CANDIDATE_C, CANDIDATE_E, CANDIDATE_F, CANDIDATE_I));
		LoggerFactory.getLogger(MartinWilkesExampleIT.class).info(builder.toString());
	}


}
