package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

public class ElectionCalculationForQualifiedGroup {
	private final ImmutableCollection<Ballot> ballots;

	public ElectionCalculationForQualifiedGroup(ImmutableCollection<Ballot> ballots) {
		this.ballots = ballots;
	}

	public ImmutableSet<Candidate> calculate(ImmutableSet<Candidate> qualifiedCandidates) {
		return ImmutableSet.of();
	}
}
