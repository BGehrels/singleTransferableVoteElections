package info.gehrels.voting.singleTransferableVote;

import info.gehrels.voting.NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum;
import info.gehrels.voting.QuorumCalculation;
import org.apache.commons.math3.fraction.BigFraction;
import org.junit.Test;

import static org.apache.commons.math3.fraction.BigFraction.ONE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public final class NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorumTest {

	public static final BigFraction ONE_TENTH = new BigFraction(1, 10);

	@Test
	public void returnsCorrectQuorumForZeroValidVotesAndZeroPostitions() {
		QuorumCalculation defaultQuorumCalculation
			= new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(0, 0);

		assertThat(quorum, is(ONE_TENTH));
	}

	@Test
	public void returnsCorrectQuorumForZeroValidVotesAndNonZeroPostitions() {
		QuorumCalculation defaultQuorumCalculation
			= new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(0, 4);

		assertThat(quorum, is(ONE_TENTH));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroNumberOfValidVotesAndZeroPostitions() {
		QuorumCalculation defaultQuorumCalculation
			= new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(4, 0);

		assertThat(quorum, is(new BigFraction(41, 10)));
	}

	@Test
	public void returnsCorrectQuorumForNonZeroNumberOfValidVotesAndNonZeroNumberOfPostitions() {
		QuorumCalculation defaultQuorumCalculation = new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(
			ONE_TENTH);
		BigFraction quorum = defaultQuorumCalculation.calculateQuorum(6, 2);

		assertThat(quorum, is(new BigFraction(21, 10)));
	}

	@Test
	public void quorumMayNotBeHigherThanNumberOfValidVotesIfThereArePositionsToElect() {
		QuorumCalculation quorumCalculation = new NotMoreThanTheAllowedNumberOfCandidatesCanReachItQuorum(ONE);
		BigFraction quorum = quorumCalculation.calculateQuorum(1, 1);
		assertThat(quorum, is(ONE));
	}


}
