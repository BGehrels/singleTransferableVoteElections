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
import info.gehrels.voting.Candidate;
import org.apache.commons.math3.fraction.BigFraction;

public interface VoteWeightRecalculator<CANDIDATE_TYPE extends Candidate> {

	ImmutableCollection<VoteState<CANDIDATE_TYPE>> recalculateExceededVoteWeight(CANDIDATE_TYPE winner,
	                                                                             BigFraction quorum,
	                                                                             ImmutableCollection<VoteState<CANDIDATE_TYPE>> voteStates,
	                                                                             CandidateStates<CANDIDATE_TYPE> candidateStates);
}
