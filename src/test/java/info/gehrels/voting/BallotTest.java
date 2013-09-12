package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot.ElectionCandidatePreference;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public final class BallotTest {
	private static final Candidate CANDIDATE = new Candidate("Peter");

	private static final Election<Candidate> ELECTION_1 = new Election<>("Office1", ImmutableSet.of(CANDIDATE));
	private static final Election<Candidate> ELECTION_2 = new Election<>("Office2", ImmutableSet.of(CANDIDATE));
	public static final ElectionCandidatePreference<Candidate> PREFERENCE_FOR_ELECTION_1 = new ElectionCandidatePreference<>(
		ELECTION_1, ImmutableSet.of(CANDIDATE));

	@Test
	public void returnsEmptyPreferenceIfBallotContainsNoDataForTheElection() {
		ImmutableSet<ElectionCandidatePreference<Candidate>> preferenceOnlyForElection1
			= ImmutableSet.of(PREFERENCE_FOR_ELECTION_1);
		Ballot<Candidate> ballot = new Ballot<>(0, preferenceOnlyForElection1);

		assertThat(ballot.getRankedCandidatesByElection(ELECTION_2), is(empty()));
	}


	@Test
	public void returnsElectionsPreferenceIfBallotContainsDataForThisElection() {
		ImmutableSet<ElectionCandidatePreference<Candidate>> preferenceOnlyForElection1
			= ImmutableSet.of(PREFERENCE_FOR_ELECTION_1);
		Ballot<Candidate> ballot = new Ballot<>(0, preferenceOnlyForElection1);

		assertThat(ballot.getRankedCandidatesByElection(ELECTION_1), contains(CANDIDATE));
	}
}
