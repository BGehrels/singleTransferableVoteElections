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

	private ImmutableList<Ballot> ballotImmutableList;
	private ElectionCalculationListener calculationListener;
	private Election election;
	private CombinedElectionsOnOneBallot combinedElectionsOnOneBallot;

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

		election = new Election(OFFICE, 0, 4, candidateSet);
		this.combinedElectionsOnOneBallot = new CombinedElectionsOnOneBallot(ImmutableSet.of(election));


		ballotImmutableList = ImmutableList.of(
			createBallot("ABDC"),
			createBallot("ACBDE"),
			createBallot("C"),
			createBallot("CAE"),
			createBallot("CBAFEDG"),
			createBallot("CBDE"),
			createBallot("CFBDEH"),
			createBallot("CDFEHA"),
			createBallot("DEC"),
			createBallot("EBDCAF"),
			createBallot("EDCA"),
			createBallot("F"),
			createBallot("FCH"),
			createBallot("FGEIHJ"),
			createBallot("FHG"),
			createBallot("GFEI"),
			createBallot("HFJAI"),
			createBallot("HGIF"),
			createBallot("IJF"),
			createBallot("IJH"),
			createBallot("JIHFE"));

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

	private Ballot createBallot(String preferences) {
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
