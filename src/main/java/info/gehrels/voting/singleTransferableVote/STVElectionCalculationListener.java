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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver.AmbiguityResolverResult;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import org.apache.commons.math3.fraction.BigFraction;

public interface STVElectionCalculationListener<CANDIDATE_TYPE extends Candidate> {
	void numberOfElectedPositions(long numberOfElectedCandidates, long numberOfSeatsToElect);

	void electedCandidates(ImmutableSet<CANDIDATE_TYPE> electedCandidates);

	void candidateDropped(
		VoteDistribution<CANDIDATE_TYPE> voteDistributionBeforeStriking, CANDIDATE_TYPE candidate);


	void voteWeightRedistributionCompleted(ImmutableCollection<VoteState<CANDIDATE_TYPE>> originalVoteStates,
	                                       ImmutableCollection<VoteState<CANDIDATE_TYPE>> newVoteStates,
	                                       VoteDistribution<CANDIDATE_TYPE> voteDistribution);

	void delegatingToExternalAmbiguityResolution(ImmutableSet<CANDIDATE_TYPE> bestCandidates);

	void externalyResolvedAmbiguity(AmbiguityResolverResult<CANDIDATE_TYPE> ambiguityResolverResult);

	void candidateIsElected(CANDIDATE_TYPE winner, BigFraction numberOfVotes, BigFraction quorum);

	void nobodyReachedTheQuorumYet(BigFraction quorum);

	void noCandidatesAreLeft();

	void calculationStarted(Election<CANDIDATE_TYPE> election, VoteDistribution<CANDIDATE_TYPE> voteDistribution);

	void quorumHasBeenCalculated(long numberOfValidBallots, long numberOfSeats, BigFraction quorum);

	void redistributingExcessiveFractionOfVoteWeight(CANDIDATE_TYPE winner, BigFraction excessiveFractionOfVoteWeight);
}
