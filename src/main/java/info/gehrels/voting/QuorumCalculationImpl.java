package info.gehrels.voting;

public class QuorumCalculationImpl implements QuorumCalculation {

	private final double surplus;

	public QuorumCalculationImpl(double surplus) {
		this.surplus = surplus;
	}

	@Override
	public double calculateQuorum(int numberOfValidBallots, int numberOfSeats) {
		return numberOfValidBallots / (numberOfSeats + 1.0) + surplus;
	}
}
