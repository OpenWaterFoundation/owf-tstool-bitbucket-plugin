// IssueLinks - results from the issues service, for links

/* NoticeStart

OWF TSTool Bitbucket Plugin
Copyright (C) 2024 Open Water Foundation

OWF TSTool Bitbucket Plugin is free software:  you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

OWF TSTool Bitbucket Plugin is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

You should have received a copy of the GNU General Public License
    along with OWF TSTool Bitbucket Plugin.  If not, see <https://www.gnu.org/licenses/>.

NoticeEnd */

package org.openwaterfoundation.tstool.plugin.bitbucket.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bitbucket "issues" objects from 'values' array.
 *
 * - 'self' is the URL to the API call
 * - 'html' is the link to the issue web page
{
      ...
      "links": {
        "self": {
          "href": "<string>",
          "name": "<string>"
        },
        "html": {
          "href": "<string>",
          "name": "<string>"
        },
        "comments": {
          "href": "<string>",
          "name": "<string>"
        },
        "attachments": {
          "href": "<string>",
          "name": "<string>"
        },
        "watch": {
          "href": "<string>",
          "name": "<string>"
        },
        "vote": {
          "href": "<string>",
          "name": "<string>"
        }
      },
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class IssueLinks {
	// Alphabetize.

	/**
	 * "attachments"
	 */
	private Link attachments = null;

	/**
	 * "comments"
	 */
	private Link comments = null;
	
	/**
	 * "html"
	 */
	private Link html = null;

	/**
	 * "self"
	 */
	private Link self = null;

	/**
	 * "watch"
	 */
	private Link watch = null;

	/**
	 * "vote"
	 */
	private Link vote = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public IssueLinks () {
	}

	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
	}

	/**
	 * Return the attachments link.
	 * @return the attachments link
	 */
	public Link getAttachments() {
		return this.attachments;
	}

	/**
	 * Return the comments link.
	 * @return the comments link
	 */
	public Link getComments() {
		return this.comments;
	}

	/**
	 * Return the HTML link.
	 * @return the HTML link
	 */
	public Link getHtml() {
		return this.html;
	}

	/**
	 * Return the self link.
	 * @return the self link
	 */
	public Link getSelf() {
		return this.self;
	}

	/**
	 * Return the vote link.
	 * @return the vote link
	 */
	public Link getVote() {
		return this.vote;
	}

	/**
	 * Return the watch link.
	 * @return the watch link
	 */
	public Link getWatch() {
		return this.watch;
	}

}