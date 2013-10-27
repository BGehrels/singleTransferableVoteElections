package info.gehrels.voting;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

import static com.google.common.base.Objects.equal;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.SetMatchers.isSubSetOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public final class Vote<CANDIDATE_TYPE extends Candidate> {
	private final Election<CANDIDATE_TYPE> election;
	private final boolean valid;
	private final boolean no;
	private final ImmutableSet<CANDIDATE_TYPE> rankedCandidates;

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createInvalidVote(
		Election<CANDIDATE_TYPE> election) {
		return new Vote<>(election, false, false, ImmutableSet.<CANDIDATE_TYPE>of());
	}

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createPreferenceVote(
		Election<CANDIDATE_TYPE> election, ImmutableSet<CANDIDATE_TYPE> preference) {
		return new Vote<>(election, true, false, validateThat(preference, is(not(empty()))));
	}

	public static <CANDIDATE_TYPE extends Candidate> Vote<CANDIDATE_TYPE> createNoVote(
		Election<CANDIDATE_TYPE> election) {
		return new Vote<>(election, true, true, ImmutableSet.<CANDIDATE_TYPE>of());
	}

	private Vote(Election<CANDIDATE_TYPE> election, boolean valid, boolean no,
	             ImmutableSet<CANDIDATE_TYPE> candidatePreference) {
		this.election = validateThat(election, is(not(nullValue())));
		this.valid = valid;
		this.no = no;
		this.rankedCandidates = validateThat(candidatePreference, isSubSetOf(election.getCandidates()));
	}

	public Election<CANDIDATE_TYPE> getElection() {
		return election;
	}

	public boolean isNo() {
		return no;
	}

	public boolean isValid() {
		return valid;
	}

	public ImmutableSet<CANDIDATE_TYPE> getRankedCandidates() {
		return rankedCandidates;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Vote)) {
			return false;
		}

		Vote<?> otherVote = (Vote<?>) obj;

		return equal(election, otherVote.election) && equal(valid, otherVote.valid) && equal(no, otherVote.no) && equal(
			rankedCandidates, otherVote.rankedCandidates);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(election, valid, no, rankedCandidates);
	}
}
