package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Election;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public final class GenderedElection extends Election<GenderedCandidate> {
	private final int numberOfFemaleExclusivePositions;
	private final int numberOfNotFemaleExclusivePositions;

	public GenderedElection(String officeName, int numberOfFemaleExclusivePositions,
	                        int numberOfNotFemaleExclusivePositions, ImmutableSet<GenderedCandidate> candidates) {
		super(officeName, candidates);
		this.numberOfFemaleExclusivePositions = validateThat(numberOfFemaleExclusivePositions,
		                                                     is(greaterThanOrEqualTo(0)));
		this.numberOfNotFemaleExclusivePositions = validateThat(numberOfNotFemaleExclusivePositions,
		                                                        is(greaterThanOrEqualTo(0)));
	}

	public int getNumberOfFemaleExclusivePositions() {
		return numberOfFemaleExclusivePositions;
	}

	public int getNumberOfNotFemaleExclusivePositions() {
		return numberOfNotFemaleExclusivePositions;
	}
}
