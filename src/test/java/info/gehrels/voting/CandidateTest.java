package info.gehrels.voting;

import com.google.common.collect.ImmutableSortedSet;
import org.junit.Test;

import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

public class CandidateTest {

	@Test
	public void isNaturallyOrderedByName() {
		Candidate peter = new Candidate("Peter", false);
		Candidate nancy = new Candidate("Nancy", true);
		Candidate aaron = new Candidate("Aaron", true);
		SortedSet<Candidate> naturallySortedCandidates =
			ImmutableSortedSet.<Candidate>naturalOrder()
			.add(peter)
			.add(nancy)
			.add(aaron)
			.build();

		assertThat(naturallySortedCandidates, hasItems(aaron, nancy, peter));
	}
}
