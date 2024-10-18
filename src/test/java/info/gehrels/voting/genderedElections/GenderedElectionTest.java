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

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class GenderedElectionTest {
    @Test
    public void replacesCandidateWithSameName() {
        GenderedCandidate otherCandidate = new GenderedCandidate("other", false);
        GenderedCandidate oldVersion = new GenderedCandidate("to be changed", false);
        GenderedElection election = new GenderedElection(
                "some office",
                1,
                1,
                ImmutableSet.of(otherCandidate, oldVersion)
        );

        GenderedCandidate newVersion = oldVersion.withIsFemale(true);
        GenderedElection changedElection = election.withReplacedCandidate(newVersion);

        assertThat(changedElection.getCandidates(), contains(otherCandidate, newVersion));
    }
    @Test
    public void wontReplacesCandidateWithDifferentName() {
        GenderedCandidate oldVersion = new GenderedCandidate("original name", false);
        GenderedCandidate newVersion = new GenderedCandidate("different name", false);
        GenderedElection election = new GenderedElection(
                "some office",
                1,
                1,
                ImmutableSet.of(oldVersion)
        );

        GenderedElection changedElection = election.withReplacedCandidate(newVersion);

        assertThat(changedElection.getCandidates(), contains(oldVersion));
    }
}