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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Bitbucket "issues" objects from 'values' array.
 *
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
        "avatar": {
          "href": "<string>",
          "name": "<string>"
        },
        "pullrequests": {
          "href": "<string>",
          "name": "<string>"
        },
        "commits": {
          "href": "<string>",
          "name": "<string>"
        },
        "forks": {
          "href": "<string>",
          "name": "<string>"
        },
        "watchers": {
          "href": "<string>",
          "name": "<string>"
        },
        "downloads": {
          "href": "<string>",
          "name": "<string>"
        },
        "clone": [
          {
            "href": "<string>",
            "name": "<string>"
          }
        ],
        "hooks": {
          "href": "<string>",
          "name": "<string>"
        }
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class RepositoryLinks {
	// Alphabetize.

	/**
	 * "avatar"
	 */
	private Link avatar = null;

	/**
	 * "clone"
	 */
	private List<Link> cloneList = new ArrayList<>();

	/**
	 * "comments"
	 */
	private Link comments = null;

	/**
	 * "downloads"
	 */
	private Link downloads = null;

	/**
	 * "forks"
	 */
	private Link forks = null;

	/**
	 * "hooks"
	 */
	private Link hooks = null;

	/**
	 * "html"
	 */
	private Link html = null;

	/**
	 * "pullRequests"
	 */
	private Link pullRequests = null;

	/**
	 * "self"
	 */
	private Link self = null;

	/**
	 * "watchers"
	 */
	private Link watchers = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public RepositoryLinks () {
	}

	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
	}

	/**
	 * Return the avatar link.
	 * @return the avatar link
	 */
	public Link getAvatar() {
		return this.avatar;
	}

	/**
	 * Return the clone link.
	 * @return the clone link
	 */
	public List<Link> getCloneList() {
		return this.cloneList;
	}

	/**
	 * Return the comments link.
	 * @return the comments link
	 */
	public Link getComments() {
		return this.comments;
	}

	/**
	 * Return the downloads link.
	 * @return the downloads link
	 */
	public Link getDownloads() {
		return this.downloads;
	}

	/**
	 * Return the forks link.
	 * @return the forks link
	 */
	public Link getForks() {
		return this.forks;
	}

	/**
	 * Return the hooks link.
	 * @return the hooks link
	 */
	public Link getHooks() {
		return this.hooks;
	}

	/**
	 * Return the HTML link.
	 * @return the HTML link
	 */
	public Link getHtml() {
		return this.html;
	}

	/**
	 * Return the pull requests link.
	 * @return the pull requests link
	 */
	public Link getPullRequests() {
		return this.pullRequests;
	}

	/**
	 * Return the self link.
	 * @return the self link
	 */
	public Link getSelf() {
		return this.self;
	}

	/**
	 * Return the watchers link.
	 * @return the watchers link
	 */
	public Link getWatchers() {
		return this.watchers;
	}

}