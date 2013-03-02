package info.gehrels.voting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogger implements ElectionCalculationListener {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Override
	public void quorumHasBeenCalculated(boolean femaleExclusive, double quorum) {
		if (femaleExclusive) {
			LOGGER.info("Das Quorum für die Frauenplätze liegt bei {}", quorum);
		} else {
			LOGGER.info("Das Quorum für die Offenen Plätze liegt bei " + quorum);
		}
	}
}
