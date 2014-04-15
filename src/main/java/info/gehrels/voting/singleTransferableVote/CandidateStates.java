/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

final class CandidateStates<CANDIDATE_TYPE> implements Iterable<CandidateState<CANDIDATE_TYPE>> {
	private final ImmutableMap<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> candidateStates;

	CandidateStates(ImmutableSet<CANDIDATE_TYPE> candidates) {
		Builder<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> candidateStatesBuilder = ImmutableMap.builder();
		for (CANDIDATE_TYPE candidate : candidates) {
			candidateStatesBuilder.put(candidate, new CandidateState<>(candidate));
		}
		candidateStates = candidateStatesBuilder.build();
	}

	private CandidateStates(ImmutableMap<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> candidateStates) {
		this.candidateStates = candidateStates;
	}

	public ImmutableSet<CANDIDATE_TYPE> getHopefulCandidates() {
		ImmutableSet.Builder<CANDIDATE_TYPE> builder = ImmutableSet.builder();
		for (Entry<CANDIDATE_TYPE, CandidateState<CANDIDATE_TYPE>> entry : candidateStates.entrySet()) {
			if ((entry.getValue() != null) && entry.getValue().isHopeful()) {
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

	private static <K, V, EK extends K, EV extends V> ImmutableMap<K, V> mapWithChangedEntry(
		ImmutableMap<K, V> map, EK changedKey, EV newValue) {
		Map<K, V> tmpMap = new HashMap<>(map);
		tmpMap.put(changedKey, newValue);
		return ImmutableMap.copyOf(tmpMap);
	}
}
