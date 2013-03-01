package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Collection;

/**
 * Hello world!
 */
public class App {
	public static void main(String[] args) {
		ImmutableSortedSet.Builder<Candidate> candidateBuilder = ImmutableSortedSet.naturalOrder();
		ImmutableSortedSet<Candidate> candidates =
			candidateBuilder
				.add(new Candidate("A", false))
				.add(new Candidate("B", false))
				.add(new Candidate("C", false))
				.add(new Candidate("D", false))
				.add(new Candidate("E", false))
				.add(new Candidate("F", false))
				.add(new Candidate("G", false))
				.add(new Candidate("H", false))
				.add(new Candidate("I", false))
				.add(new Candidate("J", false))
				.build();

		Office landesschiedsgericht = new Office("Landesschiedsgericht");
		Election election = new Election(landesschiedsgericht, 2, 2, candidates);
		ImmutableSet<Election> elections = ImmutableSet.<Election>builder().add(election).build();
		CombinedElectionsOnOneBallot combinedElectionsOnOneBallot = new CombinedElectionsOnOneBallot(elections);

		Builder<Ballot> ballotBuilder = ImmutableList.builder();
	    ballotBuilder
                .add(createBallot("ABDC", combinedElectionsOnOneBallot))
                .add(createBallot("ACBDE", combinedElectionsOnOneBallot))
                .add(createBallot("C", combinedElectionsOnOneBallot))
                .add(createBallot("CAE", combinedElectionsOnOneBallot))
                .add(createBallot("CBAFEDG", combinedElectionsOnOneBallot))
                .add(createBallot("CBDE", combinedElectionsOnOneBallot))
                .add(createBallot("CFBDEH", combinedElectionsOnOneBallot))
                .add(createBallot("CDFEHA", combinedElectionsOnOneBallot))
                .add(createBallot("DEC", combinedElectionsOnOneBallot))
                .add(createBallot("EBDCAF", combinedElectionsOnOneBallot))
                .add(createBallot("EDCA", combinedElectionsOnOneBallot))
                .add(createBallot("F", combinedElectionsOnOneBallot))
                .add(createBallot("FCH", combinedElectionsOnOneBallot))
                .add(createBallot("FGEIHJ", combinedElectionsOnOneBallot))
                .add(createBallot("FHG", combinedElectionsOnOneBallot))
                .add(createBallot("GFEI", combinedElectionsOnOneBallot))
                .add(createBallot("HFJAI", combinedElectionsOnOneBallot))
                .add(createBallot("HGIF", combinedElectionsOnOneBallot))
                .add(createBallot("IJF", combinedElectionsOnOneBallot))
                .add(createBallot("IJH", combinedElectionsOnOneBallot))
                .add(createBallot("JIHFE", combinedElectionsOnOneBallot));

		new ElectionCalculation(candidates, ballotBuilder.build(), 0, 1, new MyConflictResolutionAlgorithm())
			.calculateElectionResult();
	}

	private static Ballot createBallot(String preferences, CombinedElectionsOnOneBallot combinedElectionsOnOneBallot) {
		Election election = combinedElectionsOnOneBallot.iterator().next();
		ImmutableSet<Candidate> candidates = election.getCandidates();
		ImmutableSet.Builder<Candidate> ballotBuilder = ImmutableSet.builder();
		for (int i = 0; i < preferences.length(); i++) {
			char c = preferences.charAt(i);
			ballotBuilder.add(candidateByName("" + c, candidates));
		}
		return new Ballot(ballotBuilder.build());
	}

	private static Candidate candidateByName(String s, ImmutableSet<Candidate> candidates) {
		for (Candidate candidate : candidates) {
			if (candidate.name.equals(s)) {
				return candidate;
			}
		}

		throw new IllegalArgumentException(s);
	}

	private static class MyConflictResolutionAlgorithm implements ConflictResolutionAlgorithm {
		@Override
		public Candidate chooseWinner(Collection<Candidate> bestCandidates) {
			return bestCandidates.iterator().next();
		}
	}
}
