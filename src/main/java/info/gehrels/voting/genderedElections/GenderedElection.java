package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Election;
import info.gehrels.voting.Office;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public class GenderedElection extends Election<GenderedCandidate> {
	public final int numberOfFemaleExclusivePositions;
	public final int numberOfNotFemaleExclusivePositions;

	public GenderedElection(Office office, int numberOfFemaleExclusivePositions,
	                        int numberOfNotFemaleExclusivePositions, ImmutableSet<GenderedCandidate> candidates) {
		super(office, candidates);
		this.numberOfFemaleExclusivePositions = validateThat(numberOfFemaleExclusivePositions,
		                                                     is(greaterThanOrEqualTo(0)));
		this.numberOfNotFemaleExclusivePositions = validateThat(numberOfNotFemaleExclusivePositions,
		                                                        is(greaterThanOrEqualTo(0)));
	}
}
