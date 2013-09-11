package info.gehrels.voting;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * A Candidate is a person running for one office.
 */
public class Candidate {
    public final String name;

	public Candidate(String name) {
	    this.name = validateThat(name, not(isEmptyOrNullString()));
    }

	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
