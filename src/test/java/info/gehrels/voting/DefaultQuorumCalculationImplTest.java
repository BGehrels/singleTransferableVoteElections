package info.gehrels.voting;

import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;

public class DefaultQuorumCalculationImplTest {

	public static final BigFraction ONE_TENTH = new BigFraction(1, 10);

	@Test
	public void returnsCorrectQuorumForZeroBallotsAndZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(new BigFraction(1,10));
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(0, 0);

		validateThat(quorum, is(ONE_TENTH));
	}

	@Test
	public void returnsCorrectQuorumForZeroBallotsAndNonZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(new BigFraction(1,10));
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(0, 4);

		validateThat(quorum, is(ONE_TENTH));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroBallotsAndZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(new BigFraction(1,10));
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(4, 0);

		validateThat(quorum, is(new BigFraction(41,10)));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroBallotsAndNonZeroPostitions() {
		DefaultQuorumCalculationImpl defaultQuorumCalculation = new DefaultQuorumCalculationImpl(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(6, 2);

		validateThat(quorum, is(new BigFraction(21,10)));
	}


}
