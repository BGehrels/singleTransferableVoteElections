package info.gehrels.voting.genderedElections;

import info.gehrels.voting.Candidate;

public class GenderedCandidate extends Candidate {
	private final boolean isFemale;

	public GenderedCandidate(String name, boolean female) {
		super(name);
		this.isFemale = female;
	}

	public boolean isFemale() {
		return isFemale;
	}

	@Override
	public String toString() {
		return name + (isFemale() ? " (♀)": "");
	}
}
