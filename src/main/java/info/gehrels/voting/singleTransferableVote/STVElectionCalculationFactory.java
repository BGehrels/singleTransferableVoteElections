package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculationFactory;
import info.gehrels.voting.ElectionCalculationListener;
import info.gehrels.voting.QuorumCalculation;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class STVElectionCalculationFactory<CANDIDATE_TYPE extends Candidate> implements
	ElectionCalculationFactory<CANDIDATE_TYPE> {
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;
	private final AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver;

	public STVElectionCalculationFactory(QuorumCalculation quorumCalculation,
	                                     ElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener,
	                                     AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver) {
		this.quorumCalculation = validateThat(quorumCalculation, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.ambiguityResolver = validateThat(ambiguityResolver, is(notNullValue()));
	}

	@Override
	public final STVElectionCalculation<CANDIDATE_TYPE> createElectionCalculation(Election<CANDIDATE_TYPE> election,
	                                                                      ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots) {
		return new STVElectionCalculation<>(ballots, quorumCalculation, electionCalculationListener, election,
		                                                ambiguityResolver, new WeightedInclusiveGregoryMethod<>(
			electionCalculationListener));
	}
}
