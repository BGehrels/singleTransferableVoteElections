package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public interface AmbiguityResolver<CANDIDATE_TYPE extends Candidate> {
    AmbiguityResolverResult<CANDIDATE_TYPE> chooseOneOfMany(ImmutableSet<CANDIDATE_TYPE> bestCandidates);

	class AmbiguityResolverResult<CANDIDATE_TYPE extends Candidate> {
		public final CANDIDATE_TYPE chosenCandidate;
		public final String auditLog;

		public AmbiguityResolverResult(CANDIDATE_TYPE chosenCandidate, String auditLog) {
			this.chosenCandidate = validateThat(chosenCandidate, is(not(nullValue())));
			this.auditLog = validateThat(auditLog, not(isEmptyOrNullString()));
		}
	}
}
