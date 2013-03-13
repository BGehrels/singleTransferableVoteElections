package it.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AuditLogger;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.DefaultElectionCalculationFactory;
import info.gehrels.voting.DefaultQuorumCalculationImpl;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculationWithFemaleExclusivePositions;
import info.gehrels.voting.TestUtils;
import info.gehrels.voting.TestUtils.JustTakeTheFirstOneAmbiguityResolver;

import static info.gehrels.voting.TestUtils.createBallot;

/**
 * Hello world!
 */
public class App {

	public static final DefaultQuorumCalculationImpl QUORUM_CALCULATION = new DefaultQuorumCalculationImpl(1);
	public static final AuditLogger ELECTION_CALCULATION_LISTENER = new AuditLogger();
	public static final JustTakeTheFirstOneAmbiguityResolver AMBIGUITY_RESOLVER = new JustTakeTheFirstOneAmbiguityResolver();
	public static final DefaultElectionCalculationFactory ELECTION_CALCULATION_FACTORY
		= new DefaultElectionCalculationFactory(
		QUORUM_CALCULATION,
		ELECTION_CALCULATION_LISTENER,
		AMBIGUITY_RESOLVER);

	public static void main(String[] args) {
		ImmutableSet.Builder<Candidate> candidateBuilder = ImmutableSet.builder();
		ImmutableSet<Candidate> candidates =
			candidateBuilder
				.add(new Candidate("A", true))
				.add(new Candidate("B", true))
				.add(new Candidate("C", true))
				.add(new Candidate("D", true))
				.add(new Candidate("E", false))
				.add(new Candidate("F", false))
				.add(new Candidate("G", false))
				.add(new Candidate("H", false))
				.add(new Candidate("I", false))
				.add(new Candidate("J", false))
				.build();

		Election election = new Election(TestUtils.OFFICE, 2, 2, candidates);

		Builder<Ballot> ballotBuilder = ImmutableList.builder();
		ballotBuilder
			.add(createBallot("ABDC", election))
			.add(createBallot("ACBDE", election))
			.add(createBallot("C", election))
			.add(createBallot("CAE", election))
			.add(createBallot("CBAFEDG", election))
			.add(createBallot("CBDE", election))
			.add(createBallot("CFBDEH", election))
			.add(createBallot("CDFEHA", election))
			.add(createBallot("DEC", election))
			.add(createBallot("EBDCAF", election))
			.add(createBallot("EDCA", election))
			.add(createBallot("F", election))
			.add(createBallot("FCH", election))
			.add(createBallot("FGEIHJ", election))
			.add(createBallot("FHG", election))
			.add(createBallot("FGEI", election))
			.add(createBallot("HFJAI", election))
			.add(createBallot("HGIF", election))
			.add(createBallot("IJF", election))
			.add(createBallot("IJH", election))
			.add(createBallot("JIHFE", election));

		final ElectionCalculationWithFemaleExclusivePositions electionCalculation =
			new ElectionCalculationWithFemaleExclusivePositions(ELECTION_CALCULATION_FACTORY,
			                                                    ELECTION_CALCULATION_LISTENER);
		electionCalculation.calculateElectionResult(election, ballotBuilder.build());
	}

}
