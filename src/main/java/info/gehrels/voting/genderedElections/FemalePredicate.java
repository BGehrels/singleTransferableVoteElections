package info.gehrels.voting.genderedElections;


import com.google.common.base.Predicate;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class FemalePredicate implements Predicate<GenderedCandidate> {

	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener;

	public FemalePredicate(ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener) {
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
	}

	@Override
	public boolean apply(GenderedCandidate candidate) {
		if (candidate.isFemale) {
			return true;
		} else {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate is not female.");
			return false;
		}
	}
}
