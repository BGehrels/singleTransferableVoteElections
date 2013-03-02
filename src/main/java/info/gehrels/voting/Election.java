package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

/**
 * An Election is defined by one or more Candidates running for an Office. It also defines, how many Candidates may at
 * most be successful.
 */
public class Election {
	public final Office office;
	public final int numberOfFemaleExclusivePositions;
	public final int numberOfNotFemaleExclusivePositions;
	public final ImmutableSet<Candidate> candidates;

	public Election(Office office, int numberOfFemaleExclusivePositions, int numberOfNotFemaleExclusivePositions,
	                ImmutableSet<Candidate> candidates) {
		this.office = office;
		this.numberOfFemaleExclusivePositions = numberOfFemaleExclusivePositions;
		this.numberOfNotFemaleExclusivePositions = numberOfNotFemaleExclusivePositions;
		this.candidates = candidates;
	}
}
