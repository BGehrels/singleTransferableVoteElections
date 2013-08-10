package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * An Election is defined by one or more Candidates running for an Office. It also defines, how many Candidates may at
 * most be successful.
 */
public class Election<CANDIDATE_TYPE extends Candidate> {
	public final Office office;
	public final ImmutableSet<CANDIDATE_TYPE> candidates;

	public Election(Office office, ImmutableSet<CANDIDATE_TYPE> candidates) {
		this.office = validateThat(office, is(notNullValue()));
		this.candidates = validateThat(candidates, is(notNullValue()));
	}
}
