package info.gehrels.voting;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Sets.filter;

public class ElectionCalculation {
	private final Election election;
	private final ImmutableCollection<Ballot> ballots;
	private final int numberOfFemaleSeats;
	private final int numberOfOpenSeats;
	private final AmbiguityResolver ambiguityResolver;
	private final ElectionCalculationListener electionCalculationListener;
	private QuorumCalculation quorumCalculation;

	public ElectionCalculation(Election election, ImmutableCollection<Ballot> ballots,
	                           QuorumCalculation quorumCalculation, AmbiguityResolver ambiguityResolver,
	                           ElectionCalculationListener electionCalculationListener) {
		this.election = election;
		this.ballots = ballots;
		this.electionCalculationListener = electionCalculationListener;
		this.numberOfFemaleSeats = election.numberOfFemaleExclusivePositions;
		this.numberOfOpenSeats = election.numberOfNotFemaleExclusivePositions;
		this.ambiguityResolver = ambiguityResolver;
		this.quorumCalculation = quorumCalculation;
	}

	public ElectionResult calculateElectionResult() {
		ImmutableSet<Candidate> femaleCandidates = copyOf(filter(election.candidates,
		                                                         new FemalePredicate(electionCalculationListener)));
		ElectionCalculationForQualifiedGroup electionCalculationForQualifiedGroup = new ElectionCalculationForQualifiedGroup(
			ballots, quorumCalculation, electionCalculationListener, election, ambiguityResolver);

		ImmutableSet<Candidate> electedFemaleCandidates = electionCalculationForQualifiedGroup
			.calculate(femaleCandidates, numberOfFemaleSeats);
		NotElectedBeforePredicate notElectedBeforePredicate = new NotElectedBeforePredicate(electedFemaleCandidates,
		                                                                                    electionCalculationListener);
		ImmutableSet<Candidate> candidatesNotElectedBefore = copyOf(
			filter(election.candidates, notElectedBeforePredicate));

		ImmutableSet<Candidate> candidatesElectedInOpenRun = electionCalculationForQualifiedGroup
			.calculate(candidatesNotElectedBefore, numberOfOpenSeats);

		return new ElectionResult(electedFemaleCandidates, candidatesElectedInOpenRun);
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
