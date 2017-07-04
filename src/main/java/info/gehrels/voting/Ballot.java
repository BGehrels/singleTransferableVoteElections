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
package info.gehrels.voting;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.Optional;

import static com.google.common.base.Objects.equal;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * A Ballot instance represents a physical piece of paper marked by a voter. It contains one or more areas, each
 * designated to one Election. In each of the areas, the voter may have expressed his preference between
 * one or more Candidates, represented by a Vote.
 */
public final class Ballot<CANDIDATE_TYPE extends Candidate> {
	public final long id;
	public final ImmutableMap<Election<CANDIDATE_TYPE>, Vote<CANDIDATE_TYPE>> votesByElections;

	public Ballot(long id, ImmutableSet<Vote<CANDIDATE_TYPE>> votes) {
		this.id = id;
		validateThat(votes, is(notNullValue()));

		Builder<Election<CANDIDATE_TYPE>, Vote<CANDIDATE_TYPE>> builder = ImmutableMap.builder();
		for (Vote<CANDIDATE_TYPE> vote : votes) {
			builder.put(vote.getElection(), vote);
		}
		this.votesByElections = builder.build();
	}

	public Optional<Vote<CANDIDATE_TYPE>> getVote(Election<CANDIDATE_TYPE> election) {
		validateThat(election, is(notNullValue()));

		return Optional.ofNullable(votesByElections.get(election));
	}

	public Ballot<CANDIDATE_TYPE> withReplacedElection(String oldOfficeName, Election<CANDIDATE_TYPE> newElection) {
		ImmutableSet.Builder<Vote<CANDIDATE_TYPE>> builder = ImmutableSet.builder();
		for (Vote<CANDIDATE_TYPE> vote : votesByElections.values()) {
			if (vote.getElection().getOfficeName().equals(oldOfficeName)) {
				builder.add(vote.withReplacedElection(newElection));
			} else {
				builder.add(vote);
			}
		}
		return new Ballot<>(id, builder.build());
	}


	@Override
	public int hashCode() {
		return Objects.hashCode(id, votesByElections);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Ballot)) {
			return false;
		}

		Ballot<?> otherBallot = (Ballot<?>) obj;
		return equal(id, otherBallot.id) && equal(votesByElections, otherBallot.votesByElections);
	}
}
