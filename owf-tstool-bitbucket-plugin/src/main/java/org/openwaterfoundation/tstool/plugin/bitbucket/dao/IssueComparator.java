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
	 * Compare two issues.  The intent is to list the most important first, and the less important second.
	 * Compare based on assignee, repository name, issue properties, priority (critical, blocker, major, minor, trivial), age (oldest first), and title.
	 * If issueA is < issueB, return -1.
	 * If issueA = issueB, return 0.
	 * If issueA is > issueB, return 1.  Use 
	 * @return -1 if issueA is less than alphabetically or more important than Issue B, 0 if the same,
	 * or 1 if issueA is greater than alphabetically or more important than issueB
	 */
	public int compare ( Issue issueA, Issue issueB ) {
		// Compare the assignee:
		// - alphabetical
		// - compare the display name
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
		// Check special cases of empty display name (should not happen?).
		if ( displayNameA.isEmpty() && !displayNameB.isEmpty() ) {
			// Want non-empty listed first so treat as if A is greater.
			return 1;
		}
		else if ( !displayNameA.isEmpty() && displayNameB.isEmpty() ) {
			// Want non-empty listed first so treat as if B is greater.
			return -1;
		}
		int assigneeCompare = displayNameA.compareTo(displayNameB);
		if ( assigneeCompare != 0 ) {
			// Display names are not equal so can return.
			return assigneeCompare;
		}
		
		// Compare the repository name:
		// - alphabetical
		// - should always have a value
		String repositoryA = issueA.getRepositoryObject().getName();
		String repositoryB = issueB.getRepositoryObject().getName();
		int repositoryCompare = repositoryA.compareTo(repositoryB);
		if ( repositoryCompare != 0 ) {
			// Repository names are not equal so can return.
			return repositoryCompare;
		}

		// Compare the priority:
		// - alphabetical (because critical is first)
		// - change the values from the original to something that sorts as expected:
		//      critical      critical
		//      blocker       d-blocker
		//      major         major
		//      minor         minor
		//      trivial       trivial
		// - otherwise, the order will sort OK as is
		String priorityA = issueA.getPriority();
		if ( priorityA.equals("blocker") ) {
			priorityA = "d-blocker";
		}
		String priorityB = issueB.getPriority();
		if ( priorityB.equals("blocker") ) {
			priorityB = "d-locker";
		}
		int priorityCompare = priorityA.compareTo(priorityB);
		if ( priorityCompare != 0 ) {
			// Priority names are not equal so can return.
			return priorityCompare;
		}

		// Compare the kind:
		// - alphabetical (because bug is first)
		// - change the values from the original to something that sorts as expected:
		//       bug              bug
		//       task             c-task
		//       enhancement      enhancement
		//       proposal         proposal
		String kindA = issueA.getKind();
		if ( kindA.equals("task") ) {
			// Want task to be second highest.
			kindA = "c-task";
		}
		String kindB = issueB.getKind();
		if ( kindB.equals("task") ) {
			// Want task to be second highest.
			kindB = "c-task";
		}
		int kindCompare = kindA.compareTo(kindB);
		if ( kindCompare != 0 ) {
			return kindCompare;
		}

		// Compare the age (days):
		// - largest first
		int ageA = issueA.getAgeDays();
		int ageB = issueB.getAgeDays();
		if ( ageA < ageB ) {
			// Want B to be at the top. 
			return 1;
		}
		else if ( ageA > ageB ) {
			// Want A to be at the top.
			return -1;
		}

		// Compare the issue title:
		// - alphabetical
		String titleA = issueA.getTitle();
		String titleB = issueB.getTitle();
		return titleA.compareTo(titleB);
	}
}