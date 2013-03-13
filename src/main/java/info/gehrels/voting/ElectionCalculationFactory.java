package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

public interface ElectionCalculationFactory {
	ElectionCalculationForQualifiedGroup createElectionCalculation(Election election, ImmutableCollection<Ballot> ballots);
}
