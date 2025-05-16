package org.openwaterfoundation.tstool.plugin.bitbucket.dao;

import java.util.Comparator;

/**
 * Comparator for Collections.sort to sort Issue, currently by title.
 */
public class IssueComparator implements Comparator<Issue> {

	/**
	 * Constructor.
	 */
	public IssueComparator () {
	}
	
	/**
	 * Compare based on assignee, repository name, issue properties, priority (critical, blocker, major, minor, trivial), age (oldest first), and title.
	 * If issueA is < issueB, return -1.
	 * If issueA = issueB, return 0.
	 * If issueA is > issueB, return 1
	 */
	public int compare ( Issue issueA, Issue issueB ) {
		// Compare the assignee:
		// - a blank assignee is after a non-blank-assignee
		User assigneeA = issueA.getAssignee();
		String displayNameA = "";
		if ( assigneeA == null ) {
			displayNameA = "";
		}
		else {
			displayNameA = assigneeA.getDisplayName();
		}
		User assigneeB = issueB.getAssignee();
		String displayNameB = "";
		if ( assigneeB == null ) {
			displayNameB = "";
		}
		else {
			displayNameB = assigneeB.getDisplayName();
		}
		if ( displayNameA.isEmpty() && !displayNameB.isEmpty() ) {
			return 1;
		}
		else if ( !displayNameA.isEmpty() && displayNameB.isEmpty() ) {
			return -1;
		}
		int assigneeCompare = displayNameA.compareTo(displayNameB);
		if ( assigneeCompare != 0 ) {
			return assigneeCompare;
		}
		
		// Compare the repository name.
		String repositoryA = issueA.getRepositoryObject().getName();
		String repositoryB = issueB.getRepositoryObject().getName();
		int repositoryCompare = repositoryA.compareTo(repositoryB);
		if ( repositoryCompare != 0 ) {
			return repositoryCompare;
		}

		// Compare the priority:
		// - change blocker to dlocker
		// - otherwise, the order will sort as is
		String priorityA = issueA.getPriority();
		if ( priorityA.equals("blocker") ) {
			priorityA = "dlocker";
		}
		String priorityB = issueB.getPriority();
		if ( priorityB.equals("blocker") ) {
			priorityB = "dlocker";
		}
		int priorityCompare = priorityA.compareTo(priorityB);
		if ( priorityCompare != 0 ) {
			return priorityCompare;
		}

		// Compare the kind (bug, enhancement, proposal, task):
		// - change the values from the original to something that sorts as expected
		String kindA = issueA.getKind();
		// Bug is listed first and is not changed.
		if ( kindA.equals("task") ) {
			// Want task to be second highest.
			kindA = "c-task";
		}
		else if ( kindA.equals("enhancement") ) {
			// Want enhancements to be third highest.
			kindA = "d-enhancement";
		}
		else if ( kindA.equals("proposal") ) {
			// Want proposal to be fourth highest.
			kindA = "e-proposal";
		}
		String kindB = issueB.getKind();
		if ( kindB.equals("task") ) {
			// Want task to be second highest.
			kindB = "c-task";
		}
		else if ( kindB.equals("enhancement") ) {
			// Want enhancements to be third highest.
			kindB = "d-enhancement";
		}
		else if ( kindB.equals("proposal") ) {
			// Want proposal to be fourth highest.
			kindB = "e-proposal";
		}
		int kindCompare = kindA.compareTo(kindB);
		if ( kindCompare != 0 ) {
			return kindCompare;
		}

		// Compare the age (days).
		int ageA = issueA.getAgeDays();
		int ageB = issueB.getAgeDays();
		if ( ageB < ageA ) {
			return -1;
		}
		else if ( ageB > ageA ) {
			return 1;
		}

		// Compare the issue title.
		String titleA = issueA.getTitle();
		String titleB = issueB.getTitle();
		return titleA.compareTo(titleB);
	}
}