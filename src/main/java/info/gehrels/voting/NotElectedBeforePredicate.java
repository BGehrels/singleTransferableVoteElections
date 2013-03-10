package info.gehrels.voting;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;

public class NotElectedBeforePredicate implements Predicate<Candidate> {
	private final ImmutableCollection<Candidate> alreadyElectedCandidates;
	private final ElectionCalculationListener electionCalculationListener;

	public NotElectedBeforePredicate(ImmutableCollection<Candidate> alreadyElectedCandidates,
	                                 ElectionCalculationListener electionCalculationListener) {
		this.alreadyElectedCandidates = alreadyElectedCandidates;
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public boolean apply(Candidate candidate) {
		if (alreadyElectedCandidates.contains(candidate)) {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate has already been elected.");
			return false;
		}

		return true;
	}
}
