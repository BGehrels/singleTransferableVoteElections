package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

public interface ElectionCalculation<CANDIDATE_TYPE> {
	ImmutableSet<CANDIDATE_TYPE> calculate(ImmutableSet<CANDIDATE_TYPE> qualifiedCandidates, int numberOfSeats);
}
