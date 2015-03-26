/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

public final class TestUtils {
	private static int s_ballotId = 0;

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
		return new Ballot<>(s_ballotId++, ImmutableSet.of(vote));
	}

	public static <T extends Candidate> Ballot<T> createNoBallot(Election<T> election) {
		return new Ballot<>(s_ballotId++, ImmutableSet.of(Vote.createNoVote(election)));
	}


	public static <T extends Candidate> Ballot<T> createInvalidBallot(Election<T> election) {
		return new Ballot<>(s_ballotId++, ImmutableSet.of(Vote.createInvalidVote(election)));
	}

	private static <T extends Candidate> ImmutableSet<T> toPreference(String preferences, ImmutableSet <T> candidates) {
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
