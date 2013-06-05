package info.gehrels.voting.singleTransferableVote;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculation;
import info.gehrels.voting.ElectionCalculationListener;
import info.gehrels.voting.QuorumCalculation;
import info.gehrels.voting.singleTransferableVote.STVElectionCalculationStep.ElectionStepResult;
import org.apache.commons.math3.fraction.BigFraction;

import static com.google.common.collect.Collections2.transform;
import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static info.gehrels.voting.singleTransferableVote.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class STVElectionCalculation<CANDIDATE_TYPE extends Candidate> implements ElectionCalculation<CANDIDATE_TYPE> {
	private final ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots;
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;
	private final Election<CANDIDATE_TYPE> election;
	private final VoteWeightRedistributionMethod<CANDIDATE_TYPE> voteWeightRedistributionMethod;
	private final STVElectionCalculationStep<CANDIDATE_TYPE> electionStep;

	public STVElectionCalculation(ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots,
	                              QuorumCalculation quorumCalculation,
	                              ElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener,
	                              Election<CANDIDATE_TYPE> election, AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver,
	                              VoteWeightRedistributionMethod<CANDIDATE_TYPE> redistributionMethod) {
		this.ballots = validateThat(ballots, is(not(nullValue())));
		this.quorumCalculation = validateThat(quorumCalculation, is(not(nullValue())));
		this.election = validateThat(election, is(not(nullValue())));
		this.voteWeightRedistributionMethod = validateThat(redistributionMethod, is(not(nullValue())));
		this.electionStep = new STVElectionCalculationStep<>(
			validateThat(electionCalculationListener, is(not(nullValue()))),
		    validateThat(ambiguityResolver, is(not(nullValue())))
		);
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public final ImmutableSet<CANDIDATE_TYPE> calculate(
		ImmutableSet<CANDIDATE_TYPE> qualifiedCandidates, int numberOfSeats) {
		validateThat(qualifiedCandidates, is(not(nullValue())));
		validateThat(numberOfSeats, is(greaterThanOrEqualTo(0)));

		VoteWeightRedistributor<CANDIDATE_TYPE> redistributor = voteWeightRedistributionMethod.redistributorFor();
		int numberOfValidBallots = ballots.size();
		// Runden oder nicht runden?
		BigFraction quorum = quorumCalculation.calculateQuorum(numberOfValidBallots, numberOfSeats);
		electionCalculationListener.quorumHasBeenCalculated(numberOfValidBallots, numberOfSeats, quorum);

		CandidateStates<CANDIDATE_TYPE> candidateStates = new CandidateStates<>(qualifiedCandidates);
		ImmutableCollection<BallotState<CANDIDATE_TYPE>> ballotStates = constructBallotStates(candidateStates);

		int numberOfElectedCandidates = 0;

		electionCalculationListener
			.calculationStarted(election,
			                    VotesByCandidateCalculation
				                    .calculateVotesByCandidate(candidateStates.getHopefulCandidates(),
				                                               ballotStates));

		while (notAllSeatsFilled(numberOfElectedCandidates, numberOfSeats) && anyCandidateIsHopeful(candidateStates)) {
			ElectionStepResult<CANDIDATE_TYPE> electionStepResult = electionStep.declareWinnerOrStrikeCandidate(quorum,
			                                                                                    ballotStates,
			                                                                                    redistributor,
			                                                                                    numberOfElectedCandidates,
			                                                                                    candidateStates);
			candidateStates = electionStepResult.newCandidateStates;
			ballotStates = electionStepResult.newBallotStates;
			numberOfElectedCandidates = electionStepResult.newNumberOfElectedCandidates;
		}

		ImmutableSet<CANDIDATE_TYPE> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return electedCandidates;
	}

	private ImmutableCollection<BallotState<CANDIDATE_TYPE>> constructBallotStates(final CandidateStates<CANDIDATE_TYPE> candidateStates) {
		ImmutableList.Builder<BallotState<CANDIDATE_TYPE>> builder = ImmutableList.builder();
		return builder.addAll(transform(ballots, new Function<Ballot<CANDIDATE_TYPE>, BallotState<CANDIDATE_TYPE>>() {
			@Override
			public BallotState<CANDIDATE_TYPE> apply(Ballot<CANDIDATE_TYPE> ballot) {
				return new BallotState<>(ballot, election)
					.withFirstHopefulCandidate(candidateStates);
			}
		})).build();
	}


	private boolean notAllSeatsFilled(int numberOfElectedCandidates, int numberOfSeatsToElect) {
		boolean notAllSeatsFilled = numberOfElectedCandidates < numberOfSeatsToElect;
		electionCalculationListener.numberOfElectedPositions(numberOfElectedCandidates, numberOfSeatsToElect);
		return notAllSeatsFilled;
	}


	private boolean anyCandidateIsHopeful(CandidateStates<CANDIDATE_TYPE> candidateStates) {
		for (CandidateState<CANDIDATE_TYPE> candidateState : candidateStates) {
			if (candidateState.isHopeful()) {
				return true;
			}
		}

		electionCalculationListener.noCandidatesAreLeft();
		return false;
	}


	private ImmutableSet<CANDIDATE_TYPE> getElectedCandidates(CandidateStates<CANDIDATE_TYPE> candidateStates) {
		ImmutableSet.Builder<CANDIDATE_TYPE> builder = ImmutableSet.builder();

		for (CandidateState<CANDIDATE_TYPE> candidateState : candidateStates) {
			if (candidateState.isElected()) {
				builder.add(candidateState.getCandidate());
			}
		}
		return builder.build();
	}


}
