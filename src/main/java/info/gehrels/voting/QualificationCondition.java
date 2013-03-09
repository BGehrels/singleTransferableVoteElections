package info.gehrels.voting;

public interface QualificationCondition {
	boolean isQualified(Candidate candidate);
}
