package info.gehrels.voting;

import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum implements QuorumCalculation {

	private final BigFraction surplus;

	public NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(BigFraction surplus) {
		this.surplus = validateThat(surplus, is(greaterThan(BigFraction.ZERO)));
	}

	@Override
	public BigFraction calculateQuorum(int numberOfValidBallots, int numberOfSeats) {
		return new BigFraction(numberOfValidBallots, (numberOfSeats + 1)).add(surplus);
	}


}
