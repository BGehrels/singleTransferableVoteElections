package info.gehrels.voting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Collection;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
	    Candidate benjamin = new Candidate("Benjamin", false);
	    ImmutableSortedSet<Candidate> build = ImmutableSortedSet.<Candidate>naturalOrder().add(benjamin).build();
	    //new CombinedElectionsOnOneBallot(ImmutableSortedSet.naturalOrder(new Election(new Office("Landesschiedsgericht"), 2, 2,
	    //                                                                              build)));

        Builder<Candidate> candidateBuilder = ImmutableList.builder();
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
                .add(new Candidate("J", false));
        ImmutableList<Candidate> candidates = candidateBuilder.build();
        Builder<Ballot> ballotBuilder = ImmutableList.builder();
        ballotBuilder
                .add(createBallot("ABDC", candidates))
                .add(createBallot("ACBDE", candidates))
                .add(createBallot("C", candidates))
                .add(createBallot("CAE", candidates))
                .add(createBallot("CBAFEDG", candidates))
                .add(createBallot("CBDE", candidates))
                .add(createBallot("CFBDEH", candidates))
                .add(createBallot("CDFEHA", candidates))
                .add(createBallot("DEC", candidates))
                .add(createBallot("EBDCAF", candidates))
                .add(createBallot("EDCA", candidates))
                .add(createBallot("F", candidates))
                .add(createBallot("FCH", candidates))
                .add(createBallot("FGEIHJ", candidates))
                .add(createBallot("FHG", candidates))
                .add(createBallot("GFEI", candidates))
                .add(createBallot("HFJAI", candidates))
                .add(createBallot("HGIF", candidates))
                .add(createBallot("IJF", candidates))
                .add(createBallot("IJH", candidates))
                .add(createBallot("JIHFE", candidates));

        new ElectionCalculation(candidates, ballotBuilder.build(), 0, 1, new MyConflictResolutionAlgorithm()).calculateElectionResult();
    }

    private static Ballot createBallot(String preferences, List<Candidate> candidates) {
        Builder<Candidate> ballotBuilder = ImmutableList.builder();
        for (int i = 0; i < preferences.length(); i++) {
            char c = preferences.charAt(i);
            ballotBuilder.add(candidateByName(""+c, candidates));
        }
        //return new Ballot(ballotBuilder.build());
	    throw new UnsupportedOperationException();
    }

    private static Candidate candidateByName(String s, List<Candidate> candidates) {
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
