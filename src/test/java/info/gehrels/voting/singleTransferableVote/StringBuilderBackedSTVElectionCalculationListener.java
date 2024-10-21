/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Library. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

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
	public void redistributingExcessiveFractionOfVoteWeight(T winner,
	                                                        BigFraction excessiveFractionOfVoteWeight) {
		formatLine("Es werden %f%% des Stimmgewichts von %s weiterverteilt.",
		           excessiveFractionOfVoteWeight.percentageValue(), winner.getName());
	}

	@Override
	public void delegatingToExternalAmbiguityResolution(ImmutableSet<T> bestCandidates) {
		formatLine("Mehrere stimmengleiche Kandidierende: %s. Delegiere an externes Auswahlverfahren.", bestCandidates);
	}

	/*
	 * § 19 Abs. 4 WahlO-GJ:
	 * Sofern Zufallsauswahlen gemäß § 18 Nr. 7, 8 erforderlich sind, entscheidet das von der Tagungsleitung zu ziehende
	 * Los; die Ziehung und die Eingabe des Ergebnisses in den Computer müssen mitgliederöffentlich erfolgen.
	 */
	@Override
	public void externallyResolvedAmbiguity(AmbiguityResolverResult<T> ambiguityResolverResult) {
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
	public void calculationStarted(Election<T> election, VoteDistribution<T> voteDistribution) {
		formatLine("Ausgangsstimmverteilung:");
		dumpVoteDistribution(voteDistribution);
	}

	@Override
	public void voteWeightRedistributionCompleted(ImmutableCollection<VoteState<T>> originalVoteStates,
	                                              ImmutableCollection<VoteState<T>> newVoteStates,
	                                              VoteDistribution<T> voteDistribution) {
		for (VoteStatePair newAndOldState : getMatchingPairs(originalVoteStates, newVoteStates)) {
			VoteState<T> oldState = newAndOldState.oldState;
			VoteState<T> newState = newAndOldState.newState;
			boolean voteWeightChanged = !oldState.getVoteWeight().equals(newState.getVoteWeight());
			boolean preferredCandidateChanged = !oldState.getPreferredCandidate()
				.equals(newState.getPreferredCandidate());
			long ballotId = oldState.getBallotId();
			if (preferredCandidateChanged && voteWeightChanged) {
				formatLine(
					"Das Stimmgewicht von Stimmzettel %d verringert sich von %f%% auf %f%% und wird von %s auf %s übertragen.",
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

		dumpVoteDistribution(voteDistribution);

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
	public void candidateDropped(VoteDistribution<T> voteDistributionBeforeStriking, T candidate) {
		formatLine("%s hat mit %f Stimmen das schlechteste Ergebnis und scheidet aus.", candidate.name,
		           voteDistributionBeforeStriking.votesByCandidate.get(candidate).doubleValue());
	}

	private <CANDIDATE_TYPE extends Candidate> void dumpVoteDistribution(VoteDistribution<CANDIDATE_TYPE> voteDistribution) {
		for (Entry<CANDIDATE_TYPE, BigFraction> votesForCandidate : voteDistribution.votesByCandidate.entrySet()) {
			formatLine("\t%s: %f Stimmen", votesForCandidate.getKey().name,
			           votesForCandidate.getValue().doubleValue());
		}
		formatLine("\tNein: %f Stimmen", voteDistribution.noVotes.doubleValue());
		formatLine("\tUngültig: %f Stimmen", voteDistribution.invalidVotes.doubleValue());
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
