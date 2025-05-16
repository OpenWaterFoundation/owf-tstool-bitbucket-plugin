package org.openwaterfoundation.tstool.plugin.bitbucket.dto;

import java.util.ArrayList;
import java.util.List;

import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * The RepositoriesResponse class matches the `repositories` service response.
 * The Repository object is the main object of interest, but the other top-level data can be used to check for errors.
 * <pre>
 * {
  "pagelen": 10,
  "values": [
    {
      "scm": "git",
      "website": null,
      "has_wiki": true,
      "uuid": "{a1b2c3d4-e5f6-7890-abcd-1234567890ef}",
      "full_name": "myworkspace/my-repo",
      "name": "my-repo",
      "language": "java",
      "created_on": "2021-06-01T12:00:00.000000+00:00",
      "mainbranch": {
        "type": "branch",
        "name": "main"
      },
      "links": {
        "clone": [
          {
            "href": "https://bitbucket.org/myworkspace/my-repo.git",
            "name": "https"
          },
          {
            "href": "git@bitbucket.org:myworkspace/my-repo.git",
            "name": "ssh"
          }
        ],
        "html": {
          "href": "https://bitbucket.org/myworkspace/my-repo"
        }
      },
      "slug": "my-repo",
      "is_private": true
    },
    {
      "scm": "git",
      "uuid": "{abc12345-6789-def0-1234-567890abcdef}",
      "full_name": "myworkspace/second-repo",
      "name": "second-repo",
      "language": "python",
      "mainbranch": {
        "type": "branch",
        "name": "master"
      },
      "slug": "second-repo",
      "is_private": false
    }
  ],
  "page": 1,
  "size": 2,
  "next" : "https:/url/to/next/page" (or null if done)
}
 * </pre>
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class RepositoriesResponse {

	/**
	 * List of Repository data objects.
	 */
	private List<Repository> values = new ArrayList<>();

	/**
	 * URL for the next request, to handle paging of the results:
	 * - a maximum of 100 items can be requested per page
	 */
	private String next = null;

	/**
	 * Constructor needed by Jackson.
	 */
	public RepositoriesResponse () {
	}

	/**
	 * Return the URL for the next request.
	 * @return the URL for the next request.
	 */
	public String getNext () {
		return this.next;
	}

	/**
	 * Return the Repository objects.
	 * @return the Repository objects
	 */
	public List<Repository> getValues () {
		return this.values;
	}
}