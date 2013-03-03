package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public interface AmbiguityResolver {
    AmbiguityResolverResult chooseOneOfMany(ImmutableSet<Candidate> bestCandidates);

	class AmbiguityResolverResult {
		public final Candidate choosenCandidate;
		public final String auditLog;

		public AmbiguityResolverResult(Candidate choosenCandidate, String auditLog) {
			this.choosenCandidate = validateThat(choosenCandidate, is(not(nullValue())));
			this.auditLog = validateThat(auditLog, not(isEmptyOrNullString()));
		}
	}
}
