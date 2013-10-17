package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

public final class TestUtils {
	private static int ballotId = 0;

	private TestUtils() {
	}

	public static <T extends Candidate> Ballot<T> createBallot(String preferenceString, Election<T> election) {
		ImmutableSet<T> preference = toPreference(preferenceString, election.getCandidates());
		Vote<T> vote;
		if (preference.isEmpty()) {
			vote = Vote.createNoVote(election);
		} else {
			vote = Vote.createPreferenceVote(election, preference);
		}
		return new Ballot<T>(ballotId++, ImmutableSet.of(vote));
	}

	private static <T extends Candidate> ImmutableSet<T> toPreference(String preferences, ImmutableSet<T> candidates) {
		ImmutableSet.Builder<T> preferenceBuilder = ImmutableSet.builder();
		for (int i = 0; i < preferences.length(); i++) {
			char c = preferences.charAt(i);
			preferenceBuilder.add(candidateByName(String.valueOf(c), candidates));
		}

		return preferenceBuilder.build();
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
