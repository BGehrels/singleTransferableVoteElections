/*
 * Copyright © 2014 Benjamin Gehrels
 *
 * This file is part of The Single Transferable Vote Elections Library.
 *
 * The Single Transferable Vote Elections Web Interface is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * The Single Transferable Vote Elections Web Interface is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with The Single Transferable Vote
 * Elections Web Interface. If not, see <http://www.gnu.org/licenses/>.
 */
package info.gehrels.voting.singleTransferableVote;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculation;
import info.gehrels.voting.QuorumCalculation;
import info.gehrels.voting.Vote;
import org.apache.commons.math3.fraction.BigFraction;
import org.apache.commons.math3.util.Pair;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class STVElectionCalculation<CANDIDATE_TYPE extends Candidate> implements ElectionCalculation<CANDIDATE_TYPE> {
	private final ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots;
	private final QuorumCalculation quorumCalculation;
	private final STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;
	private final Election<CANDIDATE_TYPE> election;
	private final VoteWeightRecalculationMethod<CANDIDATE_TYPE> voteWeightRecalculationMethod;
	private final STVElectionCalculationStep<CANDIDATE_TYPE> electionStep;

	public STVElectionCalculation(ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots,
	                              QuorumCalculation quorumCalculation,
	                              STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener,
	                              Election<CANDIDATE_TYPE> election,
	                              AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver,
	                              VoteWeightRecalculationMethod<CANDIDATE_TYPE> redistributionMethod) {

		this.ballots = validateThat(ballots, allOf(
			is(not(nullValue())),
			not(hasAPairOfElementsThat(this.<CANDIDATE_TYPE>hasEqualIds()))
			));
		this.quorumCalculation = validateThat(quorumCalculation, is(not(nullValue())));
		this.election = validateThat(election, is(not(nullValue())));
		this.voteWeightRecalculationMethod = validateThat(redistributionMethod, is(not(nullValue())));
		this.electionStep = new STVElectionCalculationStep<>(
			validateThat(electionCalculationListener, is(not(nullValue()))),
			validateThat(ambiguityResolver, is(not(nullValue())))
		);
		this.electionCalculationListener = electionCalculationListener;
	}

	@Override
	public final ImmutableSet<CANDIDATE_TYPE> calculate(ImmutableSet<CANDIDATE_TYPE> qualifiedCandidates,
	                                                    long numberOfSeats) {
		validateThat(qualifiedCandidates, is(not(nullValue())));
		validateThat(numberOfSeats, is(greaterThanOrEqualTo(0L)));

		VoteWeightRecalculator<CANDIDATE_TYPE> redistributor = voteWeightRecalculationMethod.recalculatorFor();
		long numberOfValidBallots = calculateNumberOfValidVotes();
		BigFraction quorum = quorumCalculation.calculateQuorum(numberOfValidBallots, numberOfSeats);
		electionCalculationListener.quorumHasBeenCalculated(numberOfValidBallots, numberOfSeats, quorum);

		CandidateStates<CANDIDATE_TYPE> candidateStates = new CandidateStates<>(qualifiedCandidates);
		ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates = constructVoteStates(candidateStates);

		electionCalculationListener
			.calculationStarted(election, new VoteDistribution<>(candidateStates.getHopefulCandidates(),  voteStates));

		long numberOfElectedCandidates = 0;
		while (notAllSeatsFilled(numberOfElectedCandidates, numberOfSeats) && anyCandidateIsHopeful(candidateStates)) {
			STVElectionCalculationStep.ElectionStepResult<CANDIDATE_TYPE> electionStepResult = electionStep
				.declareWinnerOrStrikeCandidate(quorum,
				                                voteStates,
				                                redistributor,
				                                numberOfElectedCandidates,
				                                candidateStates);
			candidateStates = electionStepResult.newCandidateStates;
			voteStates = electionStepResult.newVoteStates;
			numberOfElectedCandidates = electionStepResult.newNumberOfElectedCandidates;
		}

		ImmutableSet<CANDIDATE_TYPE> electedCandidates = getElectedCandidates(candidateStates);
		electionCalculationListener.electedCandidates(electedCandidates);
		return electedCandidates;
	}

	private long calculateNumberOfValidVotes() {
		long numberOfValidVotes = 0;
		for (Ballot<CANDIDATE_TYPE> ballot : ballots) {
			Optional<Vote<CANDIDATE_TYPE>> vote = ballot.getVote(election);
			if (vote.isPresent() && vote.get().isValid()) {
				numberOfValidVotes++;
			}
		}

		return numberOfValidVotes;
	}

	private ImmutableCollection<VoteState<CANDIDATE_TYPE>> constructVoteStates(
		CandidateStates<CANDIDATE_TYPE> candidateStates) {
		ImmutableList.Builder<VoteState<CANDIDATE_TYPE>> builder = ImmutableList.builder();
		for (Ballot<CANDIDATE_TYPE> ballot : ballots) {
			Optional<VoteState<CANDIDATE_TYPE>> voteStateOptional = VoteState.forBallotAndElection(ballot, election);
			if (voteStateOptional.isPresent()) {
				builder.add(voteStateOptional.get().withFirstHopefulCandidate(candidateStates));
			}
		}
		return builder.build();
	}


	private boolean notAllSeatsFilled(long numberOfElectedCandidates, long numberOfSeatsToElect) {
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

	private <T extends Candidate> Matcher<Pair<Ballot<T>, Ballot<T>>> hasEqualIds() {
		return new TypeSafeDiagnosingMatcher<Pair<Ballot<T>, Ballot<T>>>() {
			@Override
			protected boolean matchesSafely(Pair<Ballot<T>, Ballot<T>> item, Description mismatchDescription) {
				long firstId = item.getFirst().id;
				long secondId = item.getSecond().id;
				if (firstId != secondId) {
					mismatchDescription.appendValue(item).appendText(" had different ids ").appendValue(firstId)
						.appendText(" and ").appendValue(secondId);
					return false;
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has equal ids");
			}
		};
	}


	private static <T> Matcher<Iterable<T>> hasAPairOfElementsThat(final Matcher<Pair<T, T>> subMatcher) {
		return new TypeSafeDiagnosingMatcher<Iterable<T>>() {
			@Override
			protected boolean matchesSafely(Iterable<T> iterable, Description mismatchDescription) {
				for (T item1 : iterable) {
					for (T item2 : iterable) {
						if (item1 != item2) {
							Pair<T, T> pair = new Pair<>(item1, item2);
							if (subMatcher.matches(pair)) {
								return true;
							}
						}
					}
				}

				mismatchDescription.appendValue(iterable).appendText(" did not contain a pair of elements that ").appendDescriptionOf(subMatcher);
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("has a pair of Elements that ").appendDescriptionOf(subMatcher);
			}
		};
	}
}
