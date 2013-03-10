package info.gehrels.voting;


import com.google.common.base.Predicate;

public class FemalePredicate implements Predicate<Candidate> {

	private final ElectionCalculationListener electionCalculationListener;

	public FemalePredicate(ElectionCalculationListener electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public boolean apply(Candidate candidate) {
		if (candidate.isFemale) {
			return true;
		} else {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate is not female.");
			return false;
		}
	}
}
