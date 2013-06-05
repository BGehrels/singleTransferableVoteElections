package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

final class CandidateStates<CANDIDATE_TYPE> implements Iterable<CandidateState<CANDIDATE_TYPE>> {
	private final ImmutableMap<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> candidateStates;

	public CandidateStates(ImmutableSet<CANDIDATE_TYPE> candidates) {
		Builder<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> candidateStates = ImmutableMap.builder();
		for (CANDIDATE_TYPE candidate : candidates) {
			candidateStates.put(candidate, new CandidateState<>(candidate));
		}
		this.candidateStates = candidateStates.build();
	}

	private CandidateStates(ImmutableMap<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> candidateStates) {
		this.candidateStates = candidateStates;
	}

	public ImmutableSet<CANDIDATE_TYPE> getHopefulCandidates() {
		ImmutableSet.Builder<CANDIDATE_TYPE> builder = ImmutableSet.builder();
		for (Entry<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> entry : candidateStates.entrySet()) {
			if (entry.getValue() != null && entry.getValue().isHopeful()) {
				builder.add(entry.getValue().getCandidate());
			}
		}

		return builder.build();
	}

	public CandidateStates<CANDIDATE_TYPE> withElected(CANDIDATE_TYPE candidate) {
		return new CandidateStates<>(
			mapWithChangedEntry(candidateStates, candidate, candidateStates.get(candidate).asElected()));
	}

	public CandidateStates<CANDIDATE_TYPE> withLooser(CANDIDATE_TYPE candidate) {
		return new CandidateStates<>(
			mapWithChangedEntry(candidateStates, candidate, candidateStates.get(candidate).asLooser()));
	}

	public CandidateState<CANDIDATE_TYPE> getCandidateState(CANDIDATE_TYPE candidate) {
		return candidateStates.get(candidate);
	}

	@Override
	public Iterator<CandidateState<CANDIDATE_TYPE>> iterator() {
		return candidateStates.values().iterator();
	}

	private <K, V, EK extends K, EV extends V> ImmutableMap<K, V> mapWithChangedEntry(
		ImmutableMap<K, V> map, EK changedKey, EV newValue) {
		HashMap<K, V> tmpMap = new HashMap<>(map);
		tmpMap.put(changedKey, newValue);
		return ImmutableMap.copyOf(tmpMap);
	}
}
