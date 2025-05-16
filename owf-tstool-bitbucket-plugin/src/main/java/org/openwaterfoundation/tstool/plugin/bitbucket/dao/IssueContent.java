// IssueContent - issue content

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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bitbucket "issue content" objects.
 * <pre>
 *      "content": {
 *      "raw": "<string>",
 *      "markup": "markdown",
 *      "html": "<string>"
 *    }
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class IssueContent {
	// Alphabetize.

	/**
	 * "html"
	 */
	@JsonProperty("html")
	private String html = "";

	/**
	 * "markup"
	 */
	@JsonProperty("markup")
	private String markup = "";

	/**
	 * "raw"
	 */
	@JsonProperty("raw")
	private String raw = "";

	/**
	 * Default constructor used by Jackson.
	 */
	public IssueContent () {
	}

	/**
	 * Return the HTML content.
	 * @return the HTML content
	 */
	public String getHtml () {
		return this.html;
	}

	/**
	 * Return the markup content
	 * @return the markup content
	 */
	public String getMarkup () {
		return this.markup;
	}

	/**
	 * Return the raw content
	 * @return the raw content
	 */
	public String getRaw () {
		return this.raw;
	}
}