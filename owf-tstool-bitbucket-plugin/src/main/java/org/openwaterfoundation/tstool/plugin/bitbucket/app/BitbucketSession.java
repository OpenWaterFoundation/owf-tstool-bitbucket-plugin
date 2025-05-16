// BitbucketSession - Bitbucket session data

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

package org.openwaterfoundation.tstool.plugin.bitbucket.app;

import java.util.Base64;

/*
 * Bitbucket session for a user,
 * which includes authentication information.
 */
public class BitbucketSession {
	
	/**
	 * Root URL for Bitbucket Cloud, common to all accounts.
	 */
	private String bitbucketCloudUrl = "";
	
	/**
	 * Authentication type.
	 */
	private BitbucketAuthenticationType authenticationType = null;

	/**
	 * App password.
	 */
	private String appPassword = "";

	/**
	 * User name.
	 */
	private String userName = "";

	/**
	 * Workspace ID.
	 */
	private String workspaceId = "";
	
	/**
	 * Create a new session using a app password.
	 */
	public BitbucketSession ( String workspaceId, String userName, String appPassword ) {
		this.authenticationType = BitbucketAuthenticationType.APP_PASSWORD;
		this.workspaceId = workspaceId;
		this.userName = userName;
		this.appPassword = appPassword;
	}
	
	/**
	 * Get the authentication to pass in HTTPS requests.
	 * The format is "UserName:AppPassword".
	 * @return the authentication header to pass in HTTPS requests
	 */
	public String getAuthorization () {
		String auth = this.userName + ":" + this.appPassword;
		String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
		return encodedAuth;
	}

	/**
	 * Get the workspace ID.
	 * @return the workspace ID
	 */
	public String getWorkspaceId () {
		return this.workspaceId;
	}
}