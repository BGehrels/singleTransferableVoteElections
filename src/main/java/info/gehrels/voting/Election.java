package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

/**
 * An Election is defined by one or more Candidates running for an Office. It also defines, how many Candidates may at
 * most be successful.
 */
public class Election {
	private final Office office;
	private final int numberOfFemaleExclusivePositions;
	private final int numberOfNotFemaleExclusivePositions;
	private final ImmutableSet<Candidate> candidates;

	public Election(Office office, int numberOfFemaleExclusivePositions, int numberOfNotFemaleExclusivePositions,
	                ImmutableSet<Candidate> candidates) {
		this.office = office;
		this.numberOfFemaleExclusivePositions = numberOfFemaleExclusivePositions;
		this.numberOfNotFemaleExclusivePositions = numberOfNotFemaleExclusivePositions;
		this.candidates = candidates;
	}

	public Office getOffice() {
		return office;
	}

	public ImmutableSet<Candidate> getCandidates() {
		return candidates;
	}

	public int getNumberOfFemaleExclusivePositions() {
		return numberOfFemaleExclusivePositions;
	}

	public int getNumberOfNotFemaleExclusivePositions() {
		return numberOfNotFemaleExclusivePositions;
	}
}
