/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.genderedElections;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.genderedElections.ElectionCalculationWithFemaleExclusivePositionsListener.NonQualificationReason;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

final class NotElectedBeforePredicate implements Predicate<GenderedCandidate> {
	private final ImmutableCollection<? extends Candidate> alreadyElectedCandidates;
	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener;

	NotElectedBeforePredicate(ImmutableCollection<GenderedCandidate> alreadyElectedCandidates,
	                                 ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener) {
		this.alreadyElectedCandidates = validateThat(alreadyElectedCandidates, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
	}

	@Override
	public boolean apply(GenderedCandidate candidate) {
		if (alreadyElectedCandidates.contains(candidate)) {
			electionCalculationListener.candidateNotQualified(candidate, NonQualificationReason.ALREADY_ELECTED);
			return false;
		}

		return true;
	}
}
