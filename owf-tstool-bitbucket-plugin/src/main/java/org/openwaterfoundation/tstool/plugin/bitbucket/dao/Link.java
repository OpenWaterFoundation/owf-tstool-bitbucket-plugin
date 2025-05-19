// Link - results from the issues service, for link item in "links"

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
 * Bitbucket link object used for different objects.
 *
{
      ...
      "links": {
        "self": {
          "href": "<string>",
          "name": "<string>"
        },
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Link {
	// Alphabetize.

	/**
	 * "href"
	 */
	private String href = "";

	/**
	 * "name"
	 */
	private String name = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public Link () {
	}

	/**
	 * Clean the data (e.g., convert strings to other types).
	 * This should be called after reading data using the API.
	 */
	public void cleanData () {
	}

	/**
	 * Return the HREF.
	 * @return the HREF
	 */
	public String getHref() {
		return this.href;
	}

	/**
	 * Return the name.
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

}