package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculationListener;
import info.gehrels.voting.QuorumCalculation;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public class STVElectionCalculationFactoryTest {
	private final QuorumCalculation quorumCalculation = mock(QuorumCalculation.class);
	private final ElectionCalculationListener<Candidate> electionCalculationListener = mock(ElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolver = mock(AmbiguityResolver.class);

	@Test
	public void returnsAElectionCalculationForQualifiedGroupInstance() {
		STVElectionCalculationFactory<?> factoryUnderTest =
			new STVElectionCalculationFactory<>(quorumCalculation, electionCalculationListener, ambiguityResolver);

		STVElectionCalculation<?> electionCalculation = factoryUnderTest
			.createElectionCalculation(mock(Election.class), mock(ImmutableCollection.class));

		assertThat(electionCalculation, is(notNullValue()));

	}
}
