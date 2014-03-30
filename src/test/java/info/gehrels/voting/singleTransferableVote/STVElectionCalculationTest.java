package info.gehrels.voting.singleTransferableVote;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import info.gehrels.voting.AmbiguityResolver;
import info.gehrels.voting.Ballot;
import info.gehrels.voting.Candidate;
import info.gehrels.voting.Election;
import info.gehrels.voting.QuorumCalculation;
import org.junit.Test;

import static info.gehrels.voting.Vote.createInvalidVote;
import static info.gehrels.voting.Vote.createNoVote;
import static info.gehrels.voting.Vote.createPreferenceVote;
import static org.apache.commons.math3.fraction.BigFraction.TWO;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class STVElectionCalculationTest {
	public static final Candidate CANDIDATE_1_A = new Candidate("1a");
	public static final Candidate CANDIDATE_1_B = new Candidate("1b");
	public static final Candidate CANDIDATE_2_A = new Candidate("2a");
	private static final Election<Candidate> ELECTION_1 = new Election<>("office1",
	                                                                     ImmutableSet.of(CANDIDATE_1_A, CANDIDATE_1_B));
	private static final Election<Candidate> ELECTION_2 = new Election<>("office2", ImmutableSet.of(CANDIDATE_2_A));

	private final QuorumCalculation quorumCalculationMock = mock(QuorumCalculation.class);
	private final STVElectionCalculationListener<Candidate> electionCalculationListenerMock =
		mock(STVElectionCalculationListener.class);
	private final AmbiguityResolver<Candidate> ambiguityResolverMock =
		mock(AmbiguityResolver.class);
	private final VoteWeightRecalculationMethod<Candidate> redistributionMethodMock =
		mock(VoteWeightRecalculationMethod.class);

	@Test
	public void callsQuorumCalculatorWithTheCorrectParameters() {
		doReturn(TWO).when(quorumCalculationMock).calculateQuorum(anyLong(), anyLong());

		new STVElectionCalculation<>(setupBallotsFixture(), quorumCalculationMock, electionCalculationListenerMock,
		                             ELECTION_1, ambiguityResolverMock, redistributionMethodMock)
			.calculate(ImmutableSet.<Candidate>of(CANDIDATE_1_A), 2);

		verify(quorumCalculationMock).calculateQuorum(3, 2);
	}

	@Test
	public void callsListenerWithCorrectValuesAfterQuorumCalculation() {
		doReturn(TWO).when(quorumCalculationMock).calculateQuorum(anyLong(), anyLong());

		new STVElectionCalculation<>(setupBallotsFixture(), quorumCalculationMock, electionCalculationListenerMock,
		                             ELECTION_1, ambiguityResolverMock, redistributionMethodMock)
			.calculate(ImmutableSet.<Candidate>of(CANDIDATE_1_A), 2);

		verify(electionCalculationListenerMock).quorumHasBeenCalculated(3, 2, TWO);
	}

	// TODO: Dieser Test ist als Whitebox-Test noch deutlich ausbaubar
    // TODO: Aufruf von electionCalculationListener.calculationStarted,
    // TODO: electionCalculationListener.electedCandidates(electedCandidates),
    // TODO: electionCalculationListener.numberOfElectedPositions,
    // TODO: electionCalculationListener.noCandidatesAreLeft() testen

	private ImmutableList<Ballot<Candidate>> setupBallotsFixture() {
		return ImmutableList.of(
			new Ballot<>(1,
			             ImmutableSet
				             .of(createPreferenceVote(ELECTION_2, ImmutableSet.<Candidate>of(CANDIDATE_2_A)))),
			new Ballot<>(2,
			             ImmutableSet.of(createInvalidVote(ELECTION_1))),
			new Ballot<>(3,
			             ImmutableSet.of(createPreferenceVote(ELECTION_1, ImmutableSet.<Candidate>of(CANDIDATE_1_A)))),
			new Ballot<>(4,
			             ImmutableSet.of(createPreferenceVote(ELECTION_1, ImmutableSet.<Candidate>of(CANDIDATE_1_B)))),
			new Ballot<>(5,
			             ImmutableSet.of(createNoVote(ELECTION_1)))
		);
	}


}
