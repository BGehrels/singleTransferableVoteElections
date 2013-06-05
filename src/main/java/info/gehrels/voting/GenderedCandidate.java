package info.gehrels.voting;

public class GenderedCandidate extends Candidate {
	public final boolean isFemale;

	public GenderedCandidate(String name, boolean female) {
		super(name);
		this.isFemale = female;
	}

	@Override
	public String toString() {
		return name + (isFemale ? " (♀)": "");
	}
}
