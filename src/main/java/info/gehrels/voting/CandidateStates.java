package info.gehrels.voting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class CandidateStates implements Iterable<CandidateState> {
	private final ImmutableMap<Candidate, CandidateState> candidateStates;

	public CandidateStates(ImmutableSet<? extends Candidate> candidates) {
		Builder<Candidate, CandidateState> candidateStates = ImmutableMap.builder();
		for (Candidate candidate : candidates) {
			candidateStates.put(candidate, new CandidateState(candidate));
		}
		this.candidateStates = candidateStates.build();
	}

	private CandidateStates(ImmutableMap<Candidate, CandidateState> candidateStates) {
		this.candidateStates = candidateStates;
	}

	public ImmutableSet<Candidate> getHopefulCandidates() {
		ImmutableSet.Builder<Candidate> builder = ImmutableSet.builder();
		for (Entry<Candidate, CandidateState> entry : candidateStates.entrySet()) {
			if (entry.getValue() != null && entry.getValue().isHopeful()) {
				builder.add(entry.getValue().getCandidate());
			}
		}

		return builder.build();
	}

	public CandidateStates withElected(Candidate candidate) {
		return new CandidateStates(
			mapWithChangedEntry(candidateStates, candidate, candidateStates.get(candidate).asElected()));
	}

	public CandidateStates withLooser(Candidate candidate) {
		return new CandidateStates(
			mapWithChangedEntry(candidateStates, candidate, candidateStates.get(candidate).asLooser()));
	}

	public CandidateState getCandidateState(Candidate candidate) {
		return candidateStates.get(candidate);
	}

	@Override
	public Iterator<CandidateState> iterator() {
		return candidateStates.values().iterator();
	}

	private <K, V, EK extends K, EV extends V> ImmutableMap<K, V> mapWithChangedEntry(
		ImmutableMap<K, V> map, EK changedKey, EV newValue) {
		HashMap<K, V> tmpMap = new HashMap<>(map);
		tmpMap.put(changedKey, newValue);
		return ImmutableMap.copyOf(tmpMap);
	}
}
