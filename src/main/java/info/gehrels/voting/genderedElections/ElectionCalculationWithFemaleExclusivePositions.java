package info.gehrels.voting.genderedElections;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.ElectionCalculation;
import info.gehrels.voting.ElectionCalculationFactory;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.filter;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.lang.Math.max;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ElectionCalculationWithFemaleExclusivePositions {
	private final ElectionCalculationFactory<GenderedCandidate> electionCalculationFactory;
	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener;

	// TODO: Validate, that Ballots without a female vote are invalid (are they?)
	public ElectionCalculationWithFemaleExclusivePositions(
		ElectionCalculationFactory<GenderedCandidate> electionCalculationFactory,
		ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener) {
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.electionCalculationFactory = validateThat(electionCalculationFactory, is(notNullValue()));
	}

	public ElectionResult calculateElectionResult(GenderedElection election,
	                                              ImmutableCollection<Ballot<GenderedCandidate>> ballots) {
		validateThat(election, is(notNullValue()));
		validateThat(ballots, is(notNullValue()));

		ElectionCalculation<GenderedCandidate> electionCalculation =
			electionCalculationFactory.createElectionCalculation(election, ballots);

		ImmutableSet<GenderedCandidate> electedFemaleCandidates = calculateElectionResultForFemaleExclusivePositions(
			election, electionCalculation);

		ImmutableSet<GenderedCandidate> candidatesElectedInOpenRun = calculateElectionResultForNonFemaleExclusivePositions(
			election, electionCalculation, electedFemaleCandidates);

		return new ElectionResult(electedFemaleCandidates, candidatesElectedInOpenRun);
	}

	private ImmutableSet<GenderedCandidate> calculateElectionResultForFemaleExclusivePositions(
		GenderedElection election, ElectionCalculation<GenderedCandidate> electionCalculation) {
		FemalePredicate femalePredicate = new FemalePredicate(electionCalculationListener);
		ImmutableSet<GenderedCandidate> femaleCandidates =
			copyOf(
				filter(election.getCandidates(), femalePredicate)
			);
		return electionCalculation.calculate(femaleCandidates,
		                                     election.getNumberOfFemaleExclusivePositions());
	}

	private ImmutableSet<GenderedCandidate> calculateElectionResultForNonFemaleExclusivePositions(
		GenderedElection election, ElectionCalculation<GenderedCandidate> electionCalculation,
		ImmutableSet<GenderedCandidate> electedFemaleCandidates) {
		int numberOfElectableNotFemaleExclusivePositions =
			max(
				0,
				election.getNumberOfNotFemaleExclusivePositions()
				- (election.getNumberOfFemaleExclusivePositions() - electedFemaleCandidates.size())
			);

		if (numberOfElectableNotFemaleExclusivePositions < election.getNumberOfNotFemaleExclusivePositions()) {
			electionCalculationListener.reducedNonFemaleExclusiveSeats(election.getNumberOfFemaleExclusivePositions(),
			                                                           electedFemaleCandidates.size(),
			                                                           election
				                                                           .getNumberOfNotFemaleExclusivePositions(),
			                                                           numberOfElectableNotFemaleExclusivePositions);
		}

		NotElectedBeforePredicate notElectedBeforePredicate = new NotElectedBeforePredicate(electedFemaleCandidates,
		                                                                                    electionCalculationListener);
		ImmutableSet<GenderedCandidate> candidatesNotElectedBefore =
			copyOf(
				filter(election.getCandidates(), notElectedBeforePredicate)
			);

		return electionCalculation
			.calculate(candidatesNotElectedBefore, numberOfElectableNotFemaleExclusivePositions);
	}


	public static class ElectionResult {
		public final ImmutableSet<GenderedCandidate> candidatesElectedInFemaleOnlyRun;
		public final ImmutableSet<GenderedCandidate> candidatesElectedInOpenRun;

		private ElectionResult(ImmutableSet<GenderedCandidate> candidatesElectedInFemaleOnlyRun,
		                       ImmutableSet<GenderedCandidate> candidatesElectedInOpenRun) {

			this.candidatesElectedInFemaleOnlyRun = candidatesElectedInFemaleOnlyRun;
			this.candidatesElectedInOpenRun = candidatesElectedInOpenRun;
		}
	}

}
