package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Candidate;
import org.junit.Test;

import static info.gehrels.voting.singleTransferableVote.CandidateStateMatchers.candidateStateFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CandidateStatesTest {

	public static final Candidate A = new Candidate("A");
	public static final Candidate B = new Candidate("B");
	public static final Candidate C = new Candidate("C");
	public static final Candidate D = new Candidate("D");

	@Test
	public void returnsEmptyCandidateSetIfCandidateStateMapIsEmpty() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.<Candidate>of());
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();
		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsEmptyCandidateSetIfAllCandidatesAreElected() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B)).withElected(A)
			.withElected(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();
		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsEmptyCandidateSetIfAllCandidatesAreLoosers() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B)).withLooser(A)
			.withLooser(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsOnlyHopfulCandidates() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B, C, D)).withLooser(A)
			.withElected(C);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, containsInAnyOrder(B, D));
	}

	@Test
	public void returnsCandidateStateForKnownCandidate() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A));

		assertThat(candidateStates.getCandidateState(A).getCandidate(), is(A));
	}

	@Test
	public void returnsNullForUnknownCandidate() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A));

		CandidateState<Candidate> candidateState = candidateStates.getCandidateState(B);

		assertThat(candidateState, is(nullValue()));
	}


	@Test
	public void withLooserReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsLooser() {
		CandidateStates<Candidate> oldCandidateStates = new CandidateStates<>(ImmutableSet.of(A, B));

		CandidateStates<Candidate> result = oldCandidateStates.withLooser(B);

		assertThat(result.getCandidateState(B).isLooser(), is(true));
	}


	@Test
	public void withElectedReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsElected() {
		CandidateStates<Candidate> oldCandidateStates = new CandidateStates<>(ImmutableSet.of(A, B));

		CandidateStates<Candidate> result = oldCandidateStates.withElected(B);

		assertThat(result.getCandidateState(B).isElected(), is(true));
	}

	@Test
	public void returnsEmptyIteratorForEmptyState() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.<Candidate>of());
		assertThat(candidateStates, is(emptyIterable()));
	}

	@Test
	public void returnsAllCandidateStatesInArbitraryOrder() {
		CandidateStates<Candidate> candidateStates = new CandidateStates<>(ImmutableSet.of(A, B));
		assertThat(candidateStates, is(containsInAnyOrder(candidateStateFor(A), candidateStateFor(B))));
	}


}
