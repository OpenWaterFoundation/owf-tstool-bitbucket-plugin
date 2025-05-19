// Issue - results from the issues service

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import RTi.Util.Time.DateTime;

/**
 * Bitbucket "issues" objects from 'values' array.
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
      "id": 2154,
      "repository": {
        "type": "<string>"
      },
      "title": "<string>",
      "reporter": {
        "type": "<string>"
      },
      "assignee": {
        "type": "<string>"
      },
      "created_on": "<string>",
      "updated_on": "<string>",
      "edited_on": "<string>",
      "state": "submitted",
      "kind": "bug",
      "priority": "trivial",
      "milestone": {
        "type": "<string>"
      },
      "version": {
        "type": "<string>"
      },
      "component": {
        "type": "<string>"
      },
      "votes": 2154,
      "content": {
        "raw": "<string>",
        "markup": "markdown",
        "html": "<string>"
      }
    }
  ]
}
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Issue {
	// Alphabetize.

	/**
	 * Age of the issue in days.
	 */
	@JsonIgnore
	private Integer ageDays = null;

	/**
	 * Assignee.
	 */
	@JsonProperty("assignee")
	private User assignee = null;

	/**
	 * "created_on"
	 */
	@JsonProperty("content")
	private IssueContent content = null;

	/**
	 * "created_on"
	 */
	@JsonProperty("created_on")
	private String createdOn = "";

	/**
	 * "created_on" as a DateTime
	 */
	@JsonIgnore
	private DateTime createdOnDateTime = null;

	/**
	 * "edited_on"
	 */
	@JsonProperty("edited_on")
	private String editedOn = "";

	/**
	 * "id"
	 */
	private Integer id = null;

	/**
	 * "links"
	 * 
	 * This could be a map containing link objects for for now fully describe with specific Link objects.
	 */
	@JsonProperty("links")
	private IssueLinks issueLinks = null;

	/**
	 * "kind"
	 */
	private String kind = "";

	/**
	 * "name"
	 */
	private String name = "";

	/**
	 * "priority"
	 */
	private String priority = "";
	
	/**
	 * Properties extracted from the content.
	 */
	private Map<String,String> propertiesMap = new HashMap<String,String>();
	
	/**
	 * Reporter.
	 */
	@JsonProperty("reporter")
	private User reporter = null;

	/**
	 * Repository object (not from the service).
	 */
	@JsonIgnore
	private Repository repositoryObject = null;

	/**
	 * "state"
	 */
	private String state = "";

	/**
	 * "title"
	 */
	private String title = "";

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
	 * "updated_on" as a DateTime
	 */
	@JsonIgnore
	private DateTime updatedOnDateTime = null;

	/**
	 * Default constructor used by Jackson.
	 */
	public Issue () {
	}

	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
	}

	/**
	 * Find an issue given its 'id'.
	 * @param issueList list of issues to search
	 * @param id issue identifier to match
	 * @return the matching issue, or null if not matched
	 */
	public static Issue findForId (List<Issue> issueList, Integer id ) {
		if ( issueList == null ) {
			return null;
		}
		if ( id == null ) {
			return null;
		}
		for ( Issue issue : issueList ) {
			if ( issue.getId().equals(id) ) {
				return issue;
			}
		}
		// Not matched.
		return null;
	}

	/**
	 * Return the age in days.
	 * @return the age in days
	 */
	public Integer getAgeDays () {
		if ( this.ageDays != null ) {
			// Return what was previously computed.
			return this.ageDays;
		}
		else {
			// Compute and return.
			if ( this.createdOnDateTime == null ) {
				// Set computed on as a DateTime.
				this.createdOnDateTime = getCreatedOnAsDateTime();
			}
			DateTime now = new DateTime ( DateTime.DATE_CURRENT );
			this.ageDays = now.getAbsoluteDay() - this.createdOnDateTime.getAbsoluteDay() + 1;
			return this.ageDays;
		}
	}

	/**
	 * Return the assignee.
	 * @return the assignee
	 */
	public User getAssignee () {
		return this.assignee;
	}

	/**
	 * Return the issue content.
	 * @return the issue content
	 */
	public IssueContent getContent () {
		return this.content;
	}

	/**
	 * Return the created on date.
	 * @return the created on date
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
	 * Return the edited on date.
	 * @return the created date
	 */
	public String getEditedOn () {
		return this.editedOn;
	}

	/**
	 * Return the edited on time as a date/time.
	 * @return the edited on time as a date/time
	 */
	public DateTime getEditedOnAsDateTime () {
		if ( this.editedOn == null ) {
			return null;
		}
		else {
			return DateTime.parse(this.editedOn);
		}
	}

	/**
	 * Return the ID.
	 * @return the ID
	 */
	public Integer getId () {
		return this.id;
	}

	/**
	 * Return the issue links.
	 * @return the issue links
	 */
	public IssueLinks getIssueLinks () {
		return this.issueLinks;
	}

	/**
	 * Return the kind.
	 * @return the kind
	 */
	public String getKind () {
		return this.kind;
	}

	/**
	 * Return the name.
	 * @return the name
	 */
	public String getName () {
		return this.name;
	}

	/**
	 * Return the priority.
	 * @return the priority
	 */
	public String getPriority () {
		return this.priority;
	}

	/**
	 * Return the reporter.
	 * @return the reporter
	 */
	public User getReporter () {
		return this.reporter;
	}

	/**
	 * Return the associated Repository object.
	 * @return the associated Repository object.
	 */
	public Repository getRepositoryObject () {
		return this.repositoryObject;
	}

	/**
	 * Return the state.
	 * @return the state
	 */
	public String getState () {
		return this.state;
	}

	/**
	 * Return the title.
	 * @return the title
	 */
	public String getTitle () {
		return this.title;
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
	 * Determine whether the state indicates an open issue (state must be 'new' or 'open').
	 * @return true if the state indicates an open issue, false otherwise
	 */
	public boolean isOpenIssue () {
		if ( this.state.equals("new") || this.state.equals("open") ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Parse the properties from the issue raw content.
	 * Properties are embedded in comment lines formatted like the following.
	 * Currently values cannot contain spaces.
	 * 
	 *  // Property1=Value1 Property2=Value2
	 */
	public void parseProperties () {
		// Get the raw content as a blob.
		String raw = getContent().getRaw();
		
		// The content lines seem to be delimited \r\n so split by \n first.
		String[] lines = raw.split("\n");
		for ( String line : lines ) {
			if ( line.startsWith("//") && line.contains("=") ) {
				// Detected a comment line:
				// - it could be in a code block but for now assume not
				// - split by spaces
				String [] properties = line.substring(2).split(" ");
				for ( String property : properties ) {
					if ( property.contains("=") ) {
						String [] parts = property.split("=");
						if ( parts.length == 2 ) { 
							this.propertiesMap.put(parts[0].trim(), parts[1].trim());
						}
					}
				}
			}
		}
	}
	
	/**
	 * 
	 */
	public String getProperty ( String propertyName ) {
		return this.propertiesMap.get ( propertyName );
	}

	/**
	 * Set the assignee.
	 * @param asignee the assignee
	 */
	public void setAssignee ( User assignee) {
		this.assignee = assignee;
	}

	/**
	 * Set the associated Repository object.
	 * @param repositoryObject the associated Repository object
	 */
	public void setRepositoryObject ( Repository repositoryObject) {
		this.repositoryObject = repositoryObject;
	}

}