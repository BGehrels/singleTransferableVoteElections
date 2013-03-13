package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public class DefaultElectionCalculationFactoryTest {
	private final QuorumCalculation quorumCalculation = mock(QuorumCalculation.class);
	private final ElectionCalculationListener electionCalculationListener = mock(ElectionCalculationListener.class);
	private final AmbiguityResolver ambiguityResolver = mock(AmbiguityResolver.class);

	@Test
	public void returnsAElectionCalculationForQualifiedGroupInstance() {
		DefaultElectionCalculationFactory factoryUnderTest =
			new DefaultElectionCalculationFactory(quorumCalculation, electionCalculationListener, ambiguityResolver);

		STVElectionCalculation electionCalculation = factoryUnderTest
			.createElectionCalculation(mock(Election.class), mock(ImmutableCollection.class));

		assertThat(electionCalculation, is(notNullValue()));

	}
}
