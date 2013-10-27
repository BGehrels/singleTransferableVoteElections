package info.gehrels.voting;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

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

		return Optional.fromNullable(votesByElections.get(election));
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
