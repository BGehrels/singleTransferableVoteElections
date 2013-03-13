package info.gehrels.voting;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class DefaultQuorumCalculationImpl implements QuorumCalculation {

	private final double surplus;

	public DefaultQuorumCalculationImpl(double surplus) {
		this.surplus = validateThat(surplus, is(greaterThan(0.0)));
	}

	@Override
	public double calculateQuorum(int numberOfValidBallots, int numberOfSeats) {
		return numberOfValidBallots / (numberOfSeats + 1.0) + surplus;
	}
}
