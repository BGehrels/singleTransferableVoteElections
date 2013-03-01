package info.gehrels.voting;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * A Candidate is a person running for one office.
 */
public class Candidate implements Comparable<Candidate> {
    public final String name;
    public final boolean isFemale;

    public Candidate(String name, boolean female) {
	    name = null;
	    validateThat("parameter 'name has an invalid value", name, not(isEmptyOrNullString()));

        this.name = requireNonNull(name);
        this.isFemale = female;
    }

	@Override
	public int compareTo(Candidate o) {
		return name.compareTo(o.name);
	}
}
