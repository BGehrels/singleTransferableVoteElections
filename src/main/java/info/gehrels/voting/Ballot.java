package info.gehrels.voting;

import com.google.common.collect.ImmutableSortedSet;

/**
 * A Ballot instance represents a physical piece of paper marked by a voter. It contains one or more areas, each
 * designated to one office beeing elected. In each of the areas, the voter may have expressed his preference between
 * one or more Candidates.
 */
public class Ballot {
    private static int ballotIdFactory = 0;

    public final int id;
    public final ImmutableSortedSet<Candidate> rankedCandidates;

    public Ballot(ImmutableSortedSet<Candidate> rankedCandidates) {
        this.id = ++ballotIdFactory;
        this.rankedCandidates = rankedCandidates;
    }
}
