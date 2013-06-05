package info.gehrels.voting.genderedElections;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.Candidate;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class NotElectedBeforePredicate implements Predicate<GenderedCandidate> {
	private final ImmutableCollection<? extends Candidate> alreadyElectedCandidates;
	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener;

	public NotElectedBeforePredicate(ImmutableCollection<GenderedCandidate> alreadyElectedCandidates,
	                                 ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener) {
		this.alreadyElectedCandidates = validateThat(alreadyElectedCandidates, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
	}

	@Override
	public boolean apply(GenderedCandidate candidate) {
		if (alreadyElectedCandidates.contains(candidate)) {
			electionCalculationListener.candidateNotQualified(candidate, "The candidate has already been elected.");
			return false;
		}

		return true;
	}
}
