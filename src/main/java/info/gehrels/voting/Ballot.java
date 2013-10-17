package info.gehrels.voting;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * A Ballot instance represents a physical piece of paper marked by a voter. It contains one or more areas, each
 * designated to one Election. In each of the areas, the voter may have expressed his preference between
 * one or more Candidates, represented by a Vote.
 */
public class Ballot<CANDIDATE_TYPE extends Candidate> {
	public final int id;
	public final ImmutableMap<Election<CANDIDATE_TYPE>, Vote<CANDIDATE_TYPE>> votesByElections;

	public Ballot(int id, ImmutableSet<Vote<CANDIDATE_TYPE>> votes) {
		this.id = id;
		validateThat(votes, is(notNullValue()));

		Builder<Election<CANDIDATE_TYPE>, Vote<CANDIDATE_TYPE>> builder = ImmutableMap.builder();
		for (Vote<CANDIDATE_TYPE> vote : votes) {
			builder.put(vote.getElection(), vote);
		}
		this.votesByElections = builder.build();
	}

	public final Optional<Vote<CANDIDATE_TYPE>> getVote(Election<CANDIDATE_TYPE> election) {
		validateThat(election, is(notNullValue()));

		return Optional.fromNullable(votesByElections.get(election));
	}

}
