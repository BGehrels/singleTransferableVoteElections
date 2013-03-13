package info.gehrels.voting;

import org.junit.Test;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;

public class DefaultQuorumCalculationImplTest {

	@Test
	public void returnsCorrectQuorumForZeroBallotsAndZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(0.1);
		double quorum = defaultQuorumCalculation.calculateQuorum(0, 0);

		validateThat(quorum, is(0.1));
	}

	@Test
	public void returnsCorrectQuorumForZeroBallotsAndNonZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(0.1);
		double quorum = defaultQuorumCalculation.calculateQuorum(0, 4);

		validateThat(quorum, is(0.1));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroBallotsAndZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(0.1);
		double quorum = defaultQuorumCalculation.calculateQuorum(4, 0);

		validateThat(quorum, is(4.1));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroBallotsAndNonZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(0.1);
		double quorum = defaultQuorumCalculation.calculateQuorum(6, 2);

		validateThat(quorum, is(2.1));
	}


}
