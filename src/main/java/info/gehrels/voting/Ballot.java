package info.gehrels.voting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.HamcrestMatchers.isSubSetOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * A Ballot instance represents a physical piece of paper marked by a voter. It contains one or more areas, each
 * designated to one Election. In each of the areas, the voter may have expressed his preference between
 * one or more Candidates, represented by a ElectionCandidatePreference.
 */
public class Ballot<CANDIDATE_TYPE extends Candidate> {
    private static int ballotIdFactory = 0;

    public final int id;
    public final ImmutableMap<Election<CANDIDATE_TYPE>, ImmutableSet<CANDIDATE_TYPE>> rankedCandidatesByElection;

    public Ballot(ImmutableSet<ElectionCandidatePreference<CANDIDATE_TYPE>> rankedCandidatesByElection) {
	    validateThat(rankedCandidatesByElection, is(notNullValue()));

        this.id = ++ballotIdFactory;
	    Builder<Election<CANDIDATE_TYPE>,ImmutableSet<CANDIDATE_TYPE>> builder = ImmutableMap.builder();
	    for (ElectionCandidatePreference<CANDIDATE_TYPE> electionCandidatePreference : rankedCandidatesByElection) {
		    builder.put(electionCandidatePreference.election, electionCandidatePreference.candidatePreference);
	    }
	    this.rankedCandidatesByElection = builder.build();
    }

	public ImmutableSet<CANDIDATE_TYPE> getRankedCandidatesByElection(Election<CANDIDATE_TYPE> election) {
		validateThat(election, is(notNullValue()));

		ImmutableSet<CANDIDATE_TYPE> candidates = rankedCandidatesByElection.get(election);
		if (candidates == null) {
			// TODO: Is there a difference between not casting a vote and voting with a empty preference? It will make
			// TODO: one in the algorithm, because the quorum is not met if too many peapole cast empty votes.
			return ImmutableSet.of();
		}

		return candidates;
	}

	public static class ElectionCandidatePreference<CANDIDATE_TYPE extends Candidate> {
		private final Election<CANDIDATE_TYPE> election;
		private final ImmutableSet<CANDIDATE_TYPE> candidatePreference;

		public ElectionCandidatePreference(Election<CANDIDATE_TYPE> election, ImmutableSet<CANDIDATE_TYPE> candidatePreference) {
			this.election = election;
			this.candidatePreference = validateThat(candidatePreference, isSubSetOf(election.candidates));
		}

	}
}
