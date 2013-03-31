package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class DefaultElectionCalculationFactory implements ElectionCalculationFactory {
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener electionCalculationListener;
	private final AmbiguityResolver ambiguityResolver;

	public DefaultElectionCalculationFactory(QuorumCalculation quorumCalculation,
	                                         ElectionCalculationListener electionCalculationListener,
	                                         AmbiguityResolver ambiguityResolver) {
		this.quorumCalculation = validateThat(quorumCalculation, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.ambiguityResolver = validateThat(ambiguityResolver, is(notNullValue()));
	}

	public STVElectionCalculation createElectionCalculation(Election election,
	                                                                      ImmutableCollection<Ballot> ballots) {
		return new STVElectionCalculation(ballots, quorumCalculation, electionCalculationListener, election,
		                                                ambiguityResolver, new WeightedInclusiveGregoryMethod(
			electionCalculationListener));
	}
}
