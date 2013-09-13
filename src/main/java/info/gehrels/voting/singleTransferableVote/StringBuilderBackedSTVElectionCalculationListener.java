package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.Map;
import java.util.Map.Entry;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public final class StringBuilderBackedSTVElectionCalculationListener<T extends Candidate>
	implements STVElectionCalculationListener<T> {
	private final StringBuilder builder;

	public StringBuilderBackedSTVElectionCalculationListener(StringBuilder builder) {
		this.builder = validateThat(builder, is(not(nullValue())));
	}


	@Override
	public void numberOfElectedPositions(int numberOfElectedCandidates, int numberOfSeatsToElect) {
		if (numberOfElectedCandidates < numberOfSeatsToElect) {
			formatLine("Es sind erst %d von %d Plätzen gewählt.", numberOfElectedCandidates, numberOfSeatsToElect);
		} else {
			formatLine("Alle %d Plätze sind gewählt.", numberOfSeatsToElect);
		}
	}

	@Override
	public void electedCandidates(ImmutableSet<T> electedCandidates) {
		formatLine("======================================");
		formatLine("Gewählt sind:");
		for (T electedCandidate : electedCandidates) {
			formatLine(format("\t%s", electedCandidate.name));
		}
	}

	@Override
	public void voteWeightRedistributed(BigFraction excessiveFractionOfVoteWeight, int ballotId,
	                                    BigFraction voteWeight) {
		formatLine(
			"Es werden %f %% der Stimmen weiterverteilt: "
			+ "Stimmzettel %d hat nun ein verbleibendes Stimmgewicht von %f %%.",
			excessiveFractionOfVoteWeight.percentageValue(), ballotId,
			voteWeight.percentageValue());
	}

	@Override
	public void delegatingToExternalAmbiguityResolution(ImmutableSet<T> bestCandidates) {
		formatLine("Mehrere Stimmgleiche Kandidierende: %s. Delegiere an externes Auswahlverfahren.", bestCandidates);
	}

	@Override
	public void externalyResolvedAmbiguity(AmbiguityResolverResult<T> ambiguityResolverResult) {
		formatLine("externes Auswahlverfahren ergab: %s. (%s)", ambiguityResolverResult.chosenCandidate.name,
		           ambiguityResolverResult.auditLog);
	}

	@Override
	public void candidateIsElected(Candidate winner, BigFraction numberOfVotes, BigFraction quorum) {
		formatLine("%s hat mit %f Stimmen das Quorum von %f Stimmen erreicht und ist gewählt.", winner.name,
		           numberOfVotes.doubleValue(), quorum.doubleValue());
	}

	@Override
	public void nobodyReachedTheQuorumYet(BigFraction quorum) {
		formatLine("Niemand von den verbleibenden Kandidierenden hat das Quorum von %f Stimmen erreicht:",
		           quorum.doubleValue());
	}

	@Override
	public void noCandidatesAreLeft() {
		formatLine("Es gibt keine hoffnungsvollen Kandidierenden mehr. Der Wahlgang wird daher beendet.");
	}

	@Override
	public void quorumHasBeenCalculated(int numberOfValidBallots, int numberOfSeats, BigFraction quorum) {
		formatLine("Das Quorum liegt bei %f (%d Sitze, %d gültige Stimmen).", quorum.doubleValue(), numberOfSeats,
		           numberOfValidBallots);
	}

	@Override
	public void calculationStarted(Election<T> election, Map<T, BigFraction> votesByCandidate) {

		formatLine("Beginne die Berechnung für Wahl „%s“. Ausgangsstimmverteilung:", election.getOfficeName());
		dumpVoteDistribution(votesByCandidate);
	}

	@Override
	public void voteWeightRedistributionCompleted(Map<T, BigFraction> votesByCandidate) {
		formatLine("Neue Stimmverteilung:");
		dumpVoteDistribution(votesByCandidate);
	}

	@Override
	public void candidateDropped(Map<T, BigFraction> votesByCandidateBeforeStriking, T candidate,
	                             BigFraction weakestVoteCount, Map<T, BigFraction> votesByCandidateAfterStriking) {
		formatLine("%s hat mit %f Stimmen das schlechteste Ergebnis und scheidet aus.", candidate.name,
		           weakestVoteCount.doubleValue());
		formatLine("Neue Stimmverteilung:");
		dumpVoteDistribution(votesByCandidateAfterStriking);
	}

	private <CANDIDATE_TYPE extends Candidate> void dumpVoteDistribution(
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidate) {
		for (Entry<CANDIDATE_TYPE, BigFraction> candidateDoubleEntry : votesByCandidate.entrySet()) {
			formatLine("\t%s: %f Stimmen", candidateDoubleEntry.getKey().name,
			           candidateDoubleEntry.getValue().doubleValue());
		}
	}

	private StringBuilder formatLine(String formatString, Object... objects) {
		return builder.append(format(formatString, objects)).append("\n");
	}
}
