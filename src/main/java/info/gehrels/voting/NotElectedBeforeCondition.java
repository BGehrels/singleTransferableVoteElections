package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

public class NotElectedBeforeCondition implements QualificationCondition {
	private final ImmutableCollection<Candidate> alreadyElectedCandidates;
	private final ElectionCalculationListener electionCalculationListener;

	public NotElectedBeforeCondition(ImmutableCollection<Candidate> alreadyElectedCandidates,
	                                 ElectionCalculationListener electionCalculationListener) {
		this.alreadyElectedCandidates = alreadyElectedCandidates;
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public boolean isQualified(Candidate candidate) {
		if (alreadyElectedCandidates.contains(candidate)) {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate has already been elected.");
			return false;
		}

		return true;
	}
}
