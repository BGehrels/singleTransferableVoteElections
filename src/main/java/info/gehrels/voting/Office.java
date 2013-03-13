package info.gehrels.voting;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;

/**
 * An Office is an organ of some legal entity. It may consist of one or more natural persons.
 */
public class Office {
	public final String name;

	public Office(String name) {
		this.name = validateThat(name, not(isEmptyString()));
	}
}
