package info.gehrels.voting;

import java.util.Collection;

public interface ConflictResolutionAlgorithm {
    Candidate chooseWinner(Collection<Candidate> bestCandidates);
}
