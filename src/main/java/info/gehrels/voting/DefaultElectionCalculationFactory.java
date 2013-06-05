package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DefaultElectionCalculationFactory<CANDIDATE_TYPE extends Candidate> implements ElectionCalculationFactory<CANDIDATE_TYPE> {
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener electionCalculationListener;
	private final AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver;

	public DefaultElectionCalculationFactory(QuorumCalculation quorumCalculation,
	                                         ElectionCalculationListener electionCalculationListener,
	                                         AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver) {
		this.quorumCalculation = validateThat(quorumCalculation, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.ambiguityResolver = validateThat(ambiguityResolver, is(notNullValue()));
	}

	public STVElectionCalculation<CANDIDATE_TYPE> createElectionCalculation(Election<CANDIDATE_TYPE> election,
	                                                                      ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots) {
		return new STVElectionCalculation<CANDIDATE_TYPE>(ballots, quorumCalculation, electionCalculationListener, election,
		                                                ambiguityResolver, new WeightedInclusiveGregoryMethod(
			electionCalculationListener));
	}
}
