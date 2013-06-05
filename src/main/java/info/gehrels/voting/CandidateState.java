package info.gehrels.voting;

final class CandidateState<CANDIDATE_TYPE> implements Cloneable {
	private final CANDIDATE_TYPE candidate;
	private boolean elected = false;
	private boolean looser = false;


	public CandidateState(CANDIDATE_TYPE candidate) {
		this.candidate = candidate;
	}

	public CANDIDATE_TYPE getCandidate() {
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

	public CandidateState<CANDIDATE_TYPE> asElected() {
		CandidateState<CANDIDATE_TYPE> result = this.clone();
		result.elected = true;
		return result;
	}

	public CandidateState<CANDIDATE_TYPE> asLooser() {
		CandidateState<CANDIDATE_TYPE> result = this.clone();
		result.looser = true;
		return result;
	}

	@Override
	protected CandidateState<CANDIDATE_TYPE> clone()  {
		try {
			return (CandidateState<CANDIDATE_TYPE>) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public String toString() {
		return candidate.toString() + ": " + (elected ? "" : "not ") + "elected, " + (looser ? "" : "no ") + "looser";
	}
}
