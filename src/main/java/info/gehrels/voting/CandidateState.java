package info.gehrels.voting;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;

final class CandidateState<CANDIDATE_TYPE> {
	private final CANDIDATE_TYPE candidate;
	private boolean elected = false;
	private boolean looser = false;


	public CandidateState(CANDIDATE_TYPE candidate) {
		this.candidate = candidate;
	}

	private CandidateState(CANDIDATE_TYPE candidate, boolean elected, boolean looser) {
		this.candidate = candidate;
		this.elected = elected;
		this.looser = looser;
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
		validateThat("Candidate " + candidate + " may not already be a looser", this.looser, is(false));
		return new CandidateState<>(this.candidate, true, false);
	}

	public CandidateState<CANDIDATE_TYPE> asLooser() {
		validateThat("Candidate " + candidate + " may not already be elected", this.elected, is(false));
		return new CandidateState<>(candidate, false, true);
	}

	@Override
	public String toString() {
		return candidate.toString() + ": " + (elected ? "" : "not ") + "elected, " + (looser ? "" : "no ") + "looser";
	}
}
