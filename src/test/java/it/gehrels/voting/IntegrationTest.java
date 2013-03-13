package it.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.DefaultElectionCalculationFactory;
import info.gehrels.voting.DefaultQuorumCalculationImpl;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculationListener;
import info.gehrels.voting.ElectionCalculationWithFemaleExclusivePositions;
import info.gehrels.voting.ElectionCalculationWithFemaleExclusivePositions.ElectionResult;
import info.gehrels.voting.TestUtils;
import info.gehrels.voting.TestUtils.JustTakeTheFirstOneAmbiguityResolver;
import org.junit.Test;

import static info.gehrels.voting.TestUtils.createBallot;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;

public class IntegrationTest {
	public static final DefaultQuorumCalculationImpl QUORUM_CALCULATION = new DefaultQuorumCalculationImpl(.001);
	public static final AmbiguityResolver AMBIGUITY_RESOLVER = new JustTakeTheFirstOneAmbiguityResolver();

	public static final Candidate CANDIDATE_A = new Candidate("A", true);
	public static final Candidate CANDIDATE_B = new Candidate("B", true);
	public static final Candidate CANDIDATE_C = new Candidate("C", true);
	public static final Candidate CANDIDATE_D = new Candidate("D", true);
	public static final Candidate CANDIDATE_E = new Candidate("E", false);
	public static final Candidate CANDIDATE_F = new Candidate("F", false);
	public static final Candidate CANDIDATE_G = new Candidate("G", false);
	public static final Candidate CANDIDATE_H = new Candidate("H", false);
	public static final Candidate CANDIDATE_I = new Candidate("I", false);
	public static final Candidate CANDIDATE_J = new Candidate("J", false);

	private ImmutableList<Ballot> ballotImmutableList;
	private ElectionCalculationListener calculationListener;
	private Election election;

	public IntegrationTest() {
		ImmutableSet<Candidate> candidateSet = ImmutableSet.of(
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

		election = new Election(TestUtils.OFFICE, 0, 4, candidateSet);

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

		calculationListener = mock(ElectionCalculationListener.class);
	}

	@Test
	public void exampleByMartinWilke() {
		ElectionCalculationWithFemaleExclusivePositions electionCalculation = new ElectionCalculationWithFemaleExclusivePositions(
			new DefaultElectionCalculationFactory(QUORUM_CALCULATION,
			                                      calculationListener,
			                                      AMBIGUITY_RESOLVER), calculationListener);
		ElectionResult electionResult = electionCalculation.calculateElectionResult(election, ballotImmutableList);

		assertThat(electionResult.candidatesElectedInOpenRun,
		           containsInAnyOrder(CANDIDATE_C, CANDIDATE_E, CANDIDATE_F, CANDIDATE_I));
	}


}
