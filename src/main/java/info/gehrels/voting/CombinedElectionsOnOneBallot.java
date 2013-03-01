package info.gehrels.voting;

import com.google.common.collect.ImmutableSet;

import java.util.Iterator;

/**
 * CombinedElectionsOnOneBallot groups a set of Elections, that are hold using one ballot layout.
 */
public class CombinedElectionsOnOneBallot implements Iterable<Election> {
	private final ImmutableSet<Election> elections;

	public CombinedElectionsOnOneBallot(ImmutableSet<Election> elections) {
		this.elections = elections;
	}


	@Override
	public Iterator<Election> iterator() {
		return elections.iterator();
	}
}
