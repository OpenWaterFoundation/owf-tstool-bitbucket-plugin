// Bitbucket_Command - This class initializes, checks, and runs the Bitbucket() command.

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

package org.openwaterfoundation.tstool.plugin.bitbucket.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFrame;

import org.openwaterfoundation.tstool.plugin.bitbucket.app.BitbucketSession;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Issue;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.IssueComparator;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.IssueLinks;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Link;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.User;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Project;
import org.openwaterfoundation.tstool.plugin.bitbucket.dao.Repository;
import org.openwaterfoundation.tstool.plugin.bitbucket.datastore.BitbucketDataStore;

import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

import RTi.Util.IO.AbstractCommand;
import RTi.Util.IO.CommandDiscoverable;
import RTi.Util.IO.CommandException;
import RTi.Util.IO.CommandLogRecord;
import RTi.Util.IO.CommandPhaseType;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.CommandProcessorRequestResultsBean;
import RTi.Util.IO.CommandStatusType;
import RTi.Util.IO.CommandStatus;
import RTi.Util.IO.CommandWarningException;
import RTi.Util.IO.FileGenerator;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.InvalidCommandParameterException;
import RTi.Util.IO.ObjectListProvider;
import RTi.Util.IO.PropList;

import RTi.Util.Message.Message;
import RTi.Util.Message.MessageUtil;
import RTi.Util.String.StringUtil;
import RTi.Util.Table.DataTable;
import RTi.Util.Table.TableField;
import RTi.Util.Table.TableRecord;
import riverside.datastore.DataStore;

/**
This class initializes, checks, and runs the Bitbucket() command.
*/
public class Bitbucket_Command extends AbstractCommand
implements CommandDiscoverable, FileGenerator, ObjectListProvider
{
	/**
	Data members used for parameter values.
	*/
	protected final String _False = "False";
	protected final String _True = "True";

	/**
	Data members used for parameter values.
	*/
	protected final String _Ignore = "Ignore";
	protected final String _Warn = "Warn";
	protected final String _Fail = "Fail";

	/**
	Output file that is created by this command.
	*/
	private File __OutputFile_File = null;

	/**
	The output table that is created for discovery mode.
	*/
	private DataTable discoveryOutputTable = null;

	/**
	Constructor.
	*/
	public Bitbucket_Command () {
		super();
		setCommandName ( "Bitbucket" );
	}

	/**
	Check the command parameter for valid values, combination, etc.
	@param parameters The parameters for the command.
	@param command_tag an indicator to be used when printing messages, to allow a cross-reference to the original commands.
	@param warning_level The warning level to use when printing parse warnings
	(recommended is 2 for initialization, and 1 for interactive command editor dialogs).
	*/
	public void checkCommandParameters ( PropList parameters, String command_tag, int warning_level )
	throws InvalidCommandParameterException {
		// General (top).
		String DataStore = parameters.getValue ( "DataStore" );
		String BitbucketCommand = parameters.getValue ( "BitbucketCommand" );
		// List Regression Issues.
    	String IncludeOpenIssues = parameters.getValue ( "IncludeOpenIssues" );
    	String IncludeClosedIssues = parameters.getValue ( "IncludeClosedIssues" );
    	// Output.
    	String OutputTableID = parameters.getValue ( "OutputTableID" );
    	String OutputFile = parameters.getValue ( "OutputFile" );
    	String AppendOutput = parameters.getValue ( "AppendOutput" );
		String IfInputNotFound = parameters.getValue ( "IfInputNotFound" );
		// General (bottom).
		String Timeout = parameters.getValue ( "Timeout" );
		String warning = "";
		String message;

		CommandStatus status = getCommandStatus();
		status.clearLog(CommandPhaseType.INITIALIZATION);

		// The existence of the file to append is not checked during initialization
		// because files may be created dynamically at runtime.

		if ( (DataStore == null) || DataStore.isEmpty() ) {
        	message = "The datastore must be specified.";
			warning += "\n" + message;
        	status.addToLog ( CommandPhaseType.INITIALIZATION,
            	new CommandLogRecord(CommandStatusType.FAILURE,
                	message, "Specify the datastore." ) );
		}

		BitbucketCommandType bitbucketCommand = null;
		if ( (BitbucketCommand == null) || BitbucketCommand.isEmpty() ) {
			message = "The Bitbucket command must be specified.";
			warning += "\n" + message;
			status.addToLog(CommandPhaseType.INITIALIZATION,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "Specify the Bitbucket command."));
		}
		else {
			bitbucketCommand = BitbucketCommandType.valueOfIgnoreCase(BitbucketCommand);
			if ( bitbucketCommand == null ) {
				message = "The Bitbucket command (" + BitbucketCommand + ") is invalid.";
				warning += "\n" + message;
				status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify a valid Bitbucket command."));
			}
		}

		if ( (IncludeOpenIssues != null) && !IncludeOpenIssues.equals("") ) {
			if ( !IncludeOpenIssues.equalsIgnoreCase(_False) && !IncludeOpenIssues.equalsIgnoreCase(_True) ) {
				message = "The IncludeOpenIssues parameter \"" + IncludeOpenIssues + "\" is invalid.";
				warning += "\n" + message;
				status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the parameter as " + _False + " (default), or " + _True + "."));
			}
		}

		if ( (IncludeClosedIssues != null) && !IncludeClosedIssues.equals("") ) {
			if ( !IncludeClosedIssues.equalsIgnoreCase(_False) && !IncludeClosedIssues.equalsIgnoreCase(_True) ) {
				message = "The IncludeClosedIssues parameter \"" + IncludeClosedIssues + "\" is invalid.";
				warning += "\n" + message;
				status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the parameter as " + _False + " (default), or " + _True + "."));
			}
		}

		if ( (AppendOutput != null) && !AppendOutput.equals("") ) {
			if ( !AppendOutput.equalsIgnoreCase(_False) && !AppendOutput.equalsIgnoreCase(_True) ) {
				message = "The AppendOutput parameter \"" + AppendOutput + "\" is invalid.";
				warning += "\n" + message;
				status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the parameter as " + _False + " (default), or " + _True + "."));
			}
		}

		if ( (IfInputNotFound != null) && !IfInputNotFound.equals("") ) {
			if ( !IfInputNotFound.equalsIgnoreCase(_Ignore) && !IfInputNotFound.equalsIgnoreCase(_Warn)
		    	&& !IfInputNotFound.equalsIgnoreCase(_Fail) ) {
				message = "The IfInputNotFound parameter \"" + IfInputNotFound + "\" is invalid.";
				warning += "\n" + message;
				status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the parameter as " + _Ignore + ", " + _Warn + " (default), or " +
						_Fail + "."));
			}
		}

		// The output table or file is needed for lists:
		// - some internal logic such as counts uses the table
		if ( (bitbucketCommand == BitbucketCommandType.LIST_PROJECTS) ||
			(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES) ||
			(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES) ) {
			// Must specify table and/or file.
			if ( ((OutputTableID == null) || OutputTableID.isEmpty()) && ((OutputFile == null) || OutputFile.isEmpty()) ) {
				message = "The output table and/or file must be specified.";
				warning += "\n" + message;
				status.addToLog(CommandPhaseType.INITIALIZATION,
					new CommandLogRecord(CommandStatusType.FAILURE,
						message, "Specify the output table ID and or file name."));
			}
		}

		if ( (Timeout != null) && !Timeout.isEmpty() && !StringUtil.isInteger(Timeout) ) {
        	message = "The Timeout (" + Timeout + ") is not an integer.";
			warning += "\n" + message;
        	status.addToLog ( CommandPhaseType.INITIALIZATION,
            	new CommandLogRecord(CommandStatusType.FAILURE,
                	message, "Specify the timeout as a number of seconds." ) );
		}

		// Check for invalid parameters.
		List<String> validList = new ArrayList<>(16);
		// General (top).
		validList.add ( "DataStore" );
		validList.add ( "BitbucketCommand" );
		// List projects.
		validList.add ( "ListProjectsRegEx" );
		validList.add ( "ListProjectsCountProperty" );
		// List repositories.
		validList.add ( "ListRepositoriesRegEx" );
		validList.add ( "ListRepositoriesCountProperty" );
		// List repository issues.
		validList.add ( "Assignee" );
		validList.add ( "IncludeOpenIssues" );
		validList.add ( "IncludeClosedIssues" );
		validList.add ( "IssueProperties" );
		validList.add ( "ListRepositoryIssuesRegEx" );
		validList.add ( "ListRepositoryIssuesCountProperty" );
		// Output
		validList.add ( "OutputTableID" );
		validList.add ( "OutputFile" );
		validList.add ( "AppendOutput" );
		// General (bottom).
		validList.add ( "Timeout" );
		//
		validList.add ( "IfInputNotFound" );
		warning = TSCommandProcessorUtil.validateParameterNames ( validList, this, warning );

		if ( warning.length() > 0 ) {
			Message.printWarning ( warning_level,
			MessageUtil.formatMessageTag(command_tag,warning_level),warning );
			throw new InvalidCommandParameterException ( warning );
		}
		status.refreshPhaseSeverity(CommandPhaseType.INITIALIZATION,CommandStatusType.SUCCESS);
	}

	/**
	 * List Bitbucket projects.
	 */
	private int doListProjects (
		BitbucketDataStore dataStore,
		CommandProcessor processor,
		BitbucketSession session,
		DataTable table,
		int projectNameCol,
		int projectTypeCol,
		int projectKeyCol,
		String regEx,
		String listProjectsCountProperty,
		int timeoutSeconds,
		CommandStatus status, int logLevel, int warningCount, String commandTag ) throws Exception {
		String routine = getClass().getSimpleName() + ".doListProjects";
		String message;

		// Read the projects.
		List<Project> projects = dataStore.readProjects ( session, timeoutSeconds );

    	TableRecord rec = null;
    	boolean allowDuplicates = false;

    	boolean doRegEx = false;
    	if ( (regEx != null) && !regEx.isEmpty() ) {
    		// Check whether the project names match the regular expression.
    		doRegEx = true;
    	}

		// Output to table.
		for ( Project project : projects ) {
			String projectName = project.getName();
			if ( doRegEx ) {
				if ( !projectName.matches(regEx) ) {
					continue;
				}
			}
			if ( table != null ) {
    			if ( !allowDuplicates ) {
    				// Try to match the repository name, which is the unique identifier.
    				rec = table.getRecord ( projectNameCol, projectName );
    			}
    			if ( rec == null ) {
    				// Create a new record.
    				rec = table.addRecord(table.emptyRecord());
    			}
    			// Set the data in the record.
    			rec.setFieldValue(projectNameCol,projectName);
    			rec.setFieldValue(projectTypeCol,project.getType());
    			rec.setFieldValue(projectKeyCol,project.getKey());
    		}
    	}
    	// Set the property indicating the number of projects.
        if ( (listProjectsCountProperty != null) && !listProjectsCountProperty.equals("") ) {
          	int projectCount = 0;
          	if ( table != null ) {
          		projectCount = table.getNumberOfRecords();
          	}
           	PropList requestParams = new PropList ( "" );
           	requestParams.setUsingObject ( "PropertyName", listProjectsCountProperty );
           	requestParams.setUsingObject ( "PropertyValue", Integer.valueOf(projectCount) );
           	try {
               	processor.processRequest( "SetProperty", requestParams);
           	}
           	catch ( Exception e ) {
               	message = "Error requesting SetProperty(Property=\"" + listProjectsCountProperty + "\") from processor.";
               	Message.printWarning(logLevel,
                   	MessageUtil.formatMessageTag( commandTag, ++warningCount),
                   	routine, message );
               	status.addToLog ( CommandPhaseType.RUN,
                   	new CommandLogRecord(CommandStatusType.FAILURE,
                       	message, "Report the problem to software support." ) );
           	}
        }

        // Return the updated warning count.
        return warningCount;
	}

	/**
	 * List Bitbucket repositories.
	 */
	private int doListRepositories (
		BitbucketDataStore dataStore,
		CommandProcessor processor,
		BitbucketSession session,
		List<Repository> repositoryList,
		DataTable table,
		int repositoryNameCol,
		int repositorySlugCol,
		int repositoryCreatedOnCol,
		int repositoryUpdatedOnCol,
    	int repositoryHasIssuesCol,
		int repositoryIsPrivateCol,
		int repositorySizeCol,
		int repositoryDescriptionCol,
		String regEx, String listRepositoriesCountProperty,
		int timeoutSeconds,
		CommandStatus status, int logLevel, int warningCount, String commandTag ) throws Exception {
		String routine = getClass().getSimpleName() + ".doListRepositories";
		String message;

		// Read the repositories.
		List<Repository> repositories = dataStore.readRepositories ( session, timeoutSeconds );

    	TableRecord rec = null;
    	boolean allowDuplicates = false;

    	boolean doRegEx = false;
    	if ( (regEx != null) && !regEx.isEmpty() ) {
    		// Check whether the repository names match the regular expression.
    		doRegEx = true;
    	}

		for ( Repository repository : repositories ) {
			String repositoryName = repository.getName();
			if ( doRegEx ) {
				if ( !repositoryName.matches(regEx) ) {
					continue;
				}
			}
			// Add the repository to the output list.
			if ( repositoryList != null ) {
		 		repositoryList.add(repository);
	 		}
			// Output to table.
			if ( table != null ) {
    			if ( !allowDuplicates ) {
    				// Try to match the repository name, which is the unique identifier.
    				rec = table.getRecord ( repositoryNameCol, repositoryName );
    			}
    			if ( rec == null ) {
    				// Create a new record.
    				rec = table.addRecord(table.emptyRecord());
    			}
    			// Set the data in the record.
    			rec.setFieldValue(repositoryNameCol,repositoryName);
    			rec.setFieldValue(repositorySlugCol,repository.getSlug());
    			rec.setFieldValue(repositoryCreatedOnCol,repository.getCreatedOnAsDateTime());
    			rec.setFieldValue(repositoryUpdatedOnCol,repository.getUpdatedOnAsDateTime());
    			rec.setFieldValue(repositoryHasIssuesCol,repository.getHasIssues());
    			rec.setFieldValue(repositoryIsPrivateCol,repository.getIsPrivate());
    			rec.setFieldValue(repositorySizeCol,repository.getSize());
    			rec.setFieldValue(repositoryDescriptionCol,repository.getDescription());
    		}
    	}
    	// Set the property indicating the number of repositories.
        if ( (listRepositoriesCountProperty != null) && !listRepositoriesCountProperty.equals("") ) {
          	int repositoryCount = 0;
          	if ( table != null ) {
          		repositoryCount = table.getNumberOfRecords();
          	}
           	PropList requestParams = new PropList ( "" );
           	requestParams.setUsingObject ( "PropertyName", listRepositoriesCountProperty );
           	requestParams.setUsingObject ( "PropertyValue", Integer.valueOf(repositoryCount) );
           	try {
               	processor.processRequest( "SetProperty", requestParams);
           	}
           	catch ( Exception e ) {
               	message = "Error requesting SetProperty(Property=\"" + listRepositoriesCountProperty + "\") from processor.";
               	Message.printWarning(logLevel,
                   	MessageUtil.formatMessageTag( commandTag, ++warningCount),
                   	routine, message );
               	status.addToLog ( CommandPhaseType.RUN,
                   	new CommandLogRecord(CommandStatusType.FAILURE,
                       	message, "Report the problem to software support." ) );
           	}
        }

        // Return the updated warning count.
        return warningCount;
	}

	/**
	 * List Bitbucket repository issues.
	 */
	private int doListRepositoryIssues (
		BitbucketDataStore dataStore,
		CommandProcessor processor,
		BitbucketSession session,
		List<Repository> repositoryList,
		DataTable table,
    	int issueRepositoryNameCol,
    	String [] issueProperties, int [] issuePropertiesCol,
    	int issueIdCol,
    	int issueLinkCol,
    	int issueTitleCol,
    	//int issueNameCol,
    	//int issueTypeCol,
    	int issuePriorityCol,
    	int issueKindCol,
    	int issueStateCol,
    	int issueAssigneeCol,
    	int issueReporterCol,
    	int issueAgeDaysCol,
    	int issueCreatedOnCol,
    	int issueUpdatedOnCol,
    	int issueEditedOnCol,
    	String assigneeToMatch,
    	boolean includeOpenIssues, boolean includeClosedIssues,
		String regEx, String listRepositoryIssuesCountProperty,
		int timeoutSeconds,
		CommandStatus status, int logLevel, int warningCount, String commandTag ) throws Exception {
		String routine = getClass().getSimpleName() + ".doListRepositoryIssues";
		String message;

    	TableRecord rec = null;
    	boolean allowDuplicates = false;

    	boolean doRegEx = false;
    	if ( (regEx != null) && !regEx.isEmpty() ) {
    		// Check whether the repository names match the regular expression.
    		doRegEx = true;
    	}
    	boolean doAssignee = false;
    	if ( (assigneeToMatch != null) && !assigneeToMatch.isEmpty() ) {
    		// Match an assignee.
    		doAssignee = true;
    	}

    	// Loop through the repositories and read the issues for each.

   		Message.printStatus(2, routine, "Reading issues for " + repositoryList.size() + " repositories.");
   		List<Issue> issues = new ArrayList<>();
    	for ( Repository repository : repositoryList ) {
    		// Read the repository issues:
    		// - for now don't use API query parameters to filter
    		Message.printStatus(2, routine, "Reading issues for repository \"" + repository.getName() + "\"");
    		List<Issue> issues0 = dataStore.readRepositoryIssues ( session, repository, timeoutSeconds );

    		// Filter the returned issues:
    		// - only include matched issues in the output table
    		
	  		for ( Issue issue : issues0 ) {
		  		String issueTitle = issue.getTitle();
		  		if ( doRegEx ) {
			  		if ( !issueTitle.matches(regEx) ) {
				  		continue;
			  		}
		  		}
		  		if ( doAssignee ) {
		  			User assignee = issue.getAssignee();
		  			if ( assignee == null ) {
		  				// No match.
		  				continue;
		  			}
		  			if ( !assigneeToMatch.equals(issue.getAssignee().getDisplayName()) ) {
		  				// No match.
		  				continue;
		  			}
		  		}
		  		if ( includeOpenIssues ) {
		  			// The state must be 'new' or 'open'.
		  			if ( issue.isOpenIssue() ) {
		  				// OK to include.
		  			}
		  			else {
		  				// Not OK to include.
		  				continue;
		  			}
		  		}
		  		if ( includeClosedIssues ) {
		  			// The state must be other than 'new' or 'open'.
		  			if ( !issue.isOpenIssue() ) {
		  				// OK to include.
		  			}
		  			else {
		  				// Not OK to include.
		  				continue;
		  			}
		  		}
		  		
		  		// Remaining issues are added to the combined list.
		  		issues.add(issue);
	  		}
    	}

		// Sort on the assignee, repository, priority, and age.
		Collections.sort(issues, new IssueComparator());
    	
    	// The remaining issues are added to the output table.

		if ( table != null ) {
  			// Output to table.
			for ( Issue issue : issues ) {
				String issueTitle = issue.getTitle();
		  		if ( !allowDuplicates ) {
			  		// Try to match the repository name, which is the unique identifier.
			  		rec = table.getRecord ( issueTitleCol, issueTitle );
		  		}
		  		if ( rec == null ) {
			  		// Create a new record.
			  		rec = table.addRecord(table.emptyRecord());
		  		}
		  		// Set the data in the record.
		  		rec.setFieldValue(issueRepositoryNameCol,issue.getRepositoryObject().getName());
		  		for ( int i = 0; i < issueProperties.length; i++ ) {
		  			String propValue = issue.getProperty(issueProperties[i]);
		  			// OK to set as null.
		  			rec.setFieldValue(issuePropertiesCol[i],propValue);
		  		}
		  		rec.setFieldValue(issueIdCol,issue.getId());
		  		IssueLinks issueLinks = issue.getIssueLinks();
		  		if ( issueLinks != null ) {
		  			Link html = issueLinks.getHtml();
		  			if ( html != null ) {
		  				rec.setFieldValue(issueLinkCol,html.getHref());
		  			}
		  		}
		  		rec.setFieldValue(issueTitleCol,issueTitle);
		  		//rec.setFieldValue(issueNameCol,issue.getName());
		  		//rec.setFieldValue(issueTypeCol,issue.getType());
		  		rec.setFieldValue(issuePriorityCol,issue.getPriority());
		  		rec.setFieldValue(issueKindCol,issue.getKind());
		  		rec.setFieldValue(issueStateCol,issue.getState());
		  		User assignee = issue.getAssignee();
		  		if ( assignee != null ) {
		  			rec.setFieldValue(issueAssigneeCol,assignee.getDisplayName());
		  		}
		  		User reporter = issue.getReporter();
		  		if ( reporter != null ) {
		  			rec.setFieldValue(issueReporterCol,reporter.getDisplayName());
		  		}
		  		rec.setFieldValue(issueAgeDaysCol,issue.getAgeDays());
		  		rec.setFieldValue(issueCreatedOnCol,issue.getCreatedOnAsDateTime());
		  		rec.setFieldValue(issueUpdatedOnCol,issue.getUpdatedOnAsDateTime());
		  		rec.setFieldValue(issueEditedOnCol,issue.getEditedOnAsDateTime());
			}
  		}

  		// Set the processor property indicating the number of issues.

  		if ( (listRepositoryIssuesCountProperty != null) && !listRepositoryIssuesCountProperty.equals("") ) {
  	  		int repositoryIssuesCount = 0;
  	  		if ( table != null ) {
  		  		repositoryIssuesCount = table.getNumberOfRecords();
  	  		}
   	  		PropList requestParams = new PropList ( "" );
   	  		requestParams.setUsingObject ( "PropertyName", listRepositoryIssuesCountProperty );
   	  		requestParams.setUsingObject ( "PropertyValue", Integer.valueOf(repositoryIssuesCount) );
   	  		try {
       	  		processor.processRequest( "SetProperty", requestParams);
   	  		}
   	  		catch ( Exception e ) {
       	  		message = "Error processing processor request SetProperty(Property=\"" + listRepositoryIssuesCountProperty + "\").";
       	  		Message.printWarning(logLevel,
           	  		MessageUtil.formatMessageTag( commandTag, ++warningCount),
               	  		routine, message );
           	  		status.addToLog ( CommandPhaseType.RUN,
               	  		new CommandLogRecord(CommandStatusType.FAILURE,
                   	  		message, "Report the problem to software support." ) );
      		}
		}
    	
        // Return the updated warning count.
        return warningCount;
	}

	/**
	Edit the command.
	@param parent The parent JFrame to which the command dialog will belong.
	@return true if the command was edited (e.g., "OK" was pressed), and false if not (e.g., "Cancel" was pressed).
	*/
	public boolean editCommand ( JFrame parent ) {
		// The command will be modified if changed.
    	List<String> tableIDChoices =
        	TSCommandProcessorUtil.getTableIdentifiersFromCommandsBeforeCommand(
            	(TSCommandProcessor)getCommandProcessor(), this);
		return (new Bitbucket_JDialog ( parent, this, tableIDChoices )).ok();
	}

	/**
	Return the table that is read by this class when run in discovery mode.
	*/
	private DataTable getDiscoveryTable() {
    	return this.discoveryOutputTable;
	}

	/**
	Return the list of files that were created by this command.
	*/
	public List<File> getGeneratedFileList () {
    	List<File> list = new ArrayList<>();
    	if ( getOutputFile() != null ) {
        	list.add ( getOutputFile() );
    	}
    	return list;
	}

	/**
	Return a list of objects of the requested type.  This class only keeps a list of DataTable objects.
	The following classes can be requested:  DataTable
	*/
	@SuppressWarnings("unchecked")
	public <T> List<T> getObjectList ( Class<T> c ) {
   	DataTable table = getDiscoveryTable();
    	List<T> v = null;
    	if ( (table != null) && (c == table.getClass()) ) {
        	v = new ArrayList<>();
        	v.add ( (T)table );
    	}
    	return v;
	}

	/**
	Return the output file generated by this command.  This method is used internally.
	@return the output file generated by this command
	*/
	private File getOutputFile () {
    	return __OutputFile_File;
	}

	/**
	Run the command.
	@param command_number Command number in sequence.
	@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
	@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
	*/
	public void runCommand ( int command_number )
	throws InvalidCommandParameterException, CommandWarningException, CommandException {
    	runCommandInternal ( command_number, CommandPhaseType.RUN );
	}

	/**
	Run the command in discovery mode.
	@param command_number Command number in sequence.
	@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
	@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
	*/
	public void runCommandDiscovery ( int command_number )
	throws InvalidCommandParameterException, CommandWarningException, CommandException {
    	runCommandInternal ( command_number, CommandPhaseType.DISCOVERY );
	}

	/**
	Run the command.
	@param commandNumber Number of command in sequence (1+).
	@exception CommandWarningException Thrown if non-fatal warnings occur (the command could produce some results).
	@exception CommandException Thrown if fatal warnings occur (the command could not produce output).
	@exception InvalidCommandParameterException Thrown if parameter one or more parameter values are invalid.
	*/
	private void runCommandInternal ( int commandNumber, CommandPhaseType commandPhase )
	throws InvalidCommandParameterException, CommandWarningException, CommandException {
		String routine = getClass().getSimpleName() + ".runCommand", message;
		int warningLevel = 2;
		int logLevel = 3; // Level for non-user messages for log file.
		String commandTag = "" + commandNumber;
		int warningCount = 0;

		PropList parameters = getCommandParameters();

    	CommandProcessor processor = getCommandProcessor();
		CommandStatus status = getCommandStatus();
    	Boolean clearStatus = Boolean.TRUE; // Default.
    	try {
    		Object o = processor.getPropContents("CommandsShouldClearRunStatus");
    		if ( o != null ) {
    			clearStatus = (Boolean)o;
    		}
    	}
    	catch ( Exception e ) {
    		// Should not happen.
    	}
    	if ( clearStatus ) {
			status.clearLog(commandPhase);
		}

    	// Clear the output file.
    	setOutputFile ( null );

		String BitbucketCommand = parameters.getValue ( "BitbucketCommand" );
		BitbucketCommandType bitbucketCommand = BitbucketCommandType.valueOfIgnoreCase(BitbucketCommand);

    	// List projects.
		String ListProjectsRegEx = parameters.getValue ( "ListProjectsRegEx" );
		// TODO smalers 2023-01-27 evaluate whether regex can be expanded or will have conflicts.
		//ListProjectsRegEx = TSCommandProcessorUtil.expandParameterValue(processor,this,ListProjectsRegEx);
		// Convert the RegEx to Java style.
		String listProjectsRegEx = null;
		if ( (ListProjectsRegEx != null) && !ListProjectsRegEx.isEmpty() ) {
			if ( ListProjectsRegEx.toUpperCase().startsWith("JAVA:") ) {
				// Use as is for a Java regular expression.
				listProjectsRegEx = ListProjectsRegEx.substring(5);
			}
			else {
				// Default to glob so convert to Java regex.
				// TODO smalers 2023-02-01 need to hanle [abc] and [a-z].
				listProjectsRegEx = ListProjectsRegEx.replace(".", "\\.").replace("*", ".*");
			}
		}
    	String ListProjectsCountProperty = parameters.getValue ( "ListProjectsCountProperty" );
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		ListProjectsCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, ListProjectsCountProperty);
    	}

    	// List repositories.
		String ListRepositoriesRegEx = parameters.getValue ( "ListRepositoriesRegEx" );
		// TODO smalers 2023-01-27 evaluate whether regex can be expanded or will have conflicts.
		//ListRepositoriesRegEx = TSCommandProcessorUtil.expandParameterValue(processor,this,ListRepositoriesRegEx);
		// Convert the RegEx to Java style.
		String listRepositoriesRegEx = null;
		if ( (ListRepositoriesRegEx != null) && !ListRepositoriesRegEx.isEmpty() ) {
			if ( ListRepositoriesRegEx.toUpperCase().startsWith("JAVA:") ) {
				// Use as is for a Java regular expression.
				listRepositoriesRegEx = ListRepositoriesRegEx.substring(5);
			}
			else {
				// Default to glob so convert to Java regex.
				// TODO smalers 2023-02-01 need to hanle [abc] and [a-z].
				listRepositoriesRegEx = ListRepositoriesRegEx.replace(".", "\\.").replace("*", ".*");
			}
		}
    	String ListRepositoriesCountProperty = parameters.getValue ( "ListRepositoriesCountProperty" );
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		ListRepositoriesCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, ListRepositoriesCountProperty);
    	}

    	// List repository issues.

		String Assignee = parameters.getValue ( "Assignee" );
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		Assignee = TSCommandProcessorUtil.expandParameterValue(processor, this, Assignee);
    	}
		String IncludeOpenIssues = parameters.getValue ( "IncludeOpenIssues" );
		boolean includeOpenIssues = true;
		if ( (IncludeOpenIssues != null) && IncludeOpenIssues.equalsIgnoreCase(_False)) {
			includeOpenIssues = false;
		}

		String IncludeClosedIssues = parameters.getValue ( "IncludeClosedIssues" );
		boolean includeClosedIssues = false;
		if ( (IncludeClosedIssues != null) && IncludeClosedIssues.equalsIgnoreCase(_True)) {
			includeClosedIssues = true;
		}

		String IssueProperties = parameters.getValue ( "IssueProperties" );
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		IssueProperties = TSCommandProcessorUtil.expandParameterValue(processor, this, IssueProperties);
    	}
		String [] issueProperties = new String[0];
		if ( (IssueProperties != null) && !IssueProperties.isEmpty() ) {
			if ( IssueProperties.contains(",") ) {
				issueProperties = IssueProperties.split(",");
				for ( int i = 0; i < issueProperties.length; i++ ) {
					issueProperties[i] = issueProperties[i].trim();
				}
			}
			else {
				// Single value.
				issueProperties = new String[1];
				issueProperties[0] = IssueProperties.trim();
			}
		}

		String ListRepositoryIssuesRegEx = parameters.getValue ( "ListRepositoryIssuesRegEx" );
		// TODO smalers 2023-01-27 evaluate whether regex can be expanded or will have conflicts.
		//ListRepositoryIssuesRegEx = TSCommandProcessorUtil.expandParameterValue(processor,this,ListRepositoryIssuesRegEx);
		// Convert the RegEx to Java style.
		String listRepositoryIssuesRegEx = null;
		if ( (ListRepositoryIssuesRegEx != null) && !ListRepositoryIssuesRegEx.isEmpty() ) {
			if ( ListRepositoryIssuesRegEx.toUpperCase().startsWith("JAVA:") ) {
				// Use as is for a Java regular expression.
				listRepositoryIssuesRegEx = ListRepositoryIssuesRegEx.substring(5);
			}
			else {
				// Default to glob so convert to Java regex.
				// TODO smalers 2023-02-01 need to hanle [abc] and [a-z].
				listRepositoryIssuesRegEx = ListRepositoryIssuesRegEx.replace(".", "\\.").replace("*", ".*");
			}
		}
    	String ListRepositoryIssuesCountProperty = parameters.getValue ( "ListRepositoryIssuesCountProperty" );
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		ListRepositoryIssuesCountProperty = TSCommandProcessorUtil.expandParameterValue(processor, this, ListRepositoryIssuesCountProperty);
    	}

    	// Output.
		boolean doTable = false;
		String OutputTableID = parameters.getValue ( "OutputTableID" );
		if ( commandPhase == CommandPhaseType.RUN ) {
			OutputTableID = TSCommandProcessorUtil.expandParameterValue(processor,this,OutputTableID);
		}
		if ( (OutputTableID != null) && !OutputTableID.isEmpty() ) {
			doTable = true;
		}
		// If an output file is to be written:
		// - output using the table, if available
		// - if an output table is not being created, create a temporary table and write it
		boolean doOutputFile = false;
		String OutputFile = parameters.getValue ( "OutputFile" ); // Expand below.
		if ( (OutputFile != null) && !OutputFile.isEmpty() ) {
			doOutputFile = true;
		}
		String IfInputNotFound = parameters.getValue ( "IfInputNotFound" );
		if ( (IfInputNotFound == null) || IfInputNotFound.equals("")) {
	    	IfInputNotFound = _Warn; // Default
		}
		String AppendOutput = parameters.getValue ( "AppendOutput" );
		boolean appendOutput = false;
		if ( (AppendOutput != null) && AppendOutput.equalsIgnoreCase(_True)) {
			appendOutput = true;
		}

		// General (bottom).
		String Timeout = parameters.getValue ("Timeout" );
		int timeoutSeconds = 5*60; // Default = 5 minutes.
		if ( commandPhase == CommandPhaseType.RUN ) {
	    	Timeout = TSCommandProcessorUtil.expandParameterValue(processor, this, Timeout);
		}
		if ( (Timeout != null) && !Timeout.isEmpty() ) {
			timeoutSeconds = Integer.valueOf(timeoutSeconds);
		}

		// Get the table to process:
		// - only if appending
		// - if not appending, (re)create below

		DataTable table = null;
    	if ( commandPhase == CommandPhaseType.RUN ) {
    		PropList requestParams = null;
			CommandProcessorRequestResultsBean bean = null;
		  	if ( (OutputTableID != null) && !OutputTableID.isEmpty() && appendOutput ) {
				// Get the table to be updated.
				requestParams = new PropList ( "" );
				requestParams.set ( "TableID", OutputTableID );
				try {
					bean = processor.processRequest( "GetTable", requestParams);
			 		PropList bean_PropList = bean.getResultsPropList();
			  		Object o_Table = bean_PropList.getContents ( "Table" );
			  		if ( o_Table != null ) {
				  		// Found the table so no need to create it below.
				  		table = (DataTable)o_Table;
				  		Message.printStatus(2, routine, "Found existing table for append.");
			  		}
				}
				catch ( Exception e ) {
			 		message = "Error requesting GetTable(TableID=\"" + OutputTableID + "\") from processor (" + e + ").";
			  		Message.printWarning(warningLevel,
				  		MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
			  		status.addToLog ( commandPhase, new CommandLogRecord(CommandStatusType.FAILURE,
				  		message, "Report problem to software support." ) );
				}
		  	}
    	}

		if ( warningCount > 0 ) {
			message = "There were " + warningCount + " warnings about command parameters - need to fix to run the command.";
			Message.printWarning ( warningLevel,
			MessageUtil.formatMessageTag(commandTag, ++warningCount), routine, message );
			throw new InvalidCommandParameterException ( message );
		}

		// Handle credentials.

		// Get the datastore here because it is needed to create the table.

    	String DataStore = parameters.getValue ( "DataStore" );
    	BitbucketDataStore dataStore = null;
		if ( (DataStore != null) && !DataStore.equals("") ) {
	    	// User has indicated that a datastore should be used.
	    	DataStore dataStore0 = ((TSCommandProcessor)getCommandProcessor()).getDataStoreForName( DataStore, BitbucketDataStore.class );
        	if ( dataStore0 != null ) {
            	Message.printStatus(2, routine, "Selected datastore is \"" + dataStore0.getName() + "\"." );
				dataStore = (BitbucketDataStore)dataStore0;
        	}
    	}
		if ( dataStore == null ) {
           	message = "Cannot get BitbucketDataStore for \"" + DataStore + "\".";
           	Message.printWarning ( 2, routine, message );
           	status.addToLog ( commandPhase,
               	new CommandLogRecord(CommandStatusType.FAILURE,
                   	message, "Verify that a BitbucketDataStore datastore is properly configured." ) );
           	throw new RuntimeException ( message );
    	}

		// Create a session using the datastore properties.
		String problem = "";
		String workspaceId = dataStore.getProperty("WorkspaceId");
		if ( (workspaceId == null) || workspaceId.isEmpty() ) {
			problem = "WorkspaceId is not set for the datastore.";
		}
		String userName = dataStore.getProperty("UserName");
		if ( (workspaceId == null) || workspaceId.isEmpty() ) {
			if ( problem.length() > 0 ) {
				problem += "  ";
			}
			problem += "UserName is not set for the datastore.";
		}
		String appPassword = dataStore.getProperty("AppPassword");
		if ( (appPassword == null) || appPassword.isEmpty() ) {
			if ( problem.length() > 0 ) {
				problem += "  ";
			}
			problem += "AppPassword is not set for the datastore.";
		}
		BitbucketSession session = null;
		session = new BitbucketSession ( workspaceId, userName, appPassword );

		if ( !problem.isEmpty() ) {
           	message = "Error accessing Bitbucket datastore \"" + DataStore + "\": " + problem;
           	Message.printWarning ( 2, routine, message );
           	status.addToLog ( commandPhase,
               	new CommandLogRecord(CommandStatusType.FAILURE,
                   	message, "Verify that the BitbucketDataStore datastore is properly configured." ) );
           	throw new RuntimeException ( message );
		}

		try {
	    	if ( commandPhase == CommandPhaseType.RUN ) {

	    		// Create a session with the credentials.
	    		//BitbucketSession bitbucketSession = new BitbucketSession();

    			// Column numbers are used later.

	    		// Project list columns:
	    		// - order of columns
        		int projectNameCol = -1;
        		int projectTypeCol = -1;
        		int projectKeyCol = -1;

	    		// Repository list columns:
	    		// - order of columns
        		int repositoryNameCol = -1;
        		int repositorySlugCol = -1;
        		int repositoryCreatedOnCol = -1;
        		int repositoryUpdatedOnCol = -1;
        		int repositoryHasIssuesCol = -1;
        		int repositoryIsPrivateCol = -1;
        		int repositorySizeCol = -1;
        		int repositoryDescriptionCol = -1;

        		// Repository issue list columns:
	    		// - order of columns
        		int issueRepositoryNameCol = -1;
        		int [] issuePropertiesCol = new int[issueProperties.length];
        		int issueIdCol = -1;
        		int issueLinkCol = -1;
        		int issueTitleCol = -1;
        		//int issueNameCol = -1;
        		//int issueTypeCol = -1;
        		int issuePriorityCol = -1;
        		int issueKindCol = -1;
        		int issueStateCol = -1;
        		int issueAssigneeCol = -1;
        		int issueReporterCol = -1;
        		int issueAgeDaysCol = -1;
        		int issueCreatedOnCol = -1;
        		int issueUpdatedOnCol = -1;
        		int issueEditedOnCol = -1;

	    		if ( doTable || doOutputFile) {
	    			// Requested a table and/or file:
	    			// - if only file is request, create a temporary table that is then written to output
    	    		if ( (table == null) || !appendOutput ) {
    	        		// The table needs to be created because it does not exist or NOT appending (so need new table):
    	    			// - the table columns depend on the Bitbucket command being executed
    	    			// 1. Define the column names based on Bitbucket commands.
    	        		List<TableField> columnList = new ArrayList<>();
    	        		if ( bitbucketCommand == BitbucketCommandType.LIST_PROJECTS ) {
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Name", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Type", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Key", -1) );
    	        		}
    	        		else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES ) {
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Name", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Slug", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_DATETIME, "CreatedOn", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_DATETIME, "UpdatedOn", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_BOOLEAN, "HasIssues", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_BOOLEAN, "IsPrivate", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_INT, "Size", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Description", -1) );
    	        		}
    	        		else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES ) {
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "RepositoryName", -1) );
    	        			for ( int i = 0; i < issueProperties.length; i++ ) {
    	        				columnList.add ( new TableField(TableField.DATA_TYPE_STRING, issueProperties[i], -1) );
    	        			}
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_INT, "Id", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Link", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Title", -1) );
    	        			//columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Name", -1) );
    	        			//columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Type", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Priority", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Kind", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "State", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Assignee", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_STRING, "Reporter", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_INT, "AgeDays", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_DATETIME, "CreatedOn", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_DATETIME, "UpdatedOn", -1) );
    	        			columnList.add ( new TableField(TableField.DATA_TYPE_DATETIME, "EditedOn", -1) );
    	        		}
    	        		// 2. Create the table if not found from the processor above.
    	        		if ( (bitbucketCommand == BitbucketCommandType.LIST_PROJECTS) ||
    	        			(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES) ||
    	        			(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES) ) {
    	        			// Create the table.
    	        			table = new DataTable( columnList );
    	        		}
                		// 3. Get the column numbers from the names for later use.
    	        		if ( bitbucketCommand == BitbucketCommandType.LIST_PROJECTS ) {
    	        			projectNameCol = table.getFieldIndex("Name");
    	        			projectTypeCol = table.getFieldIndex("Type");
    	        			projectKeyCol = table.getFieldIndex("Key");
    	        		}
    	        		else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES ) {
    	        			repositoryNameCol = table.getFieldIndex("Name");
    	        			repositorySlugCol = table.getFieldIndex("Slug");
    	        			repositoryCreatedOnCol = table.getFieldIndex("CreatedOn");
    	        			repositoryUpdatedOnCol = table.getFieldIndex("UpdatedOn");
    	        			repositoryHasIssuesCol = table.getFieldIndex("HasIssues");
    	        			repositoryIsPrivateCol = table.getFieldIndex("IsPrivate");
    	        			repositorySizeCol = table.getFieldIndex("Size");
    	        			repositoryDescriptionCol = table.getFieldIndex("Description");
    	        		}
    	        		else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES ) {
    	        			issueRepositoryNameCol = table.getFieldIndex("RepositoryName");
    	        			for ( int i = 0; i < issueProperties.length; i++ ) {
    	        				issuePropertiesCol[i] = table.getFieldIndex(issueProperties[i]);
    	        			}
    	        			issueIdCol = table.getFieldIndex("Id");
    	        			issueLinkCol = table.getFieldIndex("Link");
    	        			issueTitleCol = table.getFieldIndex("Title");
    	        			//issueNameCol = table.getFieldIndex("Name");
    	        			//issueTypeCol = table.getFieldIndex("Type");
    	        			issuePriorityCol = table.getFieldIndex("Priority");
    	        			issueKindCol = table.getFieldIndex("Kind");
    	        			issueStateCol = table.getFieldIndex("State");
    	        			issueAssigneeCol = table.getFieldIndex("Assignee");
    	        			issueReporterCol = table.getFieldIndex("Reporter");
    	        			issueAgeDaysCol = table.getFieldIndex("AgeDays");
    	        			issueCreatedOnCol = table.getFieldIndex("CreatedOn");
    	        			issueUpdatedOnCol = table.getFieldIndex("UpdatedOn");
    	        			issueEditedOnCol = table.getFieldIndex("EditedOn");
    	        		}
    	        		// 4. Set the table in the processor:
    	        		//    - if new will add
    	        		//    - if append will overwrite by replacing the matching table ID
    	        		if ( (bitbucketCommand == BitbucketCommandType.LIST_PROJECTS) ||
    	        			(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES) ||
    	        			(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES) ) {
    	        			if ( (OutputTableID != null) && !OutputTableID.isEmpty() ) {
    	        				table.setTableID ( OutputTableID );
                				Message.printStatus(2, routine, "Created new table \"" + OutputTableID + "\" for output.");
                				// Set the table in the processor:
                				// - do not set if a temporary table is being used for the output file
                				PropList requestParams = new PropList ( "" );
                				requestParams.setUsingObject ( "Table", table );
                				try {
                    				processor.processRequest( "SetTable", requestParams);
                				}
                				catch ( Exception e ) {
                    				message = "Error requesting SetTable(Table=...) from processor.";
                    				Message.printWarning(warningLevel,
                        				MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
                    				status.addToLog ( commandPhase,
                        				new CommandLogRecord(CommandStatusType.FAILURE,
                           				message, "Report problem to software support." ) );
                				}
    	        			}
    	        			else {
    	        				// Temporary table used for file only:
    	        				// - do not set in the processor
    	        				table.setTableID ( "Bitbucket" );
    	        			}
    	        		}
    	        		// 5. The table contents will be filled in when the doBitbucket* methods are called.
    	    		}
    	    		else {
    	    			// Table exists:
    	        		// - make sure that the needed columns exist and otherwise add them
    	        		if ( bitbucketCommand == BitbucketCommandType.LIST_PROJECTS ) {
    	        			projectNameCol = table.getFieldIndex("Name");
    	        			projectTypeCol = table.getFieldIndex("Type");
    	        			projectKeyCol = table.getFieldIndex("Key");
    	        			if ( projectNameCol < 0 ) {
    	            			projectNameCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Name", -1), "");
    	        			}
    	        			if ( projectTypeCol < 0 ) {
    	            			projectTypeCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Type", -1), "");
    	        			}
    	        			if ( projectKeyCol < 0 ) {
    	            			projectKeyCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Key", -1), "");
    	        			}
    	        		}
    	        		else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES ) {
    	        			repositoryNameCol = table.getFieldIndex("Name");
    	        			if ( repositoryNameCol < 0 ) {
    	            			repositoryNameCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Name", -1), "");
    	        			}
    	        			repositorySlugCol = table.getFieldIndex("Slug");
    	        			if ( repositorySlugCol < 0 ) {
    	            			repositorySlugCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Slug", -1), "");
    	        			}
    	        			repositoryCreatedOnCol = table.getFieldIndex("CreatedOn");
    	        			if ( repositoryCreatedOnCol < 0 ) {
    	            			repositoryCreatedOnCol = table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "CreatedOn", -1), "");
    	        			}
    	        			repositoryUpdatedOnCol = table.getFieldIndex("UpdatedOn");
    	        			if ( repositoryUpdatedOnCol < 0 ) {
    	            			repositoryUpdatedOnCol = table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "UpdatedOn", -1), "");
    	        			}
    	        			repositoryHasIssuesCol = table.getFieldIndex("HasIssues");
    	        			if ( repositoryHasIssuesCol < 0 ) {
    	            			repositoryHasIssuesCol = table.addField(new TableField(TableField.DATA_TYPE_BOOLEAN, "HasIssues", -1), null);
    	        			}
    	        			repositoryIsPrivateCol = table.getFieldIndex("IsPrivate");
    	        			if ( repositoryIsPrivateCol < 0 ) {
    	            			repositoryIsPrivateCol = table.addField(new TableField(TableField.DATA_TYPE_BOOLEAN, "IsPrivate", -1), null);
    	        			}
    	        			repositorySizeCol = table.getFieldIndex("Size");
    	        			if ( repositorySizeCol < 0 ) {
    	            			repositorySizeCol = table.addField(new TableField(TableField.DATA_TYPE_INT, "Size", -1), null);
    	        			}
    	        			repositoryDescriptionCol = table.getFieldIndex("Description");
    	        			if ( repositoryDescriptionCol < 0 ) {
    	            			repositoryDescriptionCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Description", -1), "");
    	        			}
    	        		}
    	        		else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES ) {
    	        			issueRepositoryNameCol = table.getFieldIndex("RepositoryName");
    	        			for ( int i = 0; i < issueProperties.length; i++ ) {
    	        				issuePropertiesCol[i] = table.getFieldIndex(issueProperties[i]);
    	        			}
    	        			issueIdCol = table.getFieldIndex("Id");
    	        			issueLinkCol = table.getFieldIndex("Link");
    	        			issueTitleCol = table.getFieldIndex("Title");
    	        			//issueNameCol = table.getFieldIndex("Name");
    	        			//issueTypeCol = table.getFieldIndex("Type");
    	        			issuePriorityCol = table.getFieldIndex("Priority");
    	        			issueKindCol = table.getFieldIndex("Kind");
    	        			issueStateCol = table.getFieldIndex("State");
    	        			issueAssigneeCol = table.getFieldIndex("Assignee");
    	        			issueReporterCol = table.getFieldIndex("Reporter");
    	        			issueAgeDaysCol = table.getFieldIndex("AgeDays");
    	        			issueCreatedOnCol = table.getFieldIndex("CreatedOn");
    	        			issueUpdatedOnCol = table.getFieldIndex("UpdatedOn");
    	        			issueEditedOnCol = table.getFieldIndex("EditedOn");
    	        			if ( issueRepositoryNameCol < 0 ) {
    	            			issueRepositoryNameCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "RepositoryName", -1), "");
    	        			}
    	        			if ( issueIdCol < 0 ) {
    	            			issueIdCol = table.addField(new TableField(TableField.DATA_TYPE_INT, "Id", -1), "");
    	        			}
    	        			if ( issueLinkCol < 0 ) {
    	            			issueLinkCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Link", -1), "");
    	        			}
    	        			if ( issueTitleCol < 0 ) {
    	            			issueTitleCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Title", -1), "");
    	        			}
    	        			//if ( issueNameCol < 0 ) {
    	            			//issueNameCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Name", -1), "");
    	        			//}
    	        			//if ( issueTypeCol < 0 ) {
    	            		//	issueTypeCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Type", -1), "");
    	        			//}
    	        			if ( issuePriorityCol < 0 ) {
    	            			issuePriorityCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Priority", -1), "");
    	        			}
    	        			if ( issueKindCol < 0 ) {
    	            			issueKindCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Kind", -1), "");
    	        			}
    	        			if ( issueStateCol < 0 ) {
    	            			issueStateCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "State", -1), "");
    	        			}
    	        			if ( issueAssigneeCol < 0 ) {
    	            			issueAssigneeCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Assignee", -1), "");
    	        			}
    	        			if ( issueReporterCol < 0 ) {
    	            			issueReporterCol = table.addField(new TableField(TableField.DATA_TYPE_STRING, "Reporter", -1), "");
    	        			}
    	        			if ( issueAgeDaysCol < 0 ) {
    	            			issueAgeDaysCol = table.addField(new TableField(TableField.DATA_TYPE_INT, "AgeDays", -1), "");
    	        			}
    	        			if ( issueCreatedOnCol < 0 ) {
    	            			issueCreatedOnCol = table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "CreatedOn", -1), "");
    	        			}
    	        			if ( issueUpdatedOnCol < 0 ) {
    	            			issueUpdatedOnCol = table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "UpdatedOn", -1), "");
    	        			}
    	        			if ( issueEditedOnCol < 0 ) {
    	            			issueEditedOnCol = table.addField(new TableField(TableField.DATA_TYPE_DATETIME, "EditedOn", -1), "");
    	        			}
    	        		}
    	        	}
    	    	}

    	    	// Call the service that was requested to create the requested output.

    	    	if ( bitbucketCommand == BitbucketCommandType.LIST_PROJECTS ) {
    	    		warningCount = doListProjects (
    	    			dataStore,
    	    			processor,
    	    			session,
    	    			table,
    	    			projectNameCol,
    	    			projectTypeCol,
    	    			projectKeyCol,
    	    			listRepositoriesRegEx,
    	    			ListRepositoriesCountProperty,
    	    			timeoutSeconds,
    	    			status, logLevel, warningCount, commandTag );
    	    	}
    	    	else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES ) {
    	    		warningCount = doListRepositories (
    	    			dataStore,
    	    			processor,
    	    			session,
    	    			null,
    	    			table,
    	    			repositoryNameCol,
    	    			repositorySlugCol,
    	    			repositoryCreatedOnCol,
    	    			repositoryUpdatedOnCol,
    	    			repositoryHasIssuesCol,
    	    			repositoryIsPrivateCol,
    	    			repositorySizeCol,
    	    			repositoryDescriptionCol,
    	    			listRepositoriesRegEx,
    	    			ListRepositoriesCountProperty,
    	    			timeoutSeconds,
    	    			status, logLevel, warningCount, commandTag );
    	    	}
   	        	else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES ) {
   	        		// Read the list of repositories to process:
   	        		// - do not output the repositories to a table since only one table can be output
   	        		List<Repository> repositoryList = new ArrayList<>();
   	        		DataTable repositoryTable = null;
    	    		warningCount = doListRepositories (
    	    			dataStore,
    	    			processor,
    	    			session,
    	    			repositoryList,
    	    			repositoryTable,
    	    			repositoryNameCol,
    	    			repositorySlugCol,
    	    			repositoryCreatedOnCol,
    	    			repositoryUpdatedOnCol,
    	    			repositoryHasIssuesCol,
    	    			repositoryIsPrivateCol,
    	    			repositorySizeCol,
    	    			repositoryDescriptionCol,
    	    			listRepositoriesRegEx,
    	    			ListRepositoriesCountProperty,
    	    			timeoutSeconds,
    	    			status, logLevel, warningCount, commandTag );
   	        		// Read the list of issues for the repositories.
    	    		warningCount = doListRepositoryIssues (
    	    			dataStore,
    	    			processor,
    	    			session,
    	    			repositoryList,
    	    			table,
    	        		issueRepositoryNameCol,
    	    			issueProperties, issuePropertiesCol,
    	        		issueIdCol,
    	        		issueLinkCol,
    	        		issueTitleCol,
    	        		//issueNameCol,
    	        		//issueTypeCol,
    	        		issuePriorityCol,
    	        		issueKindCol,
    	        		issueStateCol,
    	        		issueAssigneeCol,
    	        		issueReporterCol,
    	        		issueAgeDaysCol,
    	        		issueCreatedOnCol,
    	        		issueUpdatedOnCol,
    	        		issueEditedOnCol,
    	    			Assignee,
    	        		includeOpenIssues, includeClosedIssues,
    	    			listRepositoryIssuesRegEx,
    	    			ListRepositoryIssuesCountProperty,
    	    			timeoutSeconds,
    	    			status, logLevel, warningCount, commandTag );
    	    	}

	        	// Create the output file:
	    	   	// - write the table to a delimited file
	    	   	// - TODO smalers 2023-01-28 for now do not write comments, keep very basic

	    	   	if ( doOutputFile ) {
	    		   	String OutputFile_full = IOUtil.verifyPathForOS(
	        		   	IOUtil.toAbsolutePath(TSCommandProcessorUtil.getWorkingDir(processor),
	            		   	TSCommandProcessorUtil.expandParameterValue(processor,this,OutputFile)));
	    		   	if ( OutputFile_full.toUpperCase().endsWith("CSV") ) {
	    			   	boolean writeColumnNames = true;
	    			   	List<String> comments = null;
	    			   	String commentLinePrefix = "#";
	    			   	HashMap<String,Object> writeProps = new HashMap<>();
	    			   	if ( appendOutput && ((OutputTableID == null) || OutputTableID.isEmpty()) ) {
	    			   		// Requested append but the output table was not given:
	    			   		// - therefore the output table was a temporary table
	    			   		// - the output is only for this command so must append to the file (if it exists)
	    				   	writeProps.put("Append", "True");
	    			   	}
	    			   	table.writeDelimitedFile(OutputFile_full, ",", writeColumnNames, comments, commentLinePrefix, writeProps);
	           			setOutputFile(new File(OutputFile_full));
	    		   	}
	    		   	// TODO smalers 2023-01-31 need to implement.
	    		   	//else if ( OutputFile_full.toUpperCase().endsWith("JSON") ) {
	    		   	//}
	    		   	else {
                	   	message = "Requested output file has unknown extension - don't know how to write.";
                	   	Message.printWarning(warningLevel,
                		   	MessageUtil.formatMessageTag( commandTag, ++warningCount), routine, message );
                	   	status.addToLog ( commandPhase,
                		   	new CommandLogRecord(CommandStatusType.FAILURE,
                		   	message, "Use an output file with 'csv' file extension." ) );
	    		   	}
	    	   	}
	    	}
	    	else if ( commandPhase == CommandPhaseType.DISCOVERY ) {
   	        	if ( (bitbucketCommand == BitbucketCommandType.LIST_PROJECTS) ||
   	        		(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES) ||
   	        		(bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES) ) {
   	        		if ( (OutputTableID != null) && !OutputTableID.isEmpty() ) {
   	        			// Have a user-specified table identifier, may use ${Property}.
   	        			if ( table == null ) {
	               			// Did not find table so is being created in this command.
	               			// Create an empty table and set the ID.
	               			table = new DataTable();
   	        			}
               			table.setTableID ( OutputTableID );
	           			setDiscoveryTable ( table );
   	        		}
   	        	}
	    	}

		}
    	catch ( Exception e ) {
  	    	if ( bitbucketCommand == BitbucketCommandType.LIST_PROJECTS ) {
				message = "Unexpected error listing projects (" + e + ").";
			}
  	    	else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORIES ) {
				message = "Unexpected error listing repositories (" + e + ").";
			}
        	else if ( bitbucketCommand == BitbucketCommandType.LIST_REPOSITORY_ISSUES ) {
				message = "Unexpected error listing repository issues (" + e + ").";
        	}
			else {
				message = "Unexpected error for unknown Bitbucket command: " + BitbucketCommand;
			}
			Message.printWarning ( warningLevel,
				MessageUtil.formatMessageTag(commandTag, ++warningCount),routine, message );
			Message.printWarning ( 3, routine, e );
			status.addToLog(CommandPhaseType.RUN,
				new CommandLogRecord(CommandStatusType.FAILURE,
					message, "See the log file for details."));
			throw new CommandException ( message );
		}

    	if ( warningCount > 0 ) {
        	message = "There were " + warningCount + " warnings processing the command.";
        	Message.printWarning ( warningLevel,
            	MessageUtil.formatMessageTag(
            	commandTag, ++warningCount),
            	routine,message);
        	throw new CommandWarningException ( message );
    	}

		status.refreshPhaseSeverity(CommandPhaseType.RUN,CommandStatusType.SUCCESS);
	}

	/**
	Set the table that is read by this class in discovery mode.
	@param table the output table used in discovery mode
	*/
	private void setDiscoveryTable ( DataTable table ) {
    	this.discoveryOutputTable = table;
	}

	/**
	Set the output file that is created by this command.  This is only used internally.
	@param file the output file used in discovery mode
	*/
	private void setOutputFile ( File file ) {
    	__OutputFile_File = file;
	}

	/**
	Return the string representation of the command.
	@param parameters to include in the command
	@return the string representation of the command
	*/
	public String toString ( PropList parameters ) {
		String [] parameterOrder = {
			// General (top).
			"DataStore",
			"BitbucketCommand",
			// List projects.
			"ListProjectsRegEx",
			"ListProjectsCountProperty",
			// List repositories.
			"ListRepositoriesRegEx",
			"ListRepositoriesCountProperty",
			// List repository issues.
			"Assignee",
			"IncludeOpenIssues",
			"IncludeClosedIssues",
			"IssueProperties",
			"ListRepositoryIssuesRegEx",
			"ListRepositoryIssuesCountProperty",
			// Output.
			"OutputTableID",
			"OutputFile",
			"AppendOutput",
			// General (bottom).
			"IfInputNotFound",
			"Timeout"
		};
		return this.toString(parameters, parameterOrder);
	}

}