// Repository - results from repositories service

/* NoticeStart

OWF TSTool Bitbucket Plugin
Copyright (C) 2024-2025 Open Water Foundation

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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import RTi.Util.Time.DateTime;

/**
 * Bitbucket "repositories" objects from 'values' array.
 *
{
  "size": 142,
  "page": 102,
  "pagelen": 159,
  "next": "<string>",
  "previous": "<string>",
  "values": [
    {
      "type": "<string>",
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
      },
      "uuid": "<string>",
      "full_name": "<string>",
      "is_private": true,
      "scm": "git",
      "owner": {
        "type": "<string>"
      },
      "name": "<string>",
      "description": "<string>",
      "created_on": "<string>",
      "updated_on": "<string>",
      "size": 2154,
      "language": "<string>",
      "has_issues": true,
      "has_wiki": true,
      "fork_policy": "allow_forks",
      "project": {
        "type": "<string>"
      },
      "mainbranch": {
        "type": "<string>"
      }
    }
  ]
}
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Repository {
	// Alphabetize.

	/**
	 * "created_on"
	 */
	@JsonProperty("created_on")
	private String createdOn = "";

	/**
	 * "description"
	 */
	private String description = "";

	/**
	 * "fork_policy"
	 */
	@JsonProperty("fork_policy")
	private String forkPolicy = "";

	/**
	 * "full_name"
	 */
	@JsonProperty("full_name")
	private String fullName = "";

	/**
	 * "has_issues"
	 */
	@JsonProperty("has_issues")
	private Boolean hasIssues = null;

	/**
	 * "has_wiki"
	 */
	@JsonProperty("has_wiki")
	private Boolean hasWiki = null;

	/**
	 * "is_private"
	 */
	@JsonProperty("is_private")
	private Boolean isPrivate = null;

	/**
	 * "language"
	 */
	private String language = "";

	/**
	 * "mainBranch"
	 */
	private RepositoryMainBranch mainBranch = null;

	/**
	 * "name"
	 */
	private String name = "";

	/**
	 * "owner"
	 */
	private RepositoryOwner owner = null;

	/**
	 * "project"
	 */
	private RepositoryProject project = null;

	/**
	 * "scm"
	 */
	private String scm = "";

	/**
	 * "size"
	 */
	private Integer size = null;

	/**
	 * "slug"
	 */
	private String slug = "";

	/**
	 * "type"
	 */
	private String type = "";

	/**
	 * "updated_on"
	 */
	@JsonProperty("updated_on")
	private String updatedOn = "";

	/**
	 * "uuid"
	 */
	private String uuid = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public Repository () {
	}

	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
	}

	/**
	 * Find a repository given its 'uuid' as a string.
	 * @param repositoryList list of projects to search
	 * @param uuid repository identifier to match
	 * @return the matching repository, or null if not matched
	 */
	public static Repository findForUuid (List<Repository> repositoryList, String uuid ) {
		if ( repositoryList == null ) {
			return null;
		}
		if ( uuid == null ) {
			return null;
		}
		for ( Repository repository : repositoryList ) {
			if ( repository.getUuid().equals(uuid) ) {
				return repository;
			}
		}
		// Not matched.
		return null;
	}

	/**
	 * Return the created date.
	 * @return the created date
	 */
	public String getCreatedOn () {
		return this.createdOn;
	}

	/**
	 * Return the created on time as a date/time.
	 * @return the created on time as a date/time
	 */
	public DateTime getCreatedOnAsDateTime () {
		if ( this.createdOn == null ) {
			return null;
		}
		else {
			return DateTime.parse(this.createdOn);
		}
	}

	/**
	 * Return the description
	 * @return the description
	 */
	public String getDescription () {
		return this.description;
	}

	/**
	 * Return the fork policy.
	 * @return the fork policy
	 */
	public String getForkPolicy () {
		return this.forkPolicy;
	}

	/**
	 * Return the full name.
	 * @return the full name
	 */
	public String getFullName () {
		return this.fullName;
	}

	/**
	 * Return whether the repository has issues.
	 * @return whether the repository has issues 
	 */
	public Boolean getHasIssues () {
		return this.hasIssues;
	}

	/**
	 * Return whether the repository has a wiki.
	 * @return whether the repository has a wiki 
	 */
	public Boolean getHasWiki () {
		return this.hasWiki;
	}

	/**
	 * Return whether the repository is private.
	 * @return whether the repository is private
	 */
	public Boolean getIsPrivate () {
		return this.isPrivate;
	}

	/**
	 * Return the language.
	 * @return the language
	 */
	public String getLanguage () {
		return this.language;
	}

	/**
	 * Return the main branch.
	 * @return the main branch
	 */
	public RepositoryMainBranch getMainBranch () {
		return this.mainBranch;
	}

	/**
	 * Return the name.
	 * @return the name
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * Return the owner.
	 * @return the owner 
	 */
	public RepositoryOwner getOwner () {
		return this.owner;
	}

	/**
	 * Return the project.
	 * @return the project 
	 */
	public RepositoryProject getProject () {
		return this.project;
	}

	/**
	 * Return the SCM.
	 * @return the SCM
	 */
	public String getScm () {
		return this.scm;
	}

	/**
	 * Return the size.
	 * @return the size
	 */
	public Integer getSize () {
		return this.size;
	}

	/**
	 * Return the slugified name.
	 * @return the slugified name
	 */
	public String getSlug () {
		return this.slug;
	}

	/**
	 * Return the type.
	 * @return the type
	 */
	public String getType () {
		return this.type;
	}

	/**
	 * Return the date/time updated on.
	 * @return the date/time updated on
	 */
	public String getUpdatedOn () {
		return this.updatedOn;
	}

	/**
	 * Return the date/time updated on.
	 * @return the date/time updated on, or null
	 */
	public DateTime getUpdatedOnAsDateTime () {
		if ( this.updatedOn == null ) {
			return null;
		}
		else {
			return DateTime.parse(this.updatedOn);
		}
	}

	/**
	 * Return the UUID.
	 * @return the UUID
	 */
	public String getUuid () {
		return this.uuid;
	}

}