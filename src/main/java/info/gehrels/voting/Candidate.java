package info.gehrels.voting;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * A Candidate is a person running for one office.
 */
public class Candidate {
    public final String name;
    public final boolean isFemale;

    public Candidate(String name, boolean female) {
	    this.name = validateThat(name, not(isEmptyOrNullString()));
        this.isFemale = female;
    }

	@Override
	public String toString() {
		return name + (isFemale ? " (♀)": "");
	}
}
