package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

import static info.gehrels.voting.singleTransferableVote.VotesForCandidateCalculation.calculateVotesForCandidate;

public class WeightedInclusiveGregoryMethod<CANDIDATE_TYPE extends Candidate> implements
	VoteWeightRecalculationMethod<CANDIDATE_TYPE> {
	private final STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;

	public WeightedInclusiveGregoryMethod(STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public final VoteWeightRecalculator<CANDIDATE_TYPE> recalculatorFor() {
		return new WigmVoteWeightRecalculator<CANDIDATE_TYPE>();
	}

	private final class WigmVoteWeightRecalculator<CANDIDATE_TYPE extends Candidate>
		implements VoteWeightRecalculator<CANDIDATE_TYPE> {
		@Override
		public ImmutableList<VoteState<CANDIDATE_TYPE>> recalculateExceededVoteWeight(CANDIDATE_TYPE winner,
		                                                                              BigFraction quorum,
		                                                                              ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
		                                                                              CandidateStates<CANDIDATE_TYPE> candidateStates) {
			Builder<VoteState<CANDIDATE_TYPE>> resultBuilder = ImmutableList.builder();

			// TODO: Rundungsregeln bei der Stimmgewichtsübertragung aus der Satzung streichen.
			// § 18 S. 2 Nr. 7 sub (III)
			// - Zunächst wird der Übertragungswert ermittelt: Der Übertragungswert ist der	Überschuss der gewählten
			// Kandidatin / des gewählten Kandidaten geteilt durch ihre / seine Stimmenzahl.
			// - Auf Grundlage des Übertragungswerts wird der Stimmwert der jeweiligen Stimme ermittelt: Der Stimmwert
			// ist der bisherige Stimmwert multipliziert mit dem Übertragungswert, abgerundet auf die sieben
			// Nachkommastellen.
			// - Die Stimmen werden mit ihrem gegenwärtigen Stimmwert jeweils auf diejenige	Kandidatin / denjenigen
			// Kandidaten übertragen, auf die / den die nächste Präferenz der jeweiligen Wählerin / des jeweiligen
			// Wählers lautet. Falls die / der dort benannte KandidatIn entweder bereits für gewählt erklärt wurde oder
			// bereits aus dem Rennen ausgeschieden ist, wird die Stimme auf die / den nächsteN noch im Rennen
			// befindlichen KandidatIn übertragen. Die Stimmenzahl der betreffen den KandidatInnen wird neu
			// festgestellt.
			// TODO: Mit Hilfe des letzten Stichpunktes ließe sich gut die synchrone Stimmgewichtsübertragung bei
			// TODO: mehreren das Quorum überschreitenden Personen realisieren: Dort ist von den "für gewählt erklärt"en
			// TODO: Kandidatinnen die Rede. Mensch erklärt einfach alle (oder alle mit totalem Patt) für gewählt
			BigFraction votesForCandidate = calculateVotesForCandidate(winner, voteStates);
			BigFraction excessiveVotes = votesForCandidate.subtract(quorum);
			BigFraction excessiveFractionOfVoteWeight = excessiveVotes.divide(votesForCandidate);

			electionCalculationListener.redistributingExcessiveFractionOfVoteWeight(winner, excessiveFractionOfVoteWeight);

			for (VoteState<CANDIDATE_TYPE> voteState : voteStates) {
				if (voteState.getPreferredCandidate().orNull() == winner) {
					BigFraction newVoteWeight = voteState.getVoteWeight().multiply(excessiveFractionOfVoteWeight);
					VoteState<CANDIDATE_TYPE> newVoteState = voteState.withVoteWeight(newVoteWeight);
					resultBuilder.add(newVoteState);
				} else {
					resultBuilder.add(voteState);
				}
			}

			return resultBuilder.build();

		}

	}
}
