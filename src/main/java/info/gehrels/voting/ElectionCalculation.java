package info.gehrels.voting;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.Collections2.transform;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;

public class ElectionCalculation {
	private final Election election;
	private final ImmutableCollection<Ballot> ballots;
    private final int numberOfFemaleSeats;
    private int numberOfOpenSeats;
    private final ConflictResolutionAlgorithm conflictResolutionAlgorithm;
	private final ElectionCalculationListener electionCalculationListener;

    public ElectionCalculation(Election election, ImmutableCollection<Ballot> ballots,
                               ConflictResolutionAlgorithm conflictResolutionAlgorithm,
                               ElectionCalculationListener electionCalculationListener) {
	    this.election = election;
	    this.ballots = ballots;
	    this.electionCalculationListener = electionCalculationListener;
	    this.numberOfFemaleSeats = election.getNumberOfFemaleExclusivePositions();
        this.numberOfOpenSeats = election.getNumberOfNotFemaleExclusivePositions();
        this.conflictResolutionAlgorithm = conflictResolutionAlgorithm;
    }

    public ElectionResult calculateElectionResult() {
        int numberOfValidBallots = ballots.size();
        // Runden oder nicht runden?
        // Satzungsmäßig klarstellen, dass eigenes Quorum für Frauen und nicht Frauen...
        //double femaleQuorum = numberOfValidBallots / (numberOfFemaleSeats + 1) + 1;
        //double nonFemaleQuorum = numberOfValidBallots / (numberOfOpenSeats + 1) + 1;
        double femaleQuorum = numberOfValidBallots / (numberOfFemaleSeats + 1.0);
        double nonFemaleQuorum = numberOfValidBallots / (numberOfOpenSeats + 1.0);

	    electionCalculationListener.quorumHasBeenCalculated(true, femaleQuorum);
	    electionCalculationListener.quorumHasBeenCalculated(false, nonFemaleQuorum);

        ImmutableCollection<BallotState> ballotStates = constructBallotStates();
        ImmutableMap<Candidate, CandidateState> candidateStates = constructCandidateStates();

        int numberOfElectedFemaleCandidates = 0;

        while (anyCandidateIsHopeful(true, candidateStates) && numberOfElectedFemaleCandidates < numberOfFemaleSeats) {
            Candidate candidate = bestCandidateThatReachedTheQuorum(femaleQuorum, true, candidateStates, ballotStates);
            if (candidate != null) {
                numberOfElectedFemaleCandidates++;
                redistributeExceededVoteWeight(candidate, femaleQuorum, ballotStates);
                candidateStates.get(candidate).setElected();
            } else {
                strikeWeakestCandidate(true, candidateStates, ballotStates);
            }
        }

        resetLoosers(candidateStates);
        resetBallotStates(ballotStates);

        int numberOfElectedOpenCandidates = 0;
        while (notAllSeatsFilled(numberOfElectedOpenCandidates) && anyCandidateIsHopeful(false, candidateStates)) {
            Candidate candidate = bestCandidateThatReachedTheQuorum(nonFemaleQuorum, false, candidateStates, ballotStates);
            if (candidate != null) {
                redistributeExceededVoteWeight(candidate, nonFemaleQuorum, ballotStates);
                candidateStates.get(candidate).setElected();
                numberOfElectedOpenCandidates++;
            } else {
                strikeWeakestCandidate(false, candidateStates, ballotStates);
            }
        }

        System.out.println("======================================");
        System.out.println("Gewählt sind: ");
        for (CandidateState candidateState : candidateStates.values()) {
            if (candidateState.elected) {
                System.out.println("\t" + candidateState.candidate.name);
            }
        }


        return null;
    }

    private boolean notAllSeatsFilled(int numberOfElectedOpenCandidates) {
        boolean notAllSeatsFilled = numberOfElectedOpenCandidates < numberOfOpenSeats;
        if (notAllSeatsFilled) {
            System.out.println("Es sind erst " + numberOfElectedOpenCandidates + " auf " + numberOfOpenSeats
                               + " offene Plätze gewählt");
        } else {
            System.out.println("Alle " + numberOfOpenSeats + " offenen Plätze sind gewählt");
        }
        return notAllSeatsFilled;
    }

    private void resetBallotStates(Collection<BallotState> ballotStates) {
        for (BallotState ballotState : ballotStates) {
            ballotState.reset();
        }
    }

    private void resetLoosers(Map<Candidate, CandidateState> candidateStates) {
        for (CandidateState candidateState : candidateStates.values()) {
            candidateState.resetLooser();
        }
    }

    private void strikeWeakestCandidate(boolean onlyFemaleCandidates, Map<Candidate, CandidateState> candidateStates,
                                        Collection<BallotState> ballotStates) {
        Map<Candidate, Double> candidateDoubleMap = calculateVotesByCandidate(onlyFemaleCandidates, candidateStates,
                                                                              ballotStates);
        Candidate weakestCandidate = null;
        Double weakestVoteCount = null;
        for (Entry<Candidate, Double> votesForCandidate : candidateDoubleMap.entrySet()) {
            if (weakestVoteCount == null || votesForCandidate.getValue() < weakestVoteCount) {
                weakestCandidate = votesForCandidate.getKey();
                weakestVoteCount = votesForCandidate.getValue();
            }
        }

        System.out.println(weakestCandidate.name + " hat mit " + weakestVoteCount
                           + " Stimmen das schlechteste Ergebnis und scheidet aus.");
        candidateStates.get(weakestCandidate).setLooser();
    }

    private void redistributeExceededVoteWeight(Candidate candidate, double quorum,
                                                Collection<BallotState> ballotStates) {
        double votesForCandidate = calculateVotesForCandidate(candidate, ballotStates);
        double excessiveVotes = votesForCandidate - quorum;
        double fractionOfExcessiveVotes = excessiveVotes / votesForCandidate;
        System.out.println("Es werden " + fractionOfExcessiveVotes * 100 + "% der Stimmen weiterverteilt");

        for (BallotState ballotState : ballotStates) {
            if (ballotState.getPreferredCandidate() == candidate) {
                ballotState.reduceVoteWeight(fractionOfExcessiveVotes);
                System.out.println(
                        "Stimmzettel " + ballotState.ballot.id + " hat nun ein verbleibendes Stimmgewicht von "
                        + ballotState.getVoteWeight());
            }
        }
    }

    private double calculateVotesForCandidate(Candidate candidate, Collection<BallotState> ballotStates) {
        double votes = 0;
        for (BallotState ballotState : ballotStates) {
            if (ballotState.getPreferredCandidate() == candidate) {
                votes += ballotState.getVoteWeight();
            }
        }

        return votes;
    }

    private Candidate bestCandidateThatReachedTheQuorum(double quorum, boolean onlyFemaleCandidates,
                                                        ImmutableMap<Candidate, CandidateState> candidateStates,
                                                        ImmutableCollection<BallotState> ballotStates) {
        Map<Candidate, Double> votesByCandidate = calculateVotesByCandidate(onlyFemaleCandidates, candidateStates,
                                                                            ballotStates);
        double numberOfVotesOfBestCandidate = -1;
        Collection<Candidate> bestCandidates = newArrayList();
        for (Entry<Candidate, Double> votesForCandidate : votesByCandidate.entrySet()) {
            if (votesForCandidate.getValue() > quorum) {
                System.out.println(votesForCandidate.getKey().name + " hat mit " + votesForCandidate.getValue()
                                   + " Stimmen das Quorum erreicht.");
                if (votesForCandidate.getValue() > numberOfVotesOfBestCandidate) {
                    numberOfVotesOfBestCandidate = votesForCandidate.getValue();
                    bestCandidates = asList(votesForCandidate.getKey());
                } else if (votesForCandidate.getValue() == numberOfVotesOfBestCandidate) {
                    bestCandidates.add(votesForCandidate.getKey());
                }
            }
        }

        Candidate winner = null;

        if (bestCandidates.size() == 1) {
            winner = bestCandidates.iterator().next();
            System.out.println(winner.name + " hat die meisten Stimmen und das Quorum erreicht => gewählt");
        } else if (bestCandidates.size() > 1) {
            winner = conflictResolutionAlgorithm.chooseWinner(bestCandidates);
            System.out.println(winner.name + " wurde wegen Gleichstands extern als Gewinner ausgewählt");
        } else {
            System.out.println("Kein_e Kandidierende_r hat das Quorum erreicht.");
        }
        return winner;
    }

    private boolean anyCandidateIsHopeful(boolean onlyFemaleCandidates,
                                          ImmutableMap<Candidate, CandidateState> candidateStates) {
        for (CandidateState candidateState : candidateStates.values()) {
            if (isAcceptableCandidate(onlyFemaleCandidates, candidateState.candidate) && candidateState.isHopeful()) {
                System.out.println("Es gibt noch hoffnungsvolle Kandidierende");
                return true;
            }
        }
        System.out.println("Es gibt keine hoffnungsvollen Kandidierende mehr");
        return false;
    }

    private Map<Candidate, Double> calculateVotesByCandidate(boolean onlyFemaleCandidates,
                                                             Map<Candidate, CandidateState> candidateStates,
                                                             Collection<BallotState> ballotStates) {
        Map<Candidate, Double> votesByCandidate = new HashMap<>();
        for (BallotState ballotState : ballotStates) {
            CandidateState preferredHopefulCandidate = getPreferredHopefulCandidate(candidateStates, ballotState,
                                                                                    onlyFemaleCandidates);
            if (preferredHopefulCandidate == null) {
                continue;
            }

            Double votes = votesByCandidate.get(preferredHopefulCandidate.candidate);
            if (votes == null) {
                votesByCandidate.put(preferredHopefulCandidate.candidate, ballotState.getVoteWeight());
            } else {
                votesByCandidate.put(preferredHopefulCandidate.candidate, votes + ballotState.getVoteWeight());
            }
        }

        System.out.println("Die Stimmen verteilen sich wie folgt:");
        for (Entry<Candidate, Double> candidateDoubleEntry : votesByCandidate.entrySet()) {
            System.out.println(
                    "\t" + candidateDoubleEntry.getKey().name + ": " + candidateDoubleEntry.getValue() + " Stimmen");
        }
        return votesByCandidate;
    }


    private CandidateState getPreferredHopefulCandidate(Map<Candidate, CandidateState> candidateStates,
                                                        BallotState ballotState, boolean onlyFemaleCandidates) {
        Candidate preferredCandidate = ballotState.getPreferredCandidate();
        while (preferredCandidate != null) {
            CandidateState candidateState = candidateStates.get(preferredCandidate);
            if (isAcceptableCandidate(onlyFemaleCandidates, preferredCandidate) && candidateState.isHopeful()) {
                return candidateState;
            }

            preferredCandidate = ballotState.proceedToNextPreference();
        }

        return null;
    }

    private boolean isAcceptableCandidate(boolean onlyFemaleCandidates, Candidate candidate) {
        return !onlyFemaleCandidates || candidate.isFemale;
    }

    private ImmutableMap<Candidate, CandidateState> constructCandidateStates() {
        Builder<Candidate, CandidateState> builder = ImmutableMap.builder();
        for (Candidate candidate : election.getCandidates()) {
            builder.put(candidate, new CandidateState(candidate));
        }
        return builder.build();
    }

    private ImmutableCollection<BallotState> constructBallotStates() {
        ImmutableList.Builder<BallotState> builder = ImmutableList.builder();
        return builder.addAll(transform(ballots, new Function<Ballot, BallotState>() {
            @Override
            public BallotState apply(Ballot ballot) {
                return new BallotState(election, ballot);
            }
        })).build();
    }

    private static class ElectionResult {
    }

    private  class BallotState {
        private final Ballot ballot;
        private double voteWeigt;
	    private Iterator<Candidate> ballotIterator;
	    private Candidate candidateOfCurrentPreference;

	    public BallotState(Election election, Ballot ballot) {
            this.ballot = ballot;
		    reset();
        }

        public Candidate getPreferredCandidate() {
            return candidateOfCurrentPreference;
        }

        public double getVoteWeight() {
            return voteWeigt;
        }

        public Candidate proceedToNextPreference() {
	        candidateOfCurrentPreference = null;
            if (ballotIterator.hasNext()) {
	            candidateOfCurrentPreference = ballotIterator.next();
            }

	        return candidateOfCurrentPreference;
        }

        public void reduceVoteWeight(double fractionOfExcessiveVotes) {
            voteWeigt *= fractionOfExcessiveVotes;
        }

        public void reset() {
	        this.ballotIterator = ballot.getRankedCandidatesByElection(election).iterator();
            proceedToNextPreference();

            voteWeigt = 1;
        }
    }

    private class CandidateState {
        private final Candidate candidate;
        private boolean elected;
        private boolean looser;


        public CandidateState(Candidate candidate) {
            this.candidate = candidate;
        }

        public boolean isHopeful() {
            return !elected && !looser;
        }

        public void setElected() {
            this.elected = true;
        }

        public void setLooser() {
            this.looser = true;
        }

        public void resetLooser() {
            this.looser = false;
        }
    }
}
