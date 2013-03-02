package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

/**
 * CombinedElectionsOnOneBallot groups a set of Elections, that are hold using one ballot layout.
 */
public class CombinedElectionsOnOneBallot {
	public final ImmutableSet<Election> elections;

	public CombinedElectionsOnOneBallot(ImmutableSet<Election> elections) {
		this.elections = elections;
	}
}
