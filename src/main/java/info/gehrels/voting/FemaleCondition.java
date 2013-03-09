package info.gehrels.voting;



public class FemaleCondition implements QualificationCondition {

	private final ElectionCalculationListener electionCalculationListener;

	public FemaleCondition(ElectionCalculationListener electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public boolean isQualified(Candidate candidate) {
		if (candidate.isFemale) {
			return true;
		} else {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate is not female.");
			return false;
		}
	}
}
