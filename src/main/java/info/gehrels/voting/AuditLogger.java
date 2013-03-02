package info.gehrels.voting;

public class AuditLogger implements ElectionCalculationListener {
	@Override
	public void quorumHasBeenCalculated(boolean femaleExclusive, double quorum) {
		if (femaleExclusive) {
			System.out.println("Das Quorum für die Frauenplätze liegt bei " + quorum);
		} else {
			System.out.println("Das Quorum für die Offenen Plätze liegt bei " + quorum);
		}
	}
}
