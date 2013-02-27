package info.gehrels.voting;

import static java.util.Objects.requireNonNull;

/**
 * A Candidate is a person running for one office.
 */
public class Candidate implements Comparable<Candidate> {
    public final String name;
    public final boolean isFemale;

    public Candidate(String name, boolean female) {
        this.name = requireNonNull(name);
        this.isFemale = female;
    }

	@Override
	public int compareTo(Candidate o) {
		return name.compareTo(o.name);
	}
}
