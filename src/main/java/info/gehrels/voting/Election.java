package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * An Election is defined by one or more Candidates running for an Office. It also defines, how many Candidates may at
 * most be successful.
 */
public class Election<CANDIDATE_TYPE extends Candidate> {
	public final String officeName;
	public final ImmutableSet<CANDIDATE_TYPE> candidates;

	public Election(String officeName, ImmutableSet<CANDIDATE_TYPE> candidates) {
		this.officeName = validateThat(officeName, not(isEmptyString()));
		this.candidates = validateThat(candidates, is(notNullValue()));
	}
}
