package info.gehrels.voting;

final class CandidateState implements Cloneable {
	private final Candidate candidate;
	private boolean elected = false;
	private boolean looser = false;


	public CandidateState(Candidate candidate) {
		this.candidate = candidate;
	}

	public Candidate getCandidate() {
		return candidate;
	}

	public boolean isHopeful() {
		return !elected && !looser;
	}

	public boolean isElected() {
		return elected;
	}

	public boolean isLooser() {
		return looser;
	}

	public CandidateState asElected() {
		CandidateState result = this.clone();
		result.elected = true;
		return result;
	}

	public CandidateState asLooser() {
		CandidateState result = this.clone();
		result.looser = true;
		return result;
	}

	@Override
	protected CandidateState clone()  {
		try {
			return (CandidateState) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return candidate.toString() + ": " + (elected ? "" : "not ") + "elected, " + (looser ? "" : "no ") + "looser";
	}
}
