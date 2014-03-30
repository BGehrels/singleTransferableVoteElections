package info.gehrels.voting.singleTransferableVote;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

// § 19 Abs. 3,4 WahlO-GJ:
// (3) Mit der Verkündung des Ergebnisses muss der Versammlung ein detailliertes Protokoll der Programmabläufe zur Verfügung
// gestellt werden. Dieses Protokoll muss mindestens enthalten:
// 1. Das Quorum gemäß § 18 Nr. 2
// 2. Die Wahl von KandidatInnen gemäß § 18 Nr. 5
// 3. Das Ausscheiden von KandidatInnen gemäß § 18 Nr. 8
// 4. Die Anzahl der Stimmen von KandidatInnen zum Zeitpunkt ihrer Wahl oder ihres Ausscheidens
// 5. In Fällen des § 18 Nr. 7, 8 die Anzahl der übertragenen Stimmen, der Gesamtstimmwert dieser Stimmen zum Zeitpunkt
// der Übertragung sowie die Kandidatin / den Kandidaten von der / dem und zu der / dem übertragen wurde.
// (4) Sofern Zufallsauswahlen gemäß § 18 Nr. 7, 8 erforderlich sind, entscheidet das von der Tagungsleitung zu ziehende
// Los; die Ziehung und die Eingabe des Ergebnisses in den Computer müssen mitgliederöffentlich erfolgen.
public final class StringBuilderBackedSTVElectionCalculationListener<T extends Candidate>
	implements STVElectionCalculationListener<T> {
	private final StringBuilder builder;

	public StringBuilderBackedSTVElectionCalculationListener(StringBuilder builder) {
		this.builder = validateThat(builder, is(not(nullValue())));
	}


	@Override
	public void numberOfElectedPositions(long numberOfElectedCandidates, long numberOfSeatsToElect) {
		if (numberOfElectedCandidates < numberOfSeatsToElect) {
			formatLine("Es sind erst %d von %d Plätzen gewählt.", numberOfElectedCandidates, numberOfSeatsToElect);
		} else {
			formatLine("Alle %d Plätze sind gewählt.", numberOfSeatsToElect);
		}
	}

	@Override
	public void electedCandidates(ImmutableSet<T> electedCandidates) {
		if (!electedCandidates.isEmpty()) {
			formatLine("======================================");
			formatLine("Gewählt sind:");
			for (T electedCandidate : electedCandidates) {
				formatLine("\t%s", electedCandidate.name);
			}
		}
	}

	@Override
	public void redistributingExcessiveFractionOfVoteWeight(Candidate winner,
	                                                        BigFraction excessiveFractionOfVoteWeight) {
		formatLine("Es werden %f%% des Stimmgewichts von %s weiterverteilt.",
		           excessiveFractionOfVoteWeight.percentageValue(), winner.getName());
	}

	@Override
	public void delegatingToExternalAmbiguityResolution(ImmutableSet<T> bestCandidates) {
		formatLine("Mehrere Stimmgleiche Kandidierende: %s. Delegiere an externes Auswahlverfahren.", bestCandidates);
	}

	/*
	 * § 19 Abs. 4 WahlO-GJ:
	 * Sofern Zufallsauswahlen gemäß § 18 Nr. 7, 8 erforderlich sind, entscheidet das von der Tagungsleitung zu ziehende
	 * Los; die Ziehung und die Eingabe des Ergebnisses in den Computer müssen mitgliederöffentlich erfolgen.
	 */
	@Override
	public void externalyResolvedAmbiguity(AmbiguityResolverResult<T> ambiguityResolverResult) {
		formatLine("externes Auswahlverfahren ergab: %s. (%s)", ambiguityResolverResult.chosenCandidate.name,
		           ambiguityResolverResult.auditLog);
	}

	/*
	 * § 19 Abs. 3 S. 2 ff WahlO-GJ:
	 * Dieses Protokoll muss mindestens enthalten:
	 * [...]
	 * 2. Die Wahl von KandidatInnen gemäß § 18 Nr. 5
	 * [...]
	 * 4. Die Anzahl der Stimmen von KandidatInnen zum Zeitpunkt ihrer Wahl oder ihres Ausscheidens
	 * [...]
	 */
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

	/*
	 * § 19 Abs. 3 S. 2 ff WahlO-GJ:
	 * Dieses Protokoll muss mindestens enthalten:
	 * 1. Das Quorum gemäß § 18 Nr. 2
	 * [...]
	 */
	@Override
	public void quorumHasBeenCalculated(long numberOfValidBallots, long numberOfSeats, BigFraction quorum) {
		formatLine("Das Quorum liegt bei %f (%d Sitze, %d gültige Stimmen).", quorum.doubleValue(), numberOfSeats,
		           numberOfValidBallots);
	}

	@Override
	public void calculationStarted(Election<T> election, Map<T, BigFraction> votesByCandidate) {
		formatLine("Beginne die Berechnung für Wahl „%s“. Ausgangsstimmverteilung:", election.getOfficeName());
		dumpVoteDistribution(votesByCandidate);
	}

	@Override
	public void voteWeightRedistributionCompleted(ImmutableCollection<VoteState<T>> originalVoteStates,
	                                              ImmutableCollection<VoteState<T>> newVoteStates,
	                                              Map<T, BigFraction> votesByCandidate) {
		for (VoteStatePair newAndOldState : getMatchingPairs(originalVoteStates, newVoteStates)) {
			VoteState<T> oldState = newAndOldState.oldState;
			VoteState<T> newState = newAndOldState.newState;
			boolean voteWeightChanged = !oldState.getVoteWeight().equals(newState.getVoteWeight());
			boolean preferredCandidateChanged = !oldState.getPreferredCandidate()
				.equals(newState.getPreferredCandidate());
			long ballotId = oldState.getBallotId();
			if (preferredCandidateChanged && voteWeightChanged) {
				formatLine(
					"Das Stimmgewicht von Stimmzettel %d verrignert sich von %f%% auf %f%% und wird von %s auf %s übertragen.",
					ballotId, oldState.getVoteWeight().percentageValue(),
					newState.getVoteWeight().percentageValue(), oldState.getPreferredCandidate().get().getName(),
					getNameOrNo(newState.getPreferredCandidate()));
			} else if (preferredCandidateChanged) {
				formatLine("Stimmzettel %d überträgt sein bestehendes Stimmgewicht (%f%%) von %s auf %s", ballotId,
				           oldState.getVoteWeight().percentageValue(), oldState.getPreferredCandidate().get().getName(),
				           getNameOrNo(newState.getPreferredCandidate())
				);
			}
		}


		formatLine("Neue Stimmverteilung:");

		dumpVoteDistribution(votesByCandidate);

	}

	private String getNameOrNo(Optional<T> candidate) {
		return candidate.isPresent() ? candidate.get().name : "Nein";
	}

	private Iterable<VoteStatePair> getMatchingPairs(
		ImmutableCollection<VoteState<T>> originalVoteStates,
		ImmutableCollection<VoteState<T>> newVoteStates) {
		List<VoteStatePair> matchingPairs = new ArrayList<>();

		for (VoteState<T> originalVoteState : originalVoteStates) {
			for (VoteState<T> newVoteState : newVoteStates) {
				if (originalVoteState.getBallotId() == newVoteState.getBallotId()) {
					matchingPairs.add(new VoteStatePair(originalVoteState, newVoteState));
					break;
				}
			}
		}

		return matchingPairs;
	}

	/*
	 * § 19 Abs. 3 S. 2 ff WahlO-GJ:
	 * Dieses Protokoll muss mindestens enthalten:
	 * [...]
	 * 3. Das Ausscheiden von KandidatInnen gemäß § 18 Nr. 8
	 * 4. Die Anzahl der Stimmen von KandidatInnen zum Zeitpunkt ihrer Wahl oder ihres Ausscheidens
	 * [...]
	 */
	@Override
	public void candidateDropped(Map<T, BigFraction> votesByCandidateBeforeStriking, T candidate,
	                             BigFraction weakestVoteCount) {
		formatLine("%s hat mit %f Stimmen das schlechteste Ergebnis und scheidet aus.", candidate.name,
		           weakestVoteCount.doubleValue());
	}

	private <CANDIDATE_TYPE extends Candidate> void dumpVoteDistribution(
		Map<CANDIDATE_TYPE, BigFraction> votesByCandidate) {
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			formatLine("\t%s: %f Stimmen", votesForCandidate.getKey().name,
			           votesForCandidate.getValue().doubleValue());
		}
	}

	private void formatText(String formatString, Object... objects) {
		builder.append(format(formatString, objects));
	}

	private void formatLine(String formatString, Object... objects) {
		formatText(formatString, objects);
		builder.append("\n");
	}


	private final class VoteStatePair {
		public final VoteState<T> oldState;
		public final VoteState<T> newState;

		VoteStatePair(VoteState<T> oldState, VoteState<T> newState) {
			this.oldState = oldState;
			this.newState = newState;
		}

	}
}
