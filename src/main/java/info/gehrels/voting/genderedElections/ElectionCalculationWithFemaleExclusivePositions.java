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
package info.gehrels.voting.genderedElections;

import com.google.common.base.Predicate;
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

public final class ElectionCalculationWithFemaleExclusivePositions {
	private final ElectionCalculationFactory<GenderedCandidate> electionCalculationFactory;
	private final ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener;

	public ElectionCalculationWithFemaleExclusivePositions(
		ElectionCalculationFactory<GenderedCandidate> electionCalculationFactory,
		ElectionCalculationWithFemaleExclusivePositionsListener electionCalculationListener) {
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.electionCalculationFactory = validateThat(electionCalculationFactory, is(notNullValue()));
	}

	public Result calculateElectionResult(GenderedElection election,
	                                      ImmutableCollection<Ballot<GenderedCandidate>> ballots) {
		validateThat(election, is(notNullValue()));
		validateThat(ballots, is(notNullValue()));

		ElectionCalculation<GenderedCandidate> electionCalculation =
			electionCalculationFactory.createElectionCalculation(election, ballots);
		electionCalculationListener.startElectionCalculation(election, ballots);

		ImmutableSet<GenderedCandidate> electedFemaleCandidates = ImmutableSet.of();
		if (election.getNumberOfFemaleExclusivePositions() > 0) {
			electedFemaleCandidates = calculateElectionResultForFemaleExclusivePositions(
				election, electionCalculation);
		}

		long numberOfElectableNotFemaleExclusivePositions = calculateNumberOfElectableFemaleExclusivePositions(
			election,
			electedFemaleCandidates);

		ImmutableSet<GenderedCandidate> candidatesElectedInOpenRun = ImmutableSet.of();
		if (numberOfElectableNotFemaleExclusivePositions > 0) {
			candidatesElectedInOpenRun = calculateElectionResultForNonFemaleExclusivePositions(
				election, electionCalculation, electedFemaleCandidates, numberOfElectableNotFemaleExclusivePositions);
		}
		return new Result(electedFemaleCandidates, candidatesElectedInOpenRun);
	}

	private ImmutableSet<GenderedCandidate> calculateElectionResultForFemaleExclusivePositions(
		GenderedElection election, ElectionCalculation<GenderedCandidate> electionCalculation) {
		electionCalculationListener.startFemaleExclusiveElectionRun();
		Predicate<GenderedCandidate> femalePredicate = new FemalePredicate(electionCalculationListener);
		ImmutableSet<GenderedCandidate> femaleCandidates =
			copyOf(
				filter(election.getCandidates(), femalePredicate)
			);
		return electionCalculation.calculate(femaleCandidates,
		                                     election.getNumberOfFemaleExclusivePositions());
	}

	private ImmutableSet<GenderedCandidate> calculateElectionResultForNonFemaleExclusivePositions(
		GenderedElection election, ElectionCalculation<GenderedCandidate> electionCalculation,
		ImmutableSet<GenderedCandidate> electedFemaleCandidates, long numberOfElectableNotFemaleExclusivePositions) {
		electionCalculationListener.startNonFemaleExclusiveElectionRun();
		Predicate<GenderedCandidate> notElectedBeforePredicate = new NotElectedBeforePredicate(electedFemaleCandidates,
		                                                                                       electionCalculationListener);
		ImmutableSet<GenderedCandidate> candidatesNotElectedBefore =
			copyOf(
				filter(election.getCandidates(), notElectedBeforePredicate)
			);

		return electionCalculation
			.calculate(candidatesNotElectedBefore, numberOfElectableNotFemaleExclusivePositions);
	}

	private long calculateNumberOfElectableFemaleExclusivePositions(GenderedElection election,
	                                                                ImmutableSet<GenderedCandidate> electedFemaleCandidates) {
		long numberOfElectableNotFemaleExclusivePositions =
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
		return numberOfElectableNotFemaleExclusivePositions;
	}


	public static final class Result {
		private final ImmutableSet<GenderedCandidate> candidatesElectedInFemaleOnlyRun;
		private final ImmutableSet<GenderedCandidate> candidatesElectedInNonFemaleOnlyRun;

		Result(ImmutableSet<GenderedCandidate> candidatesElectedInFemaleOnlyRun,
		       ImmutableSet<GenderedCandidate> candidatesElectedInNonFemaleOnlyRun) {

			this.candidatesElectedInFemaleOnlyRun = candidatesElectedInFemaleOnlyRun;
			this.candidatesElectedInNonFemaleOnlyRun = candidatesElectedInNonFemaleOnlyRun;
		}

		public ImmutableSet<GenderedCandidate> getCandidatesElectedInFemaleOnlyRun() {
			return candidatesElectedInFemaleOnlyRun;
		}

		public ImmutableSet<GenderedCandidate> getCandidatesElectedInNonFemaleOnlyRun() {
			return candidatesElectedInNonFemaleOnlyRun;
		}
	}
}
