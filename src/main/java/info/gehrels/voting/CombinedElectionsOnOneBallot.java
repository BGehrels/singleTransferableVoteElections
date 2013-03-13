package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import static info.gehrels.parameterValidation.MatcherValidation.validateThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsEmptyCollection.empty;

/**
 * CombinedElectionsOnOneBallot groups a set of Elections, that are hold using one ballot layout.
 */
public class CombinedElectionsOnOneBallot {
	public final ImmutableSet<Election> elections;

	public CombinedElectionsOnOneBallot(ImmutableSet<Election> elections) {
		this.elections = validateThat(elections, is(not(empty())));
	}
}
