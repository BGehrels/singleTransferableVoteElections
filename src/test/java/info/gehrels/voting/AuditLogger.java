package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
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

	@Override
	public void electedCandidates(ImmutableSet<Candidate> electedCandidates) {
		LOGGER.info("======================================");
		LOGGER.info("Gewählt sind: ");
		for (Candidate electedCandidate : electedCandidates) {
			LOGGER.info("\t{}", electedCandidate.name);
		}
	}

	@Override
	public void candidateDropped(String name, double weakestVoteCount) {
		LOGGER.info("{} hat mit {} Stimmen das schlechteste Ergebnis und scheidet aus.", name, weakestVoteCount);
	}

	@Override
	public void voteWeightRedistributed(double excessiveFractionOfVoteWeight, Ballot ballot, double voteWeight) {
		LOGGER.info(
			"Es werden {}% der Stimmen weiterverteilt: "
			+ "Stimmzettel {} hat nun ein verbleibendes Stimmgewicht von {}.",
			excessiveFractionOfVoteWeight * 100, ballot.id, voteWeight);
	}
}