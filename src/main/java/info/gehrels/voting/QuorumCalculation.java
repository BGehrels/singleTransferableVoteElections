package info.gehrels.voting;

import org.apache.commons.math3.fraction.BigFraction;

public interface QuorumCalculation {
	BigFraction calculateQuorum(long numberOfValidVotes, long numberOfSeats);
}
