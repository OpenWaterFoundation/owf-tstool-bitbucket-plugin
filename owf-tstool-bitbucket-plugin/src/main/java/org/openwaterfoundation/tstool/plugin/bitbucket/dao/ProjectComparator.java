package org.openwaterfoundation.tstool.plugin.bitbucket.dao;

import java.util.Comparator;

/**
 * Comparator for Collections.sort to sort Project, currently by name.
 */
public class ProjectComparator implements Comparator<Project> {

	/**
	 * Constructor.
	 */
	public ProjectComparator () {
	}
	
	/**
	 * If projectA is < projectB, return -1.
	 * If projectA = projectB, return 0.
	 * If projectA is > projectB, return 1
	 */
	public int compare(Project projectA, Project projectB) {
		String nameA = projectA.getName();
		String nameB = projectB.getName();

		return nameA.compareTo(nameB);
	}
}