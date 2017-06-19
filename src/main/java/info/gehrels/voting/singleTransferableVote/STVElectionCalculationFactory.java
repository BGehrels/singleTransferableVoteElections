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
package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableCollection;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.ElectionCalculationFactory;
import info.gehrels.voting.QuorumCalculation;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class STVElectionCalculationFactory<CANDIDATE_TYPE extends Candidate> implements
	ElectionCalculationFactory<CANDIDATE_TYPE> {
	private final QuorumCalculation quorumCalculation;
	private final STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener;
	private final AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver;

	public STVElectionCalculationFactory(QuorumCalculation quorumCalculation,
	                                     STVElectionCalculationListener<CANDIDATE_TYPE> electionCalculationListener,
	                                     AmbiguityResolver<CANDIDATE_TYPE> ambiguityResolver) {
		this.quorumCalculation = validateThat(quorumCalculation, is(notNullValue()));
		this.electionCalculationListener = validateThat(electionCalculationListener, is(notNullValue()));
		this.ambiguityResolver = validateThat(ambiguityResolver, is(notNullValue()));
	}

	@Override
	public final STVElectionCalculation<CANDIDATE_TYPE> createElectionCalculation(Election<CANDIDATE_TYPE> election,
	                                                                      ImmutableCollection<Ballot<CANDIDATE_TYPE>> ballots) {
		return new STVElectionCalculation<>(ballots, quorumCalculation, electionCalculationListener, election,
		                                                ambiguityResolver, new WeightedInclusiveGregoryMethod<>(
			electionCalculationListener));
	}
}
