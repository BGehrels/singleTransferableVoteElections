package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.filter;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static java.lang.Math.max;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class ElectionCalculationWithFemaleExclusivePositions {
	private final ElectionCalculationFactory electionCalculationFactory;
	private final ElectionCalculationListener electionCalculationListener;

	public ElectionCalculationWithFemaleExclusivePositions(ElectionCalculationFactory electionCalculationFactory,
	                                                       ElectionCalculationListener electionCalculationListener) {
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.electionCalculationFactory = validateThat(electionCalculationFactory, is(notNullValue()));
	}

	public ElectionResult calculateElectionResult(Election election, ImmutableCollection<Ballot> ballots) {
		validateThat(election, is(notNullValue()));
		validateThat(ballots, is(notNullValue()));

		STVElectionCalculation electionCalculation = electionCalculationFactory
			.createElectionCalculation(election, ballots);

		ImmutableSet<Candidate> electedFemaleCandidates = calculateElectionResultForFemaleExclusivePositions(
			election, electionCalculation);

		ImmutableSet<Candidate> candidatesElectedInOpenRun = calculateElectionResultForNonFemaleExclusivePositions(
			election, electionCalculation, electedFemaleCandidates);

		return new ElectionResult(electedFemaleCandidates, candidatesElectedInOpenRun);
	}

	private ImmutableSet<Candidate> calculateElectionResultForFemaleExclusivePositions(Election election,
	                                                                                   STVElectionCalculation electionCalculation) {
		FemalePredicate femalePredicate = new FemalePredicate(electionCalculationListener);
		ImmutableSet<Candidate> femaleCandidates =
			copyOf(
				filter(election.candidates, femalePredicate)
			);
		return electionCalculation.calculate(femaleCandidates,
		                                                      election.numberOfFemaleExclusivePositions);
	}

	private ImmutableSet<Candidate> calculateElectionResultForNonFemaleExclusivePositions(Election election,
	                                                                                      STVElectionCalculation electionCalculation,
	                                                                                      ImmutableSet<Candidate> electedFemaleCandidates) {
		int numberOfElectableNotFemaleExclusivePositions =
			max(
				0,
				election.numberOfNotFemaleExclusivePositions
				- (election.numberOfFemaleExclusivePositions - electedFemaleCandidates.size())
			);

		NotElectedBeforePredicate notElectedBeforePredicate = new NotElectedBeforePredicate(electedFemaleCandidates,
		                                                                                    electionCalculationListener);
		ImmutableSet<Candidate> candidatesNotElectedBefore =
			copyOf(
				filter(election.candidates, notElectedBeforePredicate)
			);

		return electionCalculation
			.calculate(candidatesNotElectedBefore, numberOfElectableNotFemaleExclusivePositions);
	}


	public static class ElectionResult {
		public final ImmutableSet<Candidate> candidatesElectedInFemaleOnlyRun;
		public final ImmutableSet<Candidate> candidatesElectedInOpenRun;

		private ElectionResult(ImmutableSet<Candidate> candidatesElectedInFemaleOnlyRun,
		                       ImmutableSet<Candidate> candidatesElectedInOpenRun) {

			this.candidatesElectedInFemaleOnlyRun = candidatesElectedInFemaleOnlyRun;
			this.candidatesElectedInOpenRun = candidatesElectedInOpenRun;
		}
	}

}
