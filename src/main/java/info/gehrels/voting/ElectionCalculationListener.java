package info.gehrels.voting;

public interface ElectionCalculationListener {
	void quorumHasBeenCalculated(boolean b, double femaleQuorum);
}
