package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;
import info.gehrels.voting.ElectionCalculation.ElectionResult;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;

public class IntegrationTest {
	public static final QuorumCalculationImpl QUORUM_CALCULATION = new QuorumCalculationImpl(.001);
	public static final AmbiguityResolver AMBIGUITY_RESOLVER = new JustTakeTheFirstOneAmbiguityResolver();

	public static final Office OFFICE = new Office("Example Office");

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

	private final ImmutableSet<Candidate> candidateSet;

	private Ballot ballot1;
	private Ballot ballot2;
	private Ballot ballot3;
	private Ballot ballot4;
	private Ballot ballot5;
	private Ballot ballot6;
	private Ballot ballot7;
	private Ballot ballot8;
	private Ballot ballot9;
	private Ballot ballot10;
	private Ballot ballot11;
	private Ballot ballot12;
	private Ballot ballot13;
	private Ballot ballot14;
	private Ballot ballot15;
	private Ballot ballot16;
	private Ballot ballot17;
	private Ballot ballot18;
	private Ballot ballot19;
	private Ballot ballot20;
	private Ballot ballot21;

	private ImmutableList<Ballot> ballotImmutableList;
	private ElectionCalculationListener calculationListener;
	private Election election;
	private ImmutableSet<Election> elections;
	private CombinedElectionsOnOneBallot combinedElectionsOnOneBallot;

	public IntegrationTest() {
		candidateSet = ImmutableSet.<Candidate>builder()
			.add(CANDIDATE_A)
			.add(CANDIDATE_B)
			.add(CANDIDATE_C)
			.add(CANDIDATE_D)
			.add(CANDIDATE_E)
			.add(CANDIDATE_F)
			.add(CANDIDATE_G)
			.add(CANDIDATE_H)
			.add(CANDIDATE_I)
			.add(CANDIDATE_J)
			.build();

		election = new Election(OFFICE, 0, 4, candidateSet);

		elections = ImmutableSet.<Election>builder().add(election).build();
		this.combinedElectionsOnOneBallot = new CombinedElectionsOnOneBallot(elections);


		ballot1 = createBallot("ABDC", combinedElectionsOnOneBallot);
		ballot2 = createBallot("ACBDE", combinedElectionsOnOneBallot);
		ballot3 = createBallot("C", combinedElectionsOnOneBallot);
		ballot4 = createBallot("CAE", combinedElectionsOnOneBallot);
		ballot5 = createBallot("CBAFEDG", combinedElectionsOnOneBallot);
		ballot6 = createBallot("CBDE", combinedElectionsOnOneBallot);
		ballot7 = createBallot("CFBDEH", combinedElectionsOnOneBallot);
		ballot8 = createBallot("CDFEHA", combinedElectionsOnOneBallot);
		ballot9 = createBallot("DEC", combinedElectionsOnOneBallot);
		ballot10 = createBallot("EBDCAF", combinedElectionsOnOneBallot);
		ballot11 = createBallot("EDCA", combinedElectionsOnOneBallot);
		ballot12 = createBallot("F", combinedElectionsOnOneBallot);
		ballot13 = createBallot("FCH", combinedElectionsOnOneBallot);
		ballot14 = createBallot("FGEIHJ", combinedElectionsOnOneBallot);
		ballot15 = createBallot("FHG", combinedElectionsOnOneBallot);
		ballot16 = createBallot("GFEI", combinedElectionsOnOneBallot);
		ballot17 = createBallot("HFJAI", combinedElectionsOnOneBallot);
		ballot18 = createBallot("HGIF", combinedElectionsOnOneBallot);
		ballot19 = createBallot("IJF", combinedElectionsOnOneBallot);
		ballot20 = createBallot("IJH", combinedElectionsOnOneBallot);
		ballot21 = createBallot("JIHFE", combinedElectionsOnOneBallot);
		ballotImmutableList = ImmutableList.<Ballot>builder()
				.add(ballot1)
				.add(ballot2)
				.add(ballot3)
				.add(ballot4)
				.add(ballot5)
				.add(ballot6)
				.add(ballot7)
				.add(ballot8)
				.add(ballot9)
				.add(ballot10)
				.add(ballot11)
				.add(ballot12)
				.add(ballot13)
				.add(ballot14)
				.add(ballot15)
				.add(ballot16)
				.add(ballot17)
				.add(ballot18)
				.add(ballot19)
				.add(ballot20)
				.add(ballot21).build();
			calculationListener = mock(ElectionCalculationListener.class);
	}

	@Test
	public void exampleByMartinWilke() {
		ElectionCalculation electionCalculation = new ElectionCalculation(this.election, ballotImmutableList,
		                                                                  QUORUM_CALCULATION, AMBIGUITY_RESOLVER,
		                                                                  calculationListener);
		ElectionResult electionResult = electionCalculation.calculateElectionResult();

		assertThat(electionResult.electedCandidates,
		           containsInAnyOrder(CANDIDATE_C, CANDIDATE_E, CANDIDATE_F, CANDIDATE_I));
	}

	private static Ballot createBallot(String preferences, CombinedElectionsOnOneBallot combinedElectionsOnOneBallot) {
		Election election = combinedElectionsOnOneBallot.elections.iterator().next();
		ImmutableSet<Candidate> candidates = election.candidates;
		ImmutableSet.Builder<Candidate> preferenceBuilder = ImmutableSet.builder();
		for (int i = 0; i < preferences.length(); i++) {
			char c = preferences.charAt(i);
			preferenceBuilder.add(candidateByName("" + c, candidates));
		}

		ImmutableSet<Candidate> preference = preferenceBuilder.build();
		ElectionCandidatePreference electionCandidatePreference = new ElectionCandidatePreference(election, preference);
		return new Ballot(ImmutableSet.of(electionCandidatePreference));
	}

	private static Candidate candidateByName(String s, ImmutableSet<Candidate> candidates) {
		for (Candidate candidate : candidates) {
			if (candidate.name.equals(s)) {
				return candidate;
			}
		}

		throw new IllegalArgumentException(s);
	}
	private static class JustTakeTheFirstOneAmbiguityResolver implements AmbiguityResolver {
		@Override
		public AmbiguityResolverResult chooseOneOfMany(ImmutableSet<Candidate> bestCandidates) {
			return new AmbiguityResolverResult(bestCandidates.iterator().next(),
			                                   "Habe ganz primitiv das erste Element der Menge genommen");
		}

	}


}
