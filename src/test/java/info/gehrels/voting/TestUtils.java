package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;

public final class TestUtils {
	private static int ballotId = 0;

	private TestUtils() {
	}

	public static <T extends Candidate> Ballot<T> createBallot(String preferences, Election<T> election) {
		ImmutableSet<T> candidates = election.getCandidates();
		ImmutableSet.Builder<T> preferenceBuilder = ImmutableSet.builder();
		for (int i = 0; i < preferences.length(); i++) {
			char c = preferences.charAt(i);
			preferenceBuilder.add(candidateByName(String.valueOf(c), candidates));
		}

		ImmutableSet<T> preference = preferenceBuilder.build();
		ElectionCandidatePreference<T> electionCandidatePreference = new ElectionCandidatePreference<>(election, preference);
		return Ballot.createValidBallot(ballotId++, ImmutableSet.of(electionCandidatePreference));
	}

	private static <T extends Candidate> T candidateByName(String s, ImmutableSet<T> candidates) {
		for (T candidate : candidates) {
			if (candidate.name.equals(s)) {
				return candidate;
			}
		}

		throw new IllegalArgumentException(s);
	}

	public static final class JustTakeTheFirstOneAmbiguityResolver<T extends Candidate> implements AmbiguityResolver<T> {
		@Override
		public AmbiguityResolverResult<T> chooseOneOfMany(ImmutableSet<T> bestCandidates) {
			return new AmbiguityResolverResult<>(bestCandidates.iterator().next(),
			                                   "Habe ganz primitiv das erste Element der Menge genommen");
		}

	}
}
