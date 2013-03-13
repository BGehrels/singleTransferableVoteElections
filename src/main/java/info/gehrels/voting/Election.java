package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
		this.office = validateThat(office, is(notNullValue()));
		this.numberOfFemaleExclusivePositions = validateThat(numberOfFemaleExclusivePositions,
		                                                     is(greaterThanOrEqualTo(0)));
		this.numberOfNotFemaleExclusivePositions = validateThat(numberOfNotFemaleExclusivePositions,
		                                                        is(greaterThanOrEqualTo(0)));
		this.candidates = validateThat(candidates, is(notNullValue()));
	}
}
