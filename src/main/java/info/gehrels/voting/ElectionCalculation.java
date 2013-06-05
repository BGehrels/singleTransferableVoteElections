package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

public interface ElectionCalculation<CANDIDATE_TYPE> {
	public ImmutableSet<CANDIDATE_TYPE> calculate(ImmutableSet<CANDIDATE_TYPE> genderedCandidates, int numberOfSeats);
}
