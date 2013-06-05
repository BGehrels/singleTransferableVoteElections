package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.genderedElections.ElectionCalculationWithFemaleExclusivePositionsListener;
import info.gehrels.voting.genderedElections.GenderedCandidate;
import org.apache.commons.math3.fraction.BigFraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Map.Entry;

public class AuditLogger implements
	ElectionCalculationWithFemaleExclusivePositionsListener, ElectionCalculationListener<GenderedCandidate> {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Override
	public void quorumHasBeenCalculated(int numberOfValidBallots, int numberOfSeats, BigFraction quorum) {
		LOGGER.info("Das Quorum liegt bei {} ({} Sitze, {} gültige Stimmen).", quorum.doubleValue(), numberOfSeats,
		            numberOfValidBallots);
	}

	@Override
	public void reducedNonFemaleExclusiveSeats(int numberOfOpenFemaleExclusiveSeats,
	                                           int numberOfElectedFemaleExclusiveSeats,
	                                           int numberOfOpenNonFemaleExclusiveSeats,
	                                           int numberOfElectableNonFemaleExclusiveSeats) {
		LOGGER.info(
			"Es wurden nur {} von {} Frauenplätzen besetzt. Daher können auch nur {} von {} offenen Plätzen gewählt werden.",
			numberOfElectedFemaleExclusiveSeats, numberOfOpenFemaleExclusiveSeats,
			numberOfElectableNonFemaleExclusiveSeats, numberOfOpenNonFemaleExclusiveSeats);
	}

	@Override
	public void numberOfElectedPositions(int numberOfElectedCandidates, int numberOfSeatsToElect) {
		if (numberOfElectedCandidates < numberOfSeatsToElect) {
			LOGGER.info("Es sind erst {} von {} Plätze sind gewählt.", numberOfElectedCandidates, numberOfSeatsToElect,
			            "Plätzen");
		} else {
			LOGGER.info("Alle {} Plätze sind gewählt.", numberOfSeatsToElect);
		}
	}

	@Override
	public void electedCandidates(ImmutableSet<GenderedCandidate> electedCandidates) {
		LOGGER.info("======================================");
		LOGGER.info("Gewählt sind: ");
		for (GenderedCandidate electedCandidate : electedCandidates) {
			LOGGER.info("\t{}", electedCandidate.name);
		}
	}

	@Override
	public  void candidateDropped(Map<GenderedCandidate, BigFraction> votesByCandidateBeforeStriking,
	                              GenderedCandidate candidate, BigFraction weakestVoteCount,
	                             Map<GenderedCandidate, BigFraction> votesByCandidateAfterStriking) {

		LOGGER.info("{} hat mit {} Stimmen das schlechteste Ergebnis und scheidet aus.", candidate.name,
		            weakestVoteCount.doubleValue());
		LOGGER.info("Neue Stimmverteilung:");
		dumpVoteDistribution(votesByCandidateAfterStriking);
	}

	@Override
	public void voteWeightRedistributed(BigFraction excessiveFractionOfVoteWeight, int ballotId,
	                                    BigFraction voteWeight) {
		LOGGER.info(
			"Es werden {} % der Stimmen weiterverteilt: "
			+ "Stimmzettel {} hat nun ein verbleibendes Stimmgewicht von {} %.",
			excessiveFractionOfVoteWeight.multiply(100).doubleValue(), ballotId,
			voteWeight.multiply(100).doubleValue());
	}

	@Override
	public void voteWeightRedistributionCompleted(Map<GenderedCandidate, BigFraction> votesByCandidate) {
		LOGGER.info("Neue Stimmverteilung:");
		dumpVoteDistribution(votesByCandidate);
	}

	@Override
	public void delegatingToExternalAmbiguityResolution(ImmutableSet<GenderedCandidate> bestCandidates) {
		LOGGER.info("Mehrere Stimmgleiche Kandidierende: {}. Delegiere an externes Auswahlverfahren.", bestCandidates);
	}

	@Override
	public void externalyResolvedAmbiguity(AmbiguityResolverResult<GenderedCandidate> ambiguityResolverResult) {
		LOGGER.info("externes Auswahlverfahren ergab: {}. ({})", ambiguityResolverResult.chosenCandidate.name,
		            ambiguityResolverResult.auditLog);
	}

	@Override
	public void candidateIsElected(GenderedCandidate winner, BigFraction numberOfVotes, BigFraction quorum) {
		LOGGER.info("{} hat mit {} Stimmen das Quorum von {} Stimmen erreicht und ist gewählt.", winner.name,
		            numberOfVotes.doubleValue(), quorum.doubleValue());
	}

	@Override
	public void nobodyReachedTheQuorumYet(BigFraction quorum) {
		LOGGER.info("Niemand von den verbleibenden Kandidierenden hat das Quorum von {} Stimmen erreicht:",
		            quorum.doubleValue());
	}

	@Override
	public void noCandidatesAreLeft() {
		LOGGER.info("Es gibt keine hoffnungsvollen Kandidierenden mehr. Der Wahlgang wird daher beendet.");
	}

	@Override
	public void calculationStarted(Election<GenderedCandidate> election, Map<GenderedCandidate, BigFraction> voteDistribution) {
		LOGGER.info("Beginne die Berechnung für Wahl „{}“. Ausgangsstimmverteilung:",
		            election.office.name);
		dumpVoteDistribution(voteDistribution);
	}

	@Override
	public void candidateNotQualified(GenderedCandidate candidate, String reason) {
		LOGGER.info("{} kann in diesem Wahlgang nicht antreten, Grund: {}", candidate.name, reason);
	}

	private <CANDIDATE_TYPE extends Candidate> void dumpVoteDistribution(
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidate) {
		for (Entry<CANDIDATE_TYPE, BigFraction> candidateDoubleEntry : votesByCandidate.entrySet()) {
			LOGGER.info("\t{}: {} Stimmen", candidateDoubleEntry.getKey().name,
			            candidateDoubleEntry.getValue().doubleValue());
		}
	}
}