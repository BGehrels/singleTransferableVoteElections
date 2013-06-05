package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

public interface ElectionCalculationFactory<CANDIDATE_TYPE extends Candidate> {
	STVElectionCalculation<CANDIDATE_TYPE> createElectionCalculation(Election<CANDIDATE_TYPE> election, ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots);
}
