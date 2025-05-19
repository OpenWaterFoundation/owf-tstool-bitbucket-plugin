// BitbucketDataStore - class that implements the Bitbucket plugin datastore

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

package org.openwaterfoundation.tstool.plugin.bitbucket.datastore;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openwaterfoundation.tstool.plugin.bitbucket.PluginMeta;
import org.openwaterfoundation.tstool.plugin.bitbucket.app.BitbucketSession;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Issue;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.IssueComparator;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Project;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.ProjectComparator;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Repository;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.RepositoryComparator;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.User;
import org.openwaterfoundation.tstool.plugin.bitbucket.dto.IssuesResponse;
import org.openwaterfoundation.tstool.plugin.bitbucket.dto.ProjectsResponse;
import org.openwaterfoundation.tstool.plugin.bitbucket.dto.RepositoriesResponse;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import RTi.TS.TS;
import RTi.TS.TSIdent;
import RTi.Util.GUI.InputFilter_JPanel;
import RTi.Util.GUI.JWorksheet_AbstractExcelCellRenderer;
import RTi.Util.GUI.JWorksheet_AbstractRowTableModel;
import RTi.Util.IO.PropList;
import RTi.Util.IO.RequirementCheck;
import RTi.Util.IO.UrlReader;
import RTi.Util.IO.UrlResponse;
import RTi.Util.Message.Message;
import RTi.Util.String.MultiKeyStringDictionary;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Time.DateTime;
import riverside.datastore.AbstractWebServiceDataStore;
import riverside.datastore.DataStoreRequirementChecker;
import riverside.datastore.PluginDataStore;

/**
 * Datastore for Bitbucket Cloud web services (NOT Bitbucket Server).
 * Dummy out the time series methods for now - may implement later, for example to show when commits have occurred.
 * See the API documentation: https://developer.atlassian.com/cloud/bitbucket/rest/
 */
public class BitbucketDataStore extends AbstractWebServiceDataStore implements DataStoreRequirementChecker, PluginDataStore {

	/**
	 * Standard request parameters:
	 * - request 100 items per page to minimize the number of requests
	 */
	private final String COMMON_REQUEST_PARAMETERS = "?pagelen=100";

	// TODO smalers 2023-11-13 why is this separate from the built-in datastore properties?
	/**
	 * Properties for the plugin, used to help with application integration.
	 */
	Map<String,Object> pluginProperties = new LinkedHashMap<>();

	/**
	 * Global project list.
	 */
	//List<Project> projectList = new ArrayList<>();

	/**
	 * Global repository list.
	 */
	//List<Repository> repositoryList = new ArrayList<>();

	/**
	 * Expiration time at which global data will be refreshed.
	 */
	OffsetDateTime globalDataExpirationTime = null;

	/**
	 * Global data read problems:
	 * - if not empty, this should be set as an error in the Bitbucket command to indicate incomplete data
	 */
	List<String> globalDataProblems = new ArrayList<>();

	/**
	 * Global debug option for datastore, used for development and troubleshooting.
	 */
	private boolean debug = false;

	/**
	Constructor for web service.
	@param name identifier for the data store
	@param description name for the data store
	@param dmi DMI instance to use for the data store.
	*/
	public BitbucketDataStore ( String name, String description, URI serviceRootURI, PropList props ) {
		String routine = getClass().getSimpleName() + ".BitbucketDataStore";

		String prop = props.getValue("Debug");
		if ( (prop != null) && prop.equalsIgnoreCase("true") ) {
			Message.printStatus(2, routine, "Datastore \"" + name + "\" - detected Debug=true");
			this.debug = true;
		}
	    setName ( name );
	    setDescription ( description );
	    setServiceRootURI ( serviceRootURI );
	    // Set the properties in the datastore.
	    setProperties ( props );

	    // Set standard plugin properties:
        // - plugin properties can be listed in the main TSTool interface
        // - version is used to create a versioned installer and documentation.
        this.pluginProperties.put("Name", "Open Water Foundation Bitbucket data web services plugin");
        this.pluginProperties.put("Description", "Plugin to integrate TSTool with bitbucket web services.");
        this.pluginProperties.put("Author", "Open Water Foundation, https://openwaterfoundation.org");
        this.pluginProperties.put("Version", PluginMeta.VERSION);

	    // Read global data used throughout the session:
	    // - in particular a cache of the TimeSeriesCatalog used for further queries

	    readGlobalData();
	}

	/**
	 * Check global data to evaluate whether it has expired.
	 * If the global data are expired, read it again.
	 */
	public void checkGlobalDataExpiration () {
		String routine = getClass().getSimpleName() + ".checkGlobalDataExpiration";
		OffsetDateTime now = OffsetDateTime.now();
		if ( (this.globalDataExpirationTime != null) && now.isAfter(this.globalDataExpirationTime) ) {
			// Global data have expired so read it again.
			Message.printStatus(2, routine, "Global data have expired.  Reading current data.");
			readGlobalData();
		}
	}

	/**
 	* Check the web service requirement for DataStoreRequirementChecker interface, for example one of:
 	* <pre>
 	* @require datastore bitbucket version >= 1.5.5
 	* @require datastore bitbucket ?configproperty propname? == Something
 	*
 	* @enabledif datastore bitbucket version >= 1.5.5
 	* </pre>
 	* @param check a RequirementCheck object that has been initialized with the check text and
 	* will be updated in this method.
 	* @return whether the requirement condition is met, from call to check.isRequirementMet()
 	*/
	public boolean checkRequirement ( RequirementCheck check ) {
		String routine = getClass().getSimpleName() + ".checkRequirement";
		// Parse the string into parts:
		// - calling code has already interpreted the first 3 parts to be able to do this call
		String requirement = check.getRequirementText();
		Message.printStatus(2, routine, "Checking requirement: " + requirement);
		// Get the annotation that is being checked, so messages are appropriate.
		String annotation = check.getAnnotation();
		String [] requireParts = requirement.split(" ");
		// Datastore name may be an original name but a substitute is used, via TSTool command line.
		String dsName = requireParts[2];
		String dsNameNote = ""; // Note to add on messages to help confirm how substitutions are being handled.
		String checkerName = "BitbucketDataStore";
		if ( !dsName.equals(this.getName())) {
			// A substitute datastore name is being used, such as in testing.
			dsNameNote = "\nCommand file datastore name '" + dsName + "' substitute that is actually used is '" + this.getName() + "'";
		}
		if ( requireParts.length < 4 ) {
			check.setIsRequirementMet(checkerName, false, "Requirement does not contain check type as one of: version, configuration, "
				+ "for example: " + annotation + " datastore bitbucket version...");
			return check.isRequirementMet();
		}
		String checkType = requireParts[3];
		if ( checkType.equalsIgnoreCase("configuration") ) {
			// Checking requirement of form:
			// 0        1         2             3             4         5  6
			// @require datastore bitbucket configuration
			String propertyName = requireParts[4];
			String operator = requireParts[5];
			String checkValue = requireParts[6];
			// Get the configuration table property of interest:
			// - currently only support checking system_id
			if ( propertyName.equals("xxx") ) {
				// Leave this code in as an example.
				// Know how to handle "system_id" property.
				if ( (checkValue == null) || checkValue.isEmpty() ) {
					// Unable to do check.
					check.setIsRequirementMet ( checkerName, false, "'xxx' value to check is not specified in the requirement." + dsNameNote );
					return check.isRequirementMet();
				}
				else {
					// TODO smalers 2023-01-03 need to evaluate whether datastore has configuration properties.
					//String propertyValue = readConfigurationProperty(propertyName);
					String propertyValue = "";
					if ( (propertyValue == null) || propertyValue.isEmpty() ) {
						// Unable to do check.
						check.setIsRequirementMet ( checkerName, false, "Bitbucket configuration 'xxx' value is not defined." + dsNameNote );
						return check.isRequirementMet();
					}
					else {
						if ( StringUtil.compareUsingOperator(propertyValue, operator, checkValue) ) {
							check.setIsRequirementMet ( checkerName, true, "Bitbucket configuration property '" + propertyName + "' value (" + propertyValue +
								") does meet the requirement: " + operator + " " + checkValue + dsNameNote );
						}
						else {
							check.setIsRequirementMet ( checkerName, false, "Bitbucket configuration property '" + propertyName + "' value (" + propertyValue +
								") does not meet the requirement:" + operator + " " + checkValue + dsNameNote );
						}
						return check.isRequirementMet();
					}
				}
			}
			else {
				// Other properties may not be easy to compare.  Probably need to use "contains" and other operators.
				check.setIsRequirementMet ( checkerName, false, "Check type '" + checkType + "' configuration property '" + propertyName + "' is not supported.");
				return check.isRequirementMet();
			}
		}
		/* TODO smalers 2023-11-11 need to implement, maybe need to define the system ID in the configuration file as a cross check for testing.
		else if ( checkType.equalsIgnoreCase("configproperty") ) {
			if ( parts.length < 7 ) {
				// 'property' requires 7 parts
				throw new RuntimeException( "'configproperty' requirement does not contain at least 7 parts for: " + requirement);
			}
		}
		*/
		else if ( checkType.equalsIgnoreCase("version") ) {
			// Checking requirement of form:
			// 0        1         2             3       4  5
			// @require datastore nsdataws-mhfd version >= 1.5.5
			Message.printStatus(2, routine, "Checking web service version.");
			// Do a web service round trip to check version since it may change with software updates.
			String wsVersion = readVersion();
			if ( (wsVersion == null) || wsVersion.isEmpty() ) {
				// Unable to do check.
				check.setIsRequirementMet ( checkerName, false, "Web service version is unknown (services are down or software problem).");
				return check.isRequirementMet();
			}
			else {
				// Web service versions are strings of format A.B.C.D so can do semantic version comparison:
				// - only compare the first 3 parts
				//Message.printStatus(2, "checkRequirement", "Comparing " + wsVersion + " " + operator + " " + checkValue);
				String operator = requireParts[4];
				String checkValue = requireParts[5];
				boolean verCheck = StringUtil.compareSemanticVersions(wsVersion, operator, checkValue, 3);
				String message = "";
				if ( !verCheck ) {
					message = annotation + " web service version (" + wsVersion + ") does not meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				else {
					message = annotation + " web service version (" + wsVersion + ") does meet requirement: " + operator + " " + checkValue+dsNameNote;
					check.setIsRequirementMet ( checkerName, verCheck, message );
				}
				return check.isRequirementMet();
			}
		}
		else {
			// Unknown check type.
			check.setIsRequirementMet ( checkerName, false, "Requirement check type '" + checkType + "' is unknown.");
			return check.isRequirementMet();
		}

	}

	/**
 	* Return the identifier for a time series in the table model.
 	* The TSIdent parts will be uses as TSID commands.
 	* @param tableModel the table model from which to extract data
 	* @param row the displayed table row, may have been sorted
 	*/
	public TSIdent getTimeSeriesIdentifierFromTableModel( @SuppressWarnings("rawtypes") JWorksheet_AbstractRowTableModel tableModel,
		int row ) {
		return null;
	}

	/**
	 * Create a time series input filter, used to initialize user interfaces.
	 */
	public InputFilter_JPanel createTimeSeriesListInputFilterPanel () {
		return null;
	}

	/**
	 * Create a time series list table model given the desired data type, time step (interval), and input filter.
	 * The datastore performs a suitable query and creates objects to manage in the time series list.
	 * @param dataType time series data type to query, controlled by the datastore
	 * @param timeStep time interval to query, controlled by the datastore
	 * @param ifp input filter panel that provides additional filter options
	 * @return a TableModel containing the defined columns and rows.
	 */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel createTimeSeriesListTableModel(String dataType, String timeStep, InputFilter_JPanel ifp ) {
		return null;
	}

	/**
 	* Create a work table with standard columns.
 	* @param workTableID identifier for the table to be created
 	* @return new table with standard columns
 	*/
	public DataTable createWorkTable ( String workTableID ) {
    	DataTable table = new DataTable();
    	table.setTableID ( workTableID );
    	// Currently columns are hand-coded so don't need to handle dynamically.
    	//int workTableDateColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "Date", -1, -1), null);
    	//int workUserColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_STRING, "Person", -1, -1), null);
    	//int workHoursColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_FLOAT, "Hours", -1, 1), null);
    	//int workDescriptionColumn =
    			table.addField(new TableField(TableField.DATA_TYPE_STRING, "Description", -1, -1), null);
    	return table;
	}

	/**
	 * Return the API key used in the header of requests.
	 */
	public String getApiKey () {
		//Object prop = this.pluginProperties.get("ApiKey");
		Object prop = getProperties().getValue("ApiKey");
		if ( prop == null ) {
			return null;
		}
		else {
			return (String)prop;
		}
	}

	/**
	 * Return the authentication string used in the header of requests.
	 */
	public String getAuthorization () {
		//Object prop = this.pluginProperties.get("Authorization");
		Object prop = getProperties().getValue("Authorization");
		if ( prop == null ) {
			return null;
		}
		else {
			return (String)prop;
		}
	}

    /**
     * Get the global data expiration time.
     * @return the global data expiration time
     */
    public OffsetDateTime getGlobalDataExpirationTime () {
    	return this.globalDataExpirationTime;
    }

    /**
     * Get the global data problems.
     * @return the global data problems list
     */
    public List<String> getGlobalDataProblems () {
    	return this.globalDataProblems;
    }

	/**
	 * Get the HTTP request properties (HTTP headers).
	 * This must be added to all HTTP requests.
	 * @return a dictionary of HTTP request headers.
	 */
	public MultiKeyStringDictionary getHttpRequestProperties ( BitbucketSession session ) {
		MultiKeyStringDictionary requestProperties = new MultiKeyStringDictionary();
		String authorizationValue = "Basic " + session.getAuthorization();
		Message.printStatus(2, "", "Adding request header Authorization=" + authorizationValue );
		// Adding the authentication results in a closed Stream exception.
		requestProperties.add("Authorization", authorizationValue );
		return requestProperties;
	}

	/**
 	* Get the properties for the plugin.
 	* A copy of the properties map is returned so that calling code cannot change the properties for the plugin.
 	* @return plugin properties map.
 	*/
	public Map<String,Object> getPluginProperties () {
		Map<String,Object> pluginProperties = new LinkedHashMap<>();
		// For now the properties are all strings so it is easy to copy.
    	for (Map.Entry<String, Object> entry : this.pluginProperties.entrySet()) {
        	pluginProperties.put(entry.getKey(), entry.getValue());
    	}
		return pluginProperties;
	}

    /**
     * Get the global list of cached projects.
     * @return the global list of cached projects
     */
	/*
    public List<Project> getProjectCache () {
    	return this.projectList;
    }
    */

    /**
     * Get the global list of cached repositories.
     * @return the global list of cached repositories
     */
    //public List<Repository> getRepositoryCache () {
    	//return this.repositoryList;
    //}

	/**
	 * This version is required by TSTool UI.
	 * Return the list of time series data interval strings.
	 * Interval strings match TSTool conventions such as NewTimeSeries command, which uses "1Hour" rather than "1hour".
	 * This should result from calls like:  TimeInterval.getName(TimeInterval.HOUR, 0)
	 * @param dataType data type string to filter the list of data intervals.
	 * If null, blank, or "*" the data type is not considered when determining the list of data intervals.
	 * @includeWildcards if true, include "*" wildcard.
	 */
	public List<String> getTimeSeriesDataIntervalStrings ( String dataTypeString ) {
		//String routine = getClass().getSimpleName() + ".getTimeSeriesDataIntervalStrings";
		List<String> dataIntervals = new ArrayList<>();
		return dataIntervals;
	}

	/**
	 * Return the list of time series data type strings.
	 * This is the version that is required by TSTool UI.
	 * These strings are the same as the dataTypes.name properties from the stationSummaries web service request.
	 * @param dataInterval data interval from TimeInterval.getName(TimeInterval.HOUR,0) to filter the list of data types.
	 * If null, blank, or "*" the interval is not considered when determining the list of data types (treat as if "*").
	 */
	public List<String> getTimeSeriesDataTypeStrings ( String dataInterval ) {
		List<String> dataTypeStrings = new ArrayList<>();
		return dataTypeStrings;
	}

    /**
     * Get the CellRenderer used for displaying the time series in a TableModel.
     */
    @SuppressWarnings("rawtypes")
	public JWorksheet_AbstractExcelCellRenderer getTimeSeriesListCellRenderer(JWorksheet_AbstractRowTableModel tableModel) {
    	return null;
    }

    /**
     * Get the TableModel used for displaying the time series.
     */
	@SuppressWarnings("rawtypes")
	public JWorksheet_AbstractRowTableModel getTimeSeriesListTableModel(List<? extends Object> data) {
    	return null;
    }

	/**
	 * Indicate whether the datastore provides a time series input filter.
	 * This datastore does provide an input filter panel.
	 */
	public boolean providesTimeSeriesListInputFilterPanel () {
		return false;
	}

	/**
	 * Read global data that should be kept in memory to increase performance.
	 * This is called from the constructor.
	 * The following data are read and are available with get() methods:
	 * <ul>
	 * <li>TimeSeriesCatalog - cache used to find time series without re-requesting from the web service</li>
	 * </ul>
	 * If an error is detected, set on the datastore so that TSTool View / Datastores will show the error.
	 * This is usually an issue with a misconfigured datastore.
	 */
	public void readGlobalData () {
		String routine = getClass().getSimpleName() + ".readGlobalData";
		Message.printWarning ( 2, routine, "Reading global data for datastore \"" + getName() + "\"." );
		OffsetDateTime now = OffsetDateTime.now();
		//this.globalDataExpirationTime = now.plusSeconds(this.globalDataExpirationOffset);
		Message.printWarning ( 2, routine, "Global data will expire at: " + this.globalDataExpirationTime );

		// Add to avoid Eclipse warning if 'debug' is not used.
		if ( debug ) {
		}

		// Clear the global data problems.
		this.globalDataProblems.clear();

		// Project objects.

		/*
		try {
			List<Project> projectList0 = readProjects();
			if ( (projectList0.size() == 0) && (this.projectList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 projects." );
				Message.printStatus(2, routine, "Keeping " + this.projectList.size() + " previously read project data." );
				Message.printStatus(2, routine, "May have reached API access limits.  Will try again in " +
				this.globalDataExpirationOffset + " seconds." );
			}
			else {
				this.projectList = projectList0;
				Message.printStatus(2, routine, "Read " + this.projectList.size() + " projects." );
				if ( Message.isDebugOn ) {
					//for ( Project project : this.projectList ) {
					//	Message.printStatus(2, routine, "Project: " + project );
					//}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global projects (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global projects data.");
		}
		*/

		// Repository objects:
		// - this handles HTTP 420

		/*
		try {
			List<Repository> repositoryList0 = readRepositories(null);
			if ( (repositoryList0.size() == 0) && (this.repositoryList.size() > 0) ) {
				Message.printStatus(2, routine, "Read 0 repositories." );
				Message.printStatus(2, routine, "Keeping " + this.repositoryList.size() + " previously read repository data." );
			}
			else {
				this.repositoryList = repositoryList0;
				Message.printStatus(2, routine, "Read " + this.repositoryList.size() + " repositories." );
				if ( Message.isDebugOn ) {
					//for ( Repository repository : this.repositoryList ) {
					//	Message.printStatus(2, routine, "Repository: " + repository );
					//}
				}
			}
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error reading global repositories (" + e + ")");
			Message.printWarning(3, routine, e );
			this.globalDataProblems.add("Error reading global repository data.");
		}
		*/

	}

	/**
 	* Read the 'repository' objects.  Results look like:
 	* {
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

	/**
	 * Read the list of projects.
     * @param workspace the Bitbucket workspace for the projects
     * @param timeoutSeconds the timeout in seconds
 	 * @return a list of Project.
 	 */
	public List<Project> readProjects ( BitbucketSession session, int timeoutSeconds ) throws IOException {
		String routine = getClass().getSimpleName() + ".readProjects";
		//boolean debug = true;
		
		String rootUrl = getServiceRootURI().toString();
		if ( ! rootUrl.endsWith("/") ) {
			// Add a trailing /.
			rootUrl = rootUrl + "/";
		}
		String workspace = session.getWorkspaceId();
		// Encode the URL.
		try {
			workspace = URLEncoder.encode(workspace, StandardCharsets.UTF_8.toString());
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error encoding projects URL.");
		}
		String urlString = rootUrl + "/workspaces/" + workspace + "/projects" + COMMON_REQUEST_PARAMETERS;

		// Get request header properties for authentication.
		MultiKeyStringDictionary requestProperties = getHttpRequestProperties(session);
		
		// Read data one page at a time.
		List<Project> projectList = new ArrayList<>();
		while ( (urlString != null) && !urlString.isEmpty() ) {
			Message.printStatus(2, routine, "Reading projects using: " + urlString);

			String urlStringEncoded = urlString;
			UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
			UrlResponse urlResponse = null;
			try {
				urlResponse = urlReader.read();
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'projects' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
			if ( urlResponse.hadError() ) {
				// TODO smalers 2020-06-12 would be nice to not catch this immediately.
				throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
					+ "): " + urlResponse.getResponseError() );
			}
			else if ( urlResponse.getResponseCode() != 200 ) {
				throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
			}
			else {
				// Parse the response into objects.
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				String responseJson = urlResponse.getResponse();
				Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
				Message.printStatus(2, routine, "JSON response code = " + urlResponse.getResponseCode());
				try {
					ProjectsResponse projectsResponse = mapper.readValue(responseJson, new TypeReference<ProjectsResponse>(){});
					if ( debug ) {
						Message.printStatus(2, routine, "Response=" + responseJson);
					}
					//logResponseErrors ( repositoriesResponse.getErrors() );
					Message.printStatus(2, routine, "Read " + projectsResponse.getValues().size() + " projects.");
					List<Project> projectList0 = projectsResponse.getValues();
					// Add the page of results to the full list of output.
					projectList.addAll(projectList0);
			
					// Set the URL string to the next URL.
					urlString = projectsResponse.getNext();
				}
				catch ( Exception e ) {
					Message.printWarning(3, routine, "Error reading 'projects' using \"" + urlString + "\".");
					Message.printWarning(3, routine, e);
					throw new RuntimeException(e);
				}
			}
		}

		// Sort on the name.
		Collections.sort(projectList, new ProjectComparator());
		return projectList;
	}

	/**
	 * Read the list of repositories.
     * @param workspace the Bitbucket workspace for the repositories
     * @param timeoutSeconds the timeout in seconds
 	 * @return a list of Repository.
 	 */
	public List<Repository> readRepositories ( BitbucketSession session, int timeoutSeconds ) throws IOException {
		String routine = getClass().getSimpleName() + ".readRepositories";
		//boolean debug = true;
		
		String rootUrl = getServiceRootURI().toString();
		if ( ! rootUrl.endsWith("/") ) {
			// Add a trailing /.
			rootUrl = rootUrl + "/";
		}
		String workspace = session.getWorkspaceId();
		// Encode the URL.
		try {
			workspace = URLEncoder.encode(workspace, StandardCharsets.UTF_8.toString());
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error encoding repositories URL.");
		}
		String urlString = rootUrl + "repositories/" + workspace + COMMON_REQUEST_PARAMETERS;

		// Get request header properties for authentication.
		MultiKeyStringDictionary requestProperties = getHttpRequestProperties(session);
		
		// Read data one page at a time.
		List<Repository> repositoryList = new ArrayList<>();
		while ( (urlString != null) && !urlString.isEmpty() ) {
			Message.printStatus(2, routine, "Reading repositories using: " + urlString);

			String urlStringEncoded = urlString;
			UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
			UrlResponse urlResponse = null;
			try {
				urlResponse = urlReader.read();
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'repositories' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
			if ( urlResponse.hadError() ) {
				// TODO smalers 2020-06-12 would be nice to not catch this immediately.
				throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
					+ "): " + urlResponse.getResponseError() );
			}
			else if ( urlResponse.getResponseCode() != 200 ) {
				throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
			}
			else {
				// Parse the response into objects.
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				String responseJson = urlResponse.getResponse();
				Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
				Message.printStatus(2, routine, "JSON response code = " + urlResponse.getResponseCode());
				try {
					RepositoriesResponse repositoriesResponse = mapper.readValue(responseJson, new TypeReference<RepositoriesResponse>(){});
					if ( debug ) {
						Message.printStatus(2, routine, "Response=" + responseJson);
					}
					//logResponseErrors ( repositoriesResponse.getErrors() );
					Message.printStatus(2, routine, "Read " + repositoriesResponse.getValues().size() + " repositories.");
					List<Repository> repositoryList0 = repositoriesResponse.getValues();
					// Add the page of results to the full list of output.
					repositoryList.addAll(repositoryList0);
			
					// Set the URL string to the next URL.
					urlString = repositoriesResponse.getNext();
				}
				catch ( Exception e ) {
					Message.printWarning(3, routine, "Error reading 'repositories' using \"" + urlString + "\".");
					Message.printWarning(3, routine, e);
					throw new RuntimeException(e);
				}
			}
		}

		// Sort on the repository name.
		Collections.sort(repositoryList, new RepositoryComparator());

		return repositoryList;
	}

	/**
	 * Read the list of repository issues.
     * @param workspace the Bitbucket workspace for the repositories
     * @param repository the Bitbucket repository for the issues
     * @param timeoutSeconds the timeout in seconds
 	 * @return a list of Repository.
 	 */
	public List<Issue> readRepositoryIssues ( BitbucketSession session, Repository repository, int timeoutSeconds ) throws IOException {
		String routine = getClass().getSimpleName() + ".readRepositoryIssues";
		//boolean debug = true;
		List<Issue> issueList = new ArrayList<>();
		
		if ( !repository.getHasIssues() ) {
			Message.printStatus(2, routine, "Repository \"" + repository.getName() + "\" has no issues.  Not attempting to read issues.");
			return issueList;
		}
		
		String rootUrl = getServiceRootURI().toString();
		if ( ! rootUrl.endsWith("/") ) {
			// Add a trailing /.
			rootUrl = rootUrl + "/";
		}
		String workspace = session.getWorkspaceId();
		// Encode the URL.
		try {
			workspace = URLEncoder.encode(workspace, StandardCharsets.UTF_8.toString());
		}
		catch ( Exception e ) {
			Message.printWarning(3, routine, "Error encoding repository issues URL.");
		}
		String urlString = rootUrl + "repositories/" + workspace + "/" + repository.getSlug() + "/issues" + COMMON_REQUEST_PARAMETERS;

		// Get request header properties for authentication.
		MultiKeyStringDictionary requestProperties = getHttpRequestProperties(session);
		
		// Read data one page at a time.
		while ( (urlString != null) && !urlString.isEmpty() ) {
			Message.printStatus(2, routine, "Reading issues using: " + urlString);

			String urlStringEncoded = urlString;
			UrlReader urlReader = new UrlReader(urlStringEncoded, requestProperties, null, timeoutSeconds*1000 );
			UrlResponse urlResponse = null;
			try {
				urlResponse = urlReader.read();
			}
			catch ( Exception e ) {
				Message.printWarning(3, routine, "Error reading 'issues' using \"" + urlString + "\".");
				Message.printWarning(3, routine, e);
				throw new RuntimeException(e);
			}
			if ( urlResponse.hadError() ) {
				// TODO smalers 2020-06-12 would be nice to not catch this immediately.
				throw new RuntimeException ( "Reading URL returned error (code=" + urlResponse.getResponseCode()
					+ "): " + urlResponse.getResponseError() );
			}
			else if ( urlResponse.getResponseCode() != 200 ) {
				throw new RuntimeException ( "Reading URL returned error code: " + urlResponse.getResponseCode() );
			}
			else {
				// Parse the response into objects.
				ObjectMapper mapper = new ObjectMapper();
				mapper.registerModule(new JavaTimeModule());
				String responseJson = urlResponse.getResponse();
				Message.printStatus(2, routine, "JSON response has length = " + responseJson.length());
				Message.printStatus(2, routine, "JSON response code = " + urlResponse.getResponseCode());
				try {
					IssuesResponse issuesResponse = mapper.readValue(responseJson, new TypeReference<IssuesResponse>(){});
					if ( debug ) {
						Message.printStatus(2, routine, "Response=" + responseJson);
					}
					//logResponseErrors ( repositoriesResponse.getErrors() );
					Message.printStatus(2, routine, "Read " + issuesResponse.getValues().size() + " issues.");
					List<Issue> issueList0 = issuesResponse.getValues();
					// Add the page of results to the full list of output.
					issueList.addAll(issueList0);
					
					// Set the URL string to the next URL.
					urlString = issuesResponse.getNext();
				}
				catch ( Exception e ) {
					Message.printWarning(3, routine, "Error reading 'issues' using \"" + urlString + "\".");
					Message.printWarning(3, routine, e);
					throw new RuntimeException(e);
				}
			}
		}
		
		// Post-process the data:
		// - set the repository for the issue
		// - create an 'assignee' if not assigned
		// - parse properties from the 
		for ( Issue issue : issueList ) {
			// Save the reference to the associated repository.
			issue.setRepositoryObject(repository);
			
			// Make sure that the assignee is not null.
			if ( issue.getAssignee() == null ) {
				// No assignee so create one that allows grouping later.
				issue.setAssignee ( new User("NotAssigned", "NotAssigned") );
			}

			// Parse the properties.
			issue.parseProperties();
		}

		// Sort on the assignee, repository, priority, and age.
		Collections.sort(issueList, new IssueComparator());

		return issueList;
	}

    /**
     * Read a single time series given its time series identifier using default read properties.
     * @param tsid time series identifier.
     * @param readStart start of read, will be set to 'periodStart' service parameter.
     * @param readEnd end of read, will be set to 'periodEnd' service parameter.
     * @return the time series or null if not read
     */
    public TS readTimeSeries ( String tsid, DateTime readStart, DateTime readEnd, boolean readData ) {
    	//String routine = getClass().getSimpleName() + ".readTimeSeries";
    	return null;
    }

    /**
     * Read the version from the web service, used when processing #@require commands in TSTool.
     * TODO smalers 2023-01-03 need to figure out if a version is available.
     */
    private String readVersion () {
    	return "";
    }

}