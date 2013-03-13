package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

public interface ElectionCalculationFactory {
	STVElectionCalculation createElectionCalculation(Election election, ImmutableCollection<Ballot> ballots);
}
