package info.gehrels.voting;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class NotElectedBeforePredicate<T extends Candidate> implements Predicate<T> {
	private final ImmutableCollection<? extends Candidate> alreadyElectedCandidates;
	private final ElectionCalculationListener<T> electionCalculationListener;

	public NotElectedBeforePredicate(ImmutableCollection<T> alreadyElectedCandidates,
	                                 ElectionCalculationListener<T> electionCalculationListener) {
		this.alreadyElectedCandidates = validateThat(alreadyElectedCandidates, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
	}

	@Override
	public boolean apply(T candidate) {
		if (alreadyElectedCandidates.contains(candidate)) {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate has already been elected.");
			return false;
		}

		return true;
	}
}
