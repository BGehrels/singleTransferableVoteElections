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
			LOGGER.info("Das Quorum für die Offenen Plätze liegt bei {}", quorum);
		}
	}

	@Override
	public void numberOfElectedPositions(boolean femaleSeat, int numberOfElectedCandidates, int numberOfSeatsToElect) {
		String typeOfSeat = femaleSeat ? "Frauenplätzen" : "offenen Plätzen";
		if (numberOfElectedCandidates < numberOfSeatsToElect) {
			LOGGER.info("Es sind erst {} von {} {} gewählt.", numberOfElectedCandidates, numberOfSeatsToElect,
			            typeOfSeat);
		} else {
			LOGGER.info("Alle {} {} sind gewählt.", numberOfSeatsToElect, typeOfSeat);
		}
	}
}
