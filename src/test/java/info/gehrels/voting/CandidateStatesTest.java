package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static info.gehrels.voting.CandidateStateMatchers.candidateStateFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class CandidateStatesTest {

	public static final Candidate A = new Candidate("A", true);
	public static final Candidate B = new Candidate("B", true);
	public static final Candidate C = new Candidate("C", true);
	public static final Candidate D = new Candidate("D", true);

	@Test
	public void returnsEmptyCandidateSetIfCandidateStateMapIsEmpty() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.<Candidate>of());
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();
		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsEmptyCandidateSetIfAllCandidatesAreElected() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B)).withElected(A).withElected(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();
		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsEmptyCandidateSetIfAllCandidatesAreLoosers() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B)).withLooser(A).withLooser(B);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, is(empty()));
	}

	@Test
	public void returnsOnlyHopfulCandidates() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B, C, D)).withLooser(A).withElected(C);
		ImmutableSet<Candidate> hopefulCandidates = candidateStates.getHopefulCandidates();

		assertThat(hopefulCandidates, containsInAnyOrder(B, D));
	}

	@Test
	public void returnsCandidateStateForKnownCandidate() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A));

		assertThat(candidateStates.getCandidateState(A).getCandidate(), is(A));
	}

	@Test
	public void returnsNullForUnknownCandidate() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A));

		CandidateState candidateState = candidateStates.getCandidateState(B);

		assertThat(candidateState, is(nullValue()));
	}


	@Test
	public void withLooserReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsLooser() {
		CandidateStates oldCandidateStates = new CandidateStates(ImmutableSet.of(A, B));

		CandidateStates result = oldCandidateStates.withLooser(B);

		assertThat(result.getCandidateState(B).isLooser(), is(true));
	}


	@Test
	public void withElectedReturnsNewCandidateStatesWithRespectiveCandidateMarkedAsElected() {
		CandidateStates oldCandidateStates = new CandidateStates(ImmutableSet.of(A, B));

		CandidateStates result = oldCandidateStates.withElected(B);

		assertThat(result.getCandidateState(B).isElected(), is(true));
	}

	@Test
	public void returnsEmptyIteratorForEmptyState() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.<Candidate>of());
		assertThat(candidateStates, is(emptyIterable()));
	}

	@Test
	public void returnsAllCandidateStatesInArbitraryOrder() {
		CandidateStates candidateStates = new CandidateStates(ImmutableSet.of(A, B));
		assertThat(candidateStates, is(containsInAnyOrder(candidateStateFor(A), candidateStateFor(B))));
	}


}
