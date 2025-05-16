// RepositoryProject - results from repositories service

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
 * Bitbucket repository project.
 *
{
      "project": {
        "type": "<string>"
      },
}
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class RepositoryProject {
	/**
	 * "type"
	 */
	private String type = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public RepositoryProject () {
	}

	/**
	 * Return the type.
	 * @return the type
	 */
	public String getType () {
		return this.type;
	}

}