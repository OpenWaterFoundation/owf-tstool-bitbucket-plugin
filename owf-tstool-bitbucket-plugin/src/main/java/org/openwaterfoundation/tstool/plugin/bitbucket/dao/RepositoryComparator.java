package org.openwaterfoundation.tstool.plugin.bitbucket.dao;

import java.util.Comparator;

/**
 * Comparator for Collections.sort to sort Repository, currently by name.
 */
public class RepositoryComparator implements Comparator<Repository> {

	/**
	 * Constructor.
	 */
	public RepositoryComparator () {
	}
	
	/**
	 * If repositoryA is < repositoryB, return -1.
	 * If repositoryA = repositoryB, return 0.
	 * If repositoryA is > repositoryB, return 1
	 */
	public int compare(Repository repositoryA, Repository repositoryB) {
		// IrregSecond is always first, generally equivalent to instantaneous
		String nameA = repositoryA.getName();
		String nameB = repositoryB.getName();

		return nameA.compareTo(nameB);
	}
}