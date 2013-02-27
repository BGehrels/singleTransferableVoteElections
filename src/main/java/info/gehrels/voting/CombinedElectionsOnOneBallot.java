package info.gehrels.voting;

import com.google.common.collect.ImmutableSortedSet;

/**
 * CombinedElectionsOnOneBallot groups a set of Elections, that are hold using one ballot layout.
 */
public class CombinedElectionsOnOneBallot {
	private final ImmutableSortedSet<Election> elections;

	public CombinedElectionsOnOneBallot(ImmutableSortedSet<Election> elections) {
		this.elections = elections;
	}
}
