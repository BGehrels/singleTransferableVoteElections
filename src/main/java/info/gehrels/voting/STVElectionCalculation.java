package info.gehrels.voting;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.STVElectionCalculationStep.ElectionStepResult;
import org.apache.commons.math3.fraction.BigFraction;

import static com.google.common.collect.Collections2.transform;
import static info.gehrels.voting.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import static info.gehrels.voting.VotesByCandidateCalculation.calculateVotesByCandidate;

public class STVElectionCalculation {
	private final ImmutableCollection<Ballot> ballots;
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener electionCalculationListener;
	private final Election election;
	private final VoteWeightRedistributionMethod voteWeightRedistributionMethod;
	private final STVElectionCalculationStep electionStep;

	public STVElectionCalculation(ImmutableCollection<Ballot> ballots,
	                              QuorumCalculation quorumCalculation,
	                              ElectionCalculationListener electionCalculationListener,
	                              Election election, AmbiguityResolver ambiguityResolver,
	                              VoteWeightRedistributionMethod redistributionMethod) {
		this.ballots = ballots;
		this.quorumCalculation = quorumCalculation;
		this.electionCalculationListener = electionCalculationListener;
		this.election = election;
		this.voteWeightRedistributionMethod = redistributionMethod;
		this.electionStep = new STVElectionCalculationStep(electionCalculationListener, ambiguityResolver);
	}

	public ImmutableSet<Candidate> calculate(ImmutableSet<? extends Candidate> qualifiedCandidates, int numberOfSeats) {
		VoteWeightRedistributor redistributor = voteWeightRedistributionMethod.redistributorFor();
		int numberOfValidBallots = ballots.size();
		// Runden oder nicht runden?
		BigFraction quorum = quorumCalculation.calculateQuorum(numberOfValidBallots, numberOfSeats);
		electionCalculationListener.quorumHasBeenCalculated(numberOfValidBallots, numberOfSeats, quorum);

		CandidateStates candidateStates = new CandidateStates(qualifiedCandidates);
		ImmutableCollection<BallotState> ballotStates = constructBallotStates(candidateStates);

		int numberOfElectedCandidates = 0;

		electionCalculationListener
			.calculationStarted(election,
			                    calculateVotesByCandidate(candidateStates.getHopefulCandidates(),
			                                              ballotStates));

		while (notAllSeatsFilled(numberOfElectedCandidates, numberOfSeats) && anyCandidateIsHopeful(candidateStates)) {
			ElectionStepResult electionStepResult = electionStep.declareWinnerOrStrikeCandidate(quorum,
			                                                                                    ballotStates,
			                                                                                    redistributor,
			                                                                                    numberOfElectedCandidates,
			                                                                                    candidateStates);
			candidateStates = electionStepResult.newCandidateStates;
			ballotStates = electionStepResult.newBallotStates;
			numberOfElectedCandidates = electionStepResult.newNumberOfElectedCandidates;
		}

		ImmutableSet<Candidate> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return electedCandidates;
	}

	private ImmutableCollection<BallotState> constructBallotStates(final CandidateStates candidateStates) {
		ImmutableList.Builder<BallotState> builder = ImmutableList.builder();
		return builder.addAll(transform(ballots, new Function<Ballot, BallotState>() {
			@Override
			public BallotState apply(Ballot ballot) {
				return new BallotState(ballot, election)
					.withFirstHopefulCandidate(candidateStates);
			}
		})).build();
	}


	private boolean notAllSeatsFilled(int numberOfElectedCandidates, int numberOfSeatsToElect) {
		boolean notAllSeatsFilled = numberOfElectedCandidates < numberOfSeatsToElect;
		electionCalculationListener.numberOfElectedPositions(numberOfElectedCandidates, numberOfSeatsToElect);
		return notAllSeatsFilled;
	}


	private boolean anyCandidateIsHopeful(CandidateStates candidateStates) {
		for (CandidateState candidateState : candidateStates) {
			if (candidateState.isHopeful()) {
				return true;
			}
		}

		electionCalculationListener.noCandidatesAreLeft();
		return false;
	}


	private ImmutableSet<Candidate> getElectedCandidates(CandidateStates candidateStates) {
		ImmutableSet.Builder<Candidate> builder = ImmutableSet.builder();

		for (CandidateState candidateState : candidateStates) {
			if (candidateState.isElected()) {
				builder.add(candidateState.getCandidate());
			}
		}
		return builder.build();
	}


}
