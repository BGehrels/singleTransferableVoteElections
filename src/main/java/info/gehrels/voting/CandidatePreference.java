package info.gehrels.voting;

import com.google.common.collect.ImmutableSortedSet;

public class CandidatePreference {
	private final ImmutableSortedSet<Candidate> candidatesInOrderOfPreference;

	public CandidatePreference(ImmutableSortedSet<Candidate> candidatesInOrderOfPreference) {
		this.candidatesInOrderOfPreference = candidatesInOrderOfPreference;
	}
}
