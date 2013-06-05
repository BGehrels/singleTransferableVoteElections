package info.gehrels.voting.genderedElections;

import info.gehrels.voting.Candidate;

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
