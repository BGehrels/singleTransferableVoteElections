package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;

public class TestUtils {
	public static final Office OFFICE = new Office("Example Office");

	public static Ballot createBallot(String preferences, Election election) {
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

	public static class JustTakeTheFirstOneAmbiguityResolver implements AmbiguityResolver {
		@Override
		public AmbiguityResolverResult chooseOneOfMany(ImmutableSet<Candidate> bestCandidates) {
			return new AmbiguityResolverResult(bestCandidates.iterator().next(),
			                                   "Habe ganz primitiv das erste Element der Menge genommen");
		}

	}
}
