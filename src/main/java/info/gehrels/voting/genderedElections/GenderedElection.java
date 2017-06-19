/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Election;
import info.gehrels.voting.GuavaCollectors;

import java.util.Objects;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

public final class GenderedElection extends Election<GenderedCandidate> {
	private final long numberOfFemaleExclusivePositions;
	private final long numberOfNotFemaleExclusivePositions;

	public GenderedElection(String officeName, long numberOfFemaleExclusivePositions,
	                        long numberOfNotFemaleExclusivePositions, ImmutableSet<GenderedCandidate> candidates) {
		super(officeName, candidates);
		this.numberOfFemaleExclusivePositions = validateThat(numberOfFemaleExclusivePositions,
		                                                     is(greaterThanOrEqualTo(0L)));
		this.numberOfNotFemaleExclusivePositions = validateThat(numberOfNotFemaleExclusivePositions,
		                                                        is(greaterThanOrEqualTo(0L)));
	}

	public long getNumberOfFemaleExclusivePositions() {
		return numberOfFemaleExclusivePositions;
	}

	public long getNumberOfNotFemaleExclusivePositions() {
		return numberOfNotFemaleExclusivePositions;
	}

	public GenderedElection withOfficeName(String officeName) {
		return new GenderedElection(officeName, numberOfFemaleExclusivePositions, numberOfNotFemaleExclusivePositions, getCandidates());
	}

	public GenderedElection withNumberOfFemaleExclusivePositions(long numberOfFemaleExclusivePositions) {
		return new GenderedElection(getOfficeName(), numberOfFemaleExclusivePositions, numberOfNotFemaleExclusivePositions, getCandidates());
	}

	public GenderedElection withNumberOfNotFemaleExclusivePositions(long numberOfNotFemaleExclusivePositions) {
		return new GenderedElection(getOfficeName(), numberOfFemaleExclusivePositions, numberOfNotFemaleExclusivePositions, getCandidates());
	}

	public GenderedElection withReplacedCandidate(GenderedCandidate replacement) {
		ImmutableSet<GenderedCandidate> candidates = getCandidates().stream()
				.map((c) -> replaceIfSameName(c, replacement))
				.collect(GuavaCollectors.toImmutableSet());
		return new GenderedElection(getOfficeName(), numberOfFemaleExclusivePositions, numberOfNotFemaleExclusivePositions, candidates);
	}

	private GenderedCandidate replaceIfSameName(GenderedCandidate c, GenderedCandidate replacement) {
        if (Objects.equals(c.getName(), replacement.getName())) {
            return replacement;
        } else {
            return c;
        }
    }

	@Override
	public String toString() {
		return getOfficeName() + " (" + numberOfFemaleExclusivePositions + " Frauenplätze, " + numberOfNotFemaleExclusivePositions + " offene Plätze)";
	}
}
