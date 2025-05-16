package org.openwaterfoundation.tstool.plugin.bitbucket.dto;

import java.util.ArrayList;
import java.util.List;

import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Issue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The ProjectsResponse class matches the `projects` service response.
 * The Project object is the main object of interest, but the other top-level data can be used to check for errors.
 * <pre>
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class IssuesResponse {

	/**
	 * List of Issue data objects.
	 */
	private List<Issue> values = new ArrayList<>();
	
	/**
	 * URL for the next request, to handle paging of the results:
	 * - a maximum of 100 items can be requested per page
	 */
	private String next = null;

	/**
	 * Constructor needed by Jackson.
	 */
	public IssuesResponse () {
	}

	/**
	 * Return the URL for the next request.
	 * @return the URL for the next request.
	 */
	public String getNext () {
		return this.next;
	}

	/**
	 * Return the Project objects.
	 * @return the Project objects
	 */
	public List<Issue> getValues () {
		return this.values;
	}
}