// User - user type, used for issue assignee and reporter 

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bitbucket "user" type objects.
 * <pre>
 *      "assignee": {
 *          "display_name": "First Last",
 *          "nickname": "First Last",
 *          "type": "user",
 *    }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class User {
	// Alphabetize.

	/**
	 * "display_name"
	 */
	@JsonProperty("display_name")
	private String displayName = "";

	/**
	 * "nickname"
	 */
	@JsonProperty("nickname")
	private String nickname = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public User () {
	}

	/**
	 * Constructor used when not assigned.
	 */
	public User ( String displayName, String nickname ) {
		this.displayName = displayName;
		this.nickname = nickname;
	}

	/**
	 * Return the display name.
	 * @return the display name
	 */
	public String getDisplayName () {
		return this.displayName;
	}

	/**
	 * Return the nickname.
	 * @return the nickname
	 */
	public String getNickname () {
		return this.nickname;
	}

}