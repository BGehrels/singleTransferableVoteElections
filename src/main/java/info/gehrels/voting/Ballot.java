package info.gehrels.voting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.HamcrestMatchers.isSubSetOf;

/**
 * A Ballot instance represents a physical piece of paper marked by a voter. It contains one or more areas, each
 * designated to one Election. In each of the areas, the voter may have expressed his preference between
 * one or more Candidates, represented by a ElectionCandidatePreference.
 */
public class Ballot {
    private static int ballotIdFactory = 0;

    public final int id;
    public final ImmutableMap<Election, ImmutableSet<Candidate>> rankedCandidatesByElection;

    public Ballot(ImmutableSet<ElectionCandidatePreference> rankedCandidatesByElection) {
        this.id = ++ballotIdFactory;
	    Builder<Election,ImmutableSet<Candidate>> builder = ImmutableMap.builder();
	    for (ElectionCandidatePreference electionCandidatePreference : rankedCandidatesByElection) {
		    builder.put(electionCandidatePreference.election, electionCandidatePreference.candidatePreference);
	    }
	    this.rankedCandidatesByElection = builder.build();
    }

	public ImmutableSet<Candidate> getRankedCandidatesByElection(Election election) {
		ImmutableSet<Candidate> candidates = rankedCandidatesByElection.get(election);
		if (candidates == null) {
			// TODO: Is there a difference between not casting a vote and voting with a empty preference? It will make
			// TODO: one in the algorithm, because the quorum is not met if too many peapole cast empty votes.
			return ImmutableSet.of();
		}

		return candidates;
	}

	public static class ElectionCandidatePreference {
		private final Election election;
		private final ImmutableSet<Candidate> candidatePreference;

		public ElectionCandidatePreference(Election election, ImmutableSet<Candidate> candidatePreference) {
			validateThat(candidatePreference, isSubSetOf(election.getCandidates()));
			this.election = election;
			this.candidatePreference = candidatePreference;
		}

	}
}
