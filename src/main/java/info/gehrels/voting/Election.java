package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

/**
 * An Election is defined by one or more Candidates running for an Office. It also defines, how many Candidates may at
 * most be successful.
 */
public class Election {
	private final int numberOfFemaleExclusivePositions;
	private final int numberOfNotFemaleExclusivePositions;
	private final ImmutableSet<Candidate> candidates;

	public Election(int numberOfFemaleExclusivePositions, int numberOfNotFemaleExclusivePositions,
	                ImmutableSet<Candidate> candidates) {
		this.numberOfFemaleExclusivePositions = numberOfFemaleExclusivePositions;
		this.numberOfNotFemaleExclusivePositions = numberOfNotFemaleExclusivePositions;
		this.candidates = candidates;
	}
}
