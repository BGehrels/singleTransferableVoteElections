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
	public final BigFraction calculateQuorum(long numberOfValidVotes, long numberOfSeats) {
		BigFraction calculatedQuorum = new BigFraction(numberOfValidVotes, (numberOfSeats + 1)).add(surplus);
		return correctToBoundariesIfNeccessary(numberOfValidVotes, numberOfSeats, calculatedQuorum);
	}

	private BigFraction correctToBoundariesIfNeccessary(long numberOfValidVotes, long numberOfSeats,
	                                                    BigFraction calculatedQuorum) {
		if ((numberOfValidVotes != 0) && (numberOfSeats != 0)) {
			return min(new BigFraction(numberOfValidVotes), calculatedQuorum);
		} else {
			return calculatedQuorum;
		}
	}

	private BigFraction min(BigFraction a, BigFraction b) {
		if (a.compareTo(b) < 0) {
			return a;
		} else {
			return b;
		}
	}


}
