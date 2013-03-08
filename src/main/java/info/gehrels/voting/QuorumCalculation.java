package info.gehrels.voting;

public interface QuorumCalculation {
	double calculateQuorum(int numberOfValidBallots, int numberOfSeats);
}
