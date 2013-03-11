package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.filter;

public class ElectionCalculation {
	private final AmbiguityResolver ambiguityResolver;
	private final ElectionCalculationListener electionCalculationListener;
	private final QuorumCalculation quorumCalculation;

	public ElectionCalculation(QuorumCalculation quorumCalculation, AmbiguityResolver ambiguityResolver,
	                           ElectionCalculationListener electionCalculationListener) {
		this.electionCalculationListener = electionCalculationListener;
		this.ambiguityResolver = ambiguityResolver;
		this.quorumCalculation = quorumCalculation;
	}

	public ElectionResult calculateElectionResult(Election election, ImmutableCollection<Ballot> ballots) {
		ElectionCalculationForQualifiedGroup electionCalculationForQualifiedGroup = new ElectionCalculationForQualifiedGroup(
			ballots, quorumCalculation, electionCalculationListener, election, ambiguityResolver);

		ImmutableSet<Candidate> electedFemaleCandidates = calculateElectionResultForFemaleExclusivePositions(
			election, electionCalculationForQualifiedGroup);
		ImmutableSet<Candidate> candidatesElectedInOpenRun = calculateElectionResultForNonFemaleExclusivePositions(
			election, electionCalculationForQualifiedGroup, electedFemaleCandidates);

		return new ElectionResult(electedFemaleCandidates, candidatesElectedInOpenRun);
	}

	private ImmutableSet<Candidate> calculateElectionResultForFemaleExclusivePositions(Election election,
	                                                                                   ElectionCalculationForQualifiedGroup electionCalculationForQualifiedGroup) {
		FemalePredicate femalePredicate = new FemalePredicate(electionCalculationListener);
		ImmutableSet<Candidate> femaleCandidates =
			copyOf(
				filter(election.candidates, femalePredicate)
			);
		return electionCalculationForQualifiedGroup.calculate(femaleCandidates,
		                                                      election.numberOfFemaleExclusivePositions);
	}

	private ImmutableSet<Candidate> calculateElectionResultForNonFemaleExclusivePositions(Election election,
	                                                                                      ElectionCalculationForQualifiedGroup electionCalculationForQualifiedGroup,
	                                                                                      ImmutableSet<Candidate> electedFemaleCandidates) {
		NotElectedBeforePredicate notElectedBeforePredicate = new NotElectedBeforePredicate(electedFemaleCandidates,
		                                                                                    electionCalculationListener);
		ImmutableSet<Candidate> candidatesNotElectedBefore =
			copyOf(
			  filter(election.candidates, notElectedBeforePredicate)
			);

		return electionCalculationForQualifiedGroup
			.calculate(candidatesNotElectedBefore, election.numberOfNotFemaleExclusivePositions);
	}


	public static class ElectionResult {
		public final ImmutableSet<Candidate> candidatesElectedInFemaleOnlyRun;
		public final ImmutableSet<Candidate> candidatesElectedInOpenRun;

		public ElectionResult(ImmutableSet<Candidate> candidatesElectedInFemaleOnlyRun,
		                      ImmutableSet<Candidate> candidatesElectedInOpenRun) {

			this.candidatesElectedInFemaleOnlyRun = candidatesElectedInFemaleOnlyRun;
			this.candidatesElectedInOpenRun = candidatesElectedInOpenRun;
		}
	}

}
