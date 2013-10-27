package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.QuorumCalculation;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public final class STVElectionCalculationFactoryTest {
	private final QuorumCalculation quorumCalculation = mock(QuorumCalculation.class);
	private final STVElectionCalculationListener<Candidate> electionCalculationListener = mock(STVElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolver = mock(AmbiguityResolver.class);

	@Test
	public void returnsASTVElectionCalculationInstance() {
		STVElectionCalculationFactory<Candidate> factoryUnderTest =
			new STVElectionCalculationFactory<>(quorumCalculation, electionCalculationListener, ambiguityResolver);

		ImmutableCollection<Ballot<Candidate>> candidates= ImmutableList.of();
		Election<Candidate> election = mock(Election.class);
		STVElectionCalculation<Candidate> electionCalculation = factoryUnderTest.createElectionCalculation(election, candidates);

		assertThat(electionCalculation, is(notNullValue()));

	}
}
