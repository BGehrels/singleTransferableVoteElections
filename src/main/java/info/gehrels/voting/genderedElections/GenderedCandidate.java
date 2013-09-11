package info.gehrels.voting.genderedElections;

import info.gehrels.voting.Candidate;

public final class GenderedCandidate extends Candidate {
	private final boolean isFemale;

	public GenderedCandidate(String name, boolean isFemale) {
		super(name);
		this.isFemale = isFemale;
	}

	public boolean isFemale() {
		return isFemale;
	}

	@Override
	public String toString() {
		return name + (isFemale ? " (♀)": "");
	}
}
