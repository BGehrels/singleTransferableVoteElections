package info.gehrels.voting;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import org.apache.commons.math3.fraction.BigFraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.ImmutableSet.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static info.gehrels.voting.VoteWeightRedistributionMethod.VoteWeightRedistributor;
import static info.gehrels.voting.VotesByCandidateCalculation.calculateVotesByCandidate;
import static info.gehrels.voting.VotesForCandidateCalculation.calculateVotesForCandidate;
import static java.util.Arrays.asList;

public class STVElectionCalculation {
	private final ImmutableCollection<Ballot> ballots;
	private final QuorumCalculation quorumCalculation;
	private final ElectionCalculationListener electionCalculationListener;
	private final Election election;
	private final AmbiguityResolver ambiguityResolver;
	private final VoteWeightRedistributionMethod voteWeightRedistributionMethod;

	public STVElectionCalculation(ImmutableCollection<Ballot> ballots,
	                              QuorumCalculation quorumCalculation,
	                              ElectionCalculationListener electionCalculationListener,
	                              Election election, AmbiguityResolver ambiguityResolver,
	                              VoteWeightRedistributionMethod redistributionMethod) {
		this.ballots = ballots;
		this.quorumCalculation = quorumCalculation;
		this.electionCalculationListener = electionCalculationListener;
		this.election = election;
		this.ambiguityResolver = ambiguityResolver;
		voteWeightRedistributionMethod = redistributionMethod;
	}

	public ImmutableSet<Candidate> calculate(ImmutableSet<? extends Candidate> qualifiedCandidates, int numberOfSeats) {
		VoteWeightRedistributor redistributor = voteWeightRedistributionMethod.redistributorFor();
		int numberOfValidBallots = ballots.size();
		// Runden oder nicht runden?
		BigFraction quorum = quorumCalculation.calculateQuorum(numberOfValidBallots, numberOfSeats);
		electionCalculationListener.quorumHasBeenCalculated(numberOfValidBallots, numberOfSeats, quorum);

		Map<Candidate, CandidateState> candidateStates = constructCandidateStates(qualifiedCandidates);
		ImmutableCollection<BallotState> ballotStates = constructBallotStates(candidateStates);

		int numberOfElectedCandidates = 0;

		electionCalculationListener
			.calculationStarted(election,
			                    calculateVotesByCandidate(hopefulCandidates(candidateStates), ballotStates));

		while (notAllSeatsFilled(numberOfElectedCandidates, numberOfSeats) && anyCandidateIsHopeful(
			candidateStates)) {
			Candidate winner = bestCandidateThatReachedTheQuorum(quorum, candidateStates, ballotStates);
			if (winner != null) {
				electionCalculationListener
					.candidateIsElected(winner, calculateVotesForCandidate(winner, ballotStates), quorum);

				numberOfElectedCandidates++;
				ballotStates = redistributor.redistributeExceededVoteWeight(winner, quorum, ballotStates);
				CandidateState newCandidateState = candidateStates.get(winner).asElected();
				candidateStates.put(winner, newCandidateState);
				ballotStates = createBallotStatesPointingAtNextHopefulCandidate(ballotStates, candidateStates);
				electionCalculationListener.voteWeightRedistributionCompleted(
					calculateVotesByCandidate(hopefulCandidates(candidateStates), ballotStates));

			} else {
				electionCalculationListener.nobodyReachedTheQuorumYet(quorum);
				ballotStates = strikeWeakestCandidate(candidateStates, ballotStates);
			}
		}

		ImmutableSet<Candidate> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return electedCandidates;
	}

	private Map<Candidate, CandidateState> constructCandidateStates(
		ImmutableSet<? extends Candidate> qualifiedCandidates) {
		Map<Candidate, CandidateState> candidateStates = new HashMap<>();
		for (Candidate candidate : qualifiedCandidates) {
			candidateStates.put(candidate, new CandidateState(candidate));
		}
		return candidateStates;
	}

	private ImmutableCollection<BallotState> constructBallotStates(
		final Map<Candidate, CandidateState> candidateStates) {
		ImmutableList.Builder<BallotState> builder = ImmutableList.builder();
		return builder.addAll(transform(ballots, new Function<Ballot, BallotState>() {
			@Override
			public BallotState apply(Ballot ballot) {
				return createBallotStatePointingAtNextHopefulCandidate(candidateStates,
				                                                       new BallotState(ballot, election));
			}
		})).build();
	}


	private ImmutableSet<Candidate> hopefulCandidates(Map<Candidate, CandidateState> candidateStates) {
		ImmutableSet.Builder<Candidate> builder = ImmutableSet.builder();
		for (Entry<Candidate, CandidateState> entry : candidateStates.entrySet()) {
			if (entry.getValue() != null && entry.getValue().isHopeful()) {
				builder.add(entry.getValue().getCandidate());
			}
		}

		return builder.build();
	}

	private boolean notAllSeatsFilled(int numberOfElectedCandidates, int numberOfSeatsToElect) {
		boolean notAllSeatsFilled = numberOfElectedCandidates < numberOfSeatsToElect;
		electionCalculationListener.numberOfElectedPositions(numberOfElectedCandidates, numberOfSeatsToElect);
		return notAllSeatsFilled;
	}


	private boolean anyCandidateIsHopeful(Map<Candidate, CandidateState> candidateStates) {
		for (CandidateState candidateState : candidateStates.values()) {
			if (candidateState.isHopeful()) {
				return true;
			}
		}

		electionCalculationListener.noCandidatesAreLeft();
		return false;
	}

	private Candidate bestCandidateThatReachedTheQuorum(BigFraction quorum,
	                                                    Map<Candidate, CandidateState> candidateStates,
	                                                    ImmutableCollection<BallotState> ballotStates) {
		Map<Candidate, BigFraction> votesByCandidate =
			calculateVotesByCandidate(hopefulCandidates(candidateStates), ballotStates);
		BigFraction numberOfVotesOfBestCandidate = BigFraction.MINUS_ONE;
		Collection<Candidate> bestCandidates = newArrayList();
		for (Entry<Candidate, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(quorum) >= 0) {
				if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) > 0) {
					numberOfVotesOfBestCandidate = votesForCandidate.getValue();
					bestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
				} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
					bestCandidates.add(votesForCandidate.getKey());
				}
			}
		}


		// TODO: Ist ambiguity resolution hier überhaupt nötig?
		return chooseOneOutOfManyCandidates(copyOf(bestCandidates));
	}

	private ImmutableCollection<BallotState> strikeWeakestCandidate(Map<Candidate, CandidateState> candidateStates,
	                                                                ImmutableCollection<BallotState> ballotStates) {
		Map<Candidate, BigFraction> votesByCandidateBeforeStriking =
			calculateVotesByCandidate(
				hopefulCandidates(candidateStates), ballotStates);

		Candidate weakestCandidate = calculateWeakestCandidate(votesByCandidateBeforeStriking);

		// TODO: Mehrdeutigkeiten bei Schwächsten Kandidaten extern auswählen lassen
		CandidateState newState = candidateStates.get(weakestCandidate).asLooser();
		candidateStates.put(weakestCandidate, newState);
		ballotStates = createBallotStatesPointingAtNextHopefulCandidate(ballotStates, candidateStates);

		Map<Candidate, BigFraction> votesByCandidateAfterStriking =
			calculateVotesByCandidate(hopefulCandidates(
				candidateStates),
			                          ballotStates
			);

		electionCalculationListener.candidateDropped(
			votesByCandidateBeforeStriking,
			weakestCandidate.name,
			votesByCandidateBeforeStriking.get(weakestCandidate),
			votesByCandidateAfterStriking);
		return ballotStates;
	}

	private ImmutableCollection<BallotState> createBallotStatesPointingAtNextHopefulCandidate(
		ImmutableCollection<BallotState> ballotStates,
		Map<Candidate, CandidateState> candidateStates) {
		ImmutableList.Builder<BallotState> resultBuilder = ImmutableList.builder();
		for (BallotState ballotState : ballotStates) {
			resultBuilder.add(createBallotStatePointingAtNextHopefulCandidate(candidateStates, ballotState));
		}

		return resultBuilder.build();
	}

	private ImmutableSet<Candidate> getElectedCandidates(Map<Candidate, CandidateState> candidateStates) {
		ImmutableSet.Builder<Candidate> builder = ImmutableSet.builder();

		for (CandidateState candidateState : candidateStates.values()) {
			if (candidateState.isElected()) {
				builder.add(candidateState.getCandidate());
			}
		}
		return builder.build();
	}

	private Candidate chooseOneOutOfManyCandidates(ImmutableSet<Candidate> candidates) {
		Candidate winner = null;

		if (candidates.size() == 1) {
			return candidates.iterator().next();
		} else if (candidates.size() > 1) {
			electionCalculationListener.delegatingToExternalAmbiguityResolution(candidates);
			AmbiguityResolverResult ambiguityResolverResult = ambiguityResolver.chooseOneOfMany(candidates);
			electionCalculationListener.externalyResolvedAmbiguity(ambiguityResolverResult);
			winner = ambiguityResolverResult.choosenCandidate;
		}

		return winner;
	}

	private Candidate calculateWeakestCandidate(Map<Candidate, BigFraction> votesByCandidate) {
		BigFraction numberOfVotesOfBestCandidate = new BigFraction(Integer.MAX_VALUE, 1);
		//TODO: Hier sollten eigentlich 0-Kandidierende noch aufgeführt werden, solange sie nicht bereits gedroppd sind.
		Collection<Candidate> weakestCandidates = newArrayList();
		for (Entry<Candidate, BigFraction> votesForCandidate : votesByCandidate.entrySet()) {
			if (votesForCandidate.getValue().compareTo(numberOfVotesOfBestCandidate) < 0) {
				numberOfVotesOfBestCandidate = votesForCandidate.getValue();
				weakestCandidates = new ArrayList<>(asList(votesForCandidate.getKey()));
			} else if (votesForCandidate.getValue().equals(numberOfVotesOfBestCandidate)) {
				weakestCandidates.add(votesForCandidate.getKey());
			}
		}

		return chooseOneOutOfManyCandidates(copyOf(weakestCandidates));
	}


	private BallotState createBallotStatePointingAtNextHopefulCandidate(
		Map<Candidate, CandidateState> candidateStates, BallotState ballotState) {
		BallotState result = ballotState;

		Candidate preferredCandidate = result.getPreferredCandidate();
		while (preferredCandidate != null) {

			CandidateState candidateState = candidateStates.get(preferredCandidate);
			if (candidateState != null && candidateState.isHopeful()) {
				return result;
			}

			result = result.withNextPreference();
			preferredCandidate = result.getPreferredCandidate();
		}

		return result;
	}


}
