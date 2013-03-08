package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;
import info.gehrels.voting.ElectionCalculation.ElectionResult;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.mock;

public class IntegrationTest {

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

	private static class MyAmbiguityResolver implements AmbiguityResolver {
		@Override
		public AmbiguityResolverResult chooseOneOfMany(ImmutableSet<Candidate> bestCandidates) {
			return new AmbiguityResolverResult(bestCandidates.iterator().next(),
			                                   "Habe ganz primitiv das erste Element der Menge genommen");
		}
	}
	@Test
	public void exampleByMartinWilke() {
		ImmutableSet<Candidate> candidates =
			candidateSet;

				Office office = new Office("Example Office");
				Election election = new Election(office, 0, 4, candidates);
				ImmutableSet<Election> elections = ImmutableSet.<Election>builder().add(election).build();
				CombinedElectionsOnOneBallot combinedElectionsOnOneBallot = new CombinedElectionsOnOneBallot(elections);

				Builder<Ballot> ballotBuilder = ImmutableList.builder();
				ballotBuilder
					.add(createBallot("ABDC", combinedElectionsOnOneBallot))
					.add(createBallot("ACBDE", combinedElectionsOnOneBallot))
					.add(createBallot("C", combinedElectionsOnOneBallot))
					.add(createBallot("CAE", combinedElectionsOnOneBallot))
					.add(createBallot("CBAFEDG", combinedElectionsOnOneBallot))
					.add(createBallot("CBDE", combinedElectionsOnOneBallot))
					.add(createBallot("CFBDEH", combinedElectionsOnOneBallot))
					.add(createBallot("CDFEHA", combinedElectionsOnOneBallot))
					.add(createBallot("DEC", combinedElectionsOnOneBallot))
					.add(createBallot("EBDCAF", combinedElectionsOnOneBallot))
					.add(createBallot("EDCA", combinedElectionsOnOneBallot))
					.add(createBallot("F", combinedElectionsOnOneBallot))
					.add(createBallot("FCH", combinedElectionsOnOneBallot))
					.add(createBallot("FGEIHJ", combinedElectionsOnOneBallot))
					.add(createBallot("FHG", combinedElectionsOnOneBallot))
					.add(createBallot("GFEI", combinedElectionsOnOneBallot))
					.add(createBallot("HFJAI", combinedElectionsOnOneBallot))
					.add(createBallot("HGIF", combinedElectionsOnOneBallot))
					.add(createBallot("IJF", combinedElectionsOnOneBallot))
					.add(createBallot("IJH", combinedElectionsOnOneBallot))
					.add(createBallot("JIHFE", combinedElectionsOnOneBallot));

		ElectionResult electionResult = new ElectionCalculation(election, ballotBuilder.build(),
		                                                        new QuorumCalculationImpl(.001), new MyAmbiguityResolver(),
		                                                        mock(ElectionCalculationListener.class))
			.calculateElectionResult();

		System.err.println(electionResult.electedCandidates);
		assertThat(electionResult.electedCandidates, containsInAnyOrder(CANDIDATE_C, CANDIDATE_E, CANDIDATE_F, CANDIDATE_I));
	}
	
	
}
