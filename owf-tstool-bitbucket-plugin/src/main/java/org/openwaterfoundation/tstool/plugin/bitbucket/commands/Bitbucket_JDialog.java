// Bitbucket_JDialog - editor for Bitbucket command

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

package org.openwaterfoundation.tstool.plugin.bitbucket.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openwaterfoundation.tstool.plugin.bitbucket.PluginMeta;
import org.openwaterfoundation.tstool.plugin.bitbucket.datastore.BitbucketDataStore;

import RTi.Util.GUI.JFileChooserFactory;
import RTi.Util.GUI.JGUIUtil;
import RTi.Util.GUI.SimpleJButton;
import RTi.Util.GUI.SimpleJComboBox;
import RTi.Util.Help.HelpViewer;
import RTi.Util.IO.CommandProcessor;
import RTi.Util.IO.IOUtil;
import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import riverside.datastore.DataStore;
import rti.tscommandprocessor.core.TSCommandProcessor;
import rti.tscommandprocessor.core.TSCommandProcessorUtil;

@SuppressWarnings("serial")
public class Bitbucket_JDialog extends JDialog
implements ActionListener, ChangeListener, ItemListener, KeyListener, WindowListener
{
	/**
	 * Tab index for Bitbucket commands.
	 */
	private final int listProjectsTabIndex = 0;
	private final int listRepositoriesTabIndex = 1;
	private final int listRepositoryIssuesTabIndex = 2;

	private final String __AddWorkingDirectory = "Abs";
	private final String __RemoveWorkingDirectory = "Rel";

	private SimpleJButton __browseOutput_JButton = null;
	private SimpleJButton __pathOutput_JButton = null;
	private SimpleJButton __cancel_JButton = null;
	private SimpleJButton __ok_JButton = null;
	private SimpleJButton __help_JButton = null;
	private JTabbedPane __main_JTabbedPane = null;
	//private SimpleJComboBox __IfInputNotFound_JComboBox = null;

	// General (top).
	private SimpleJComboBox __DataStore_JComboBox = null;
	private SimpleJComboBox __BitbucketCommand_JComboBox = null;

	// List Projects tab.
	private JTextField __ListProjectsRegEx_JTextField = null;
	private JTextField __ListProjectsCountProperty_JTextField = null;

	// List Repositories tab.
	private JTextField __ListRepositoriesRegEx_JTextField = null;
	private JTextField __ListRepositoriesCountProperty_JTextField = null;

	// List Repository Issues tab.
	private JTextField __Assignee_JTextField = null;
	private SimpleJComboBox __IncludeOpenIssues_JComboBox = null;
	private SimpleJComboBox __IncludeClosedIssues_JComboBox = null;
	private JTextField __IssueProperties_JTextField = null;
	private JTextField __ListRepositoryIssuesRegEx_JTextField = null;
	private JTextField __ListRepositoryIssuesCountProperty_JTextField = null;

	// Output tab.
	private SimpleJComboBox __OutputTableID_JComboBox = null;
	private JTextField __OutputFile_JTextField = null;
	private SimpleJComboBox __AppendOutput_JComboBox = null;

	// General (bottom).
	private JTextField __Timeout_JTextField;

	private JTextArea __command_JTextArea = null;
	private String __working_dir = null;
	private boolean __error_wait = false;
	private boolean __first_time = true;
	private Bitbucket_Command __command = null;
	private boolean __ok = false; // Whether the user has pressed OK to close the dialog.
	private boolean ignoreEvents = false; // Ignore events when initializing, to avoid infinite loop.
	//private JFrame __parent = null;

	/**
	Command editor constructor.
	@param parent JFrame class instantiating this class.
	@param command Command to edit.
	@param tableIDChoices list of tables to choose from, used if appending
	*/
	public Bitbucket_JDialog ( JFrame parent, Bitbucket_Command command, List<String> tableIDChoices ) {
		super(parent, true);
		initialize ( parent, command, tableIDChoices );
	}

	/**
	Responds to ActionEvents.
	@param event ActionEvent object
	*/
	public void actionPerformed( ActionEvent event ) {
		String routine = getClass().getSimpleName() + ".actionPeformed";
		if ( this.ignoreEvents ) {
	        return; // Startup.
	    }

		Object o = event.getSource();

	    if ( o == this.__BitbucketCommand_JComboBox ) {
	    	setTabForBitbucketCommand();
	    }
	    else if ( o == __browseOutput_JButton ) {
	        String last_directory_selected = JGUIUtil.getLastFileDialogDirectory();
	        JFileChooser fc = null;
	        if ( last_directory_selected != null ) {
	            fc = JFileChooserFactory.createJFileChooser(last_directory_selected );
	        }
	        else {
	            fc = JFileChooserFactory.createJFileChooser(__working_dir );
	        }
	        fc.setDialogTitle( "Select Output File");

	        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
	            String directory = fc.getSelectedFile().getParent();
	            String filename = fc.getSelectedFile().getName();
	            String path = fc.getSelectedFile().getPath();

	            if (filename == null || filename.equals("")) {
	                return;
	            }

	            if (path != null) {
					// Convert path to relative path by default.
					try {
						__OutputFile_JTextField.setText(IOUtil.toRelativePath(__working_dir, path));
					}
					catch ( Exception e ) {
						Message.printWarning ( 1, routine, "Error converting file to relative path." );
					}
	                JGUIUtil.setLastFileDialogDirectory(directory);
	                refresh();
	            }
	        }
	    }
	    else if ( o == __cancel_JButton ) {
			response ( false );
		}
		else if ( o == __help_JButton ) {
			HelpViewer.getInstance().showHelp("command", "Bitbucket", PluginMeta.getDocumentationRootUrl());
		}
		else if ( o == __ok_JButton ) {
			refresh ();
			checkInput();
			if ( !__error_wait ) {
				response ( true );
			}
		}
	    else if ( o == __pathOutput_JButton ) {
	        if ( __pathOutput_JButton.getText().equals(__AddWorkingDirectory) ) {
	            __OutputFile_JTextField.setText (IOUtil.toAbsolutePath(__working_dir,__OutputFile_JTextField.getText() ) );
	        }
	        else if ( __pathOutput_JButton.getText().equals(__RemoveWorkingDirectory) ) {
	            try {
	                __OutputFile_JTextField.setText ( IOUtil.toRelativePath ( __working_dir,
	                        __OutputFile_JTextField.getText() ) );
	            }
	            catch ( Exception e ) {
	                Message.printWarning ( 1,"Bitbucket_JDialog",
	                "Error converting output file name to relative path." );
	            }
	        }
	        refresh ();
	    }
		else {
			// Choices.
			refresh();
		}
	}

	/**
	Check the input.  If errors exist, warn the user and set the __error_wait flag to true.
	This should be called before response() is allowed to complete.
	*/
	private void checkInput () {
		if ( this.ignoreEvents ) {
	        return; // Startup.
	    }
		// Put together a list of parameters to check.
		PropList props = new PropList ( "" );
		// General (top).
	    String DataStore = __DataStore_JComboBox.getSelected();
		String BitbucketCommand = __BitbucketCommand_JComboBox.getSelected();
		// List projects.
		String ListProjectsRegEx = __ListProjectsRegEx_JTextField.getText().trim();
		String ListProjectsCountProperty = __ListProjectsCountProperty_JTextField.getText().trim();
		// List repositories.
		String ListRepositoriesRegEx = __ListRepositoriesRegEx_JTextField.getText().trim();
		String ListRepositoriesCountProperty = __ListRepositoriesCountProperty_JTextField.getText().trim();
		// List repository issues.
		String Assignee = __Assignee_JTextField.getText().trim();
		String IncludeOpenIssues = __IncludeOpenIssues_JComboBox.getSelected();
		String IncludeClosedIssues = __IncludeClosedIssues_JComboBox.getSelected();
		String IssueProperties = __IssueProperties_JTextField.getText().trim();
		String ListRepositoryIssuesRegEx = __ListRepositoryIssuesRegEx_JTextField.getText().trim();
		String ListRepositoryIssuesCountProperty = __ListRepositoryIssuesCountProperty_JTextField.getText().trim();
		// Output.
		String OutputTableID = __OutputTableID_JComboBox.getSelected();
		String OutputFile = __OutputFile_JTextField.getText().trim();
		String AppendOutput = __AppendOutput_JComboBox.getSelected();
		// General (bottom).
		String Timeout = __Timeout_JTextField.getText().trim();
		//String IfInputNotFound = __IfInputNotFound_JComboBox.getSelected();
		__error_wait = false;
	    if ( DataStore.length() > 0 ) {
	        props.set ( "DataStore", DataStore );
	    }
		if ( (BitbucketCommand != null) && !BitbucketCommand.isEmpty() ) {
			props.set ( "BitbucketCommand", BitbucketCommand );
		}
		// List projects.
		if ( (ListProjectsRegEx != null) && !ListProjectsRegEx.isEmpty() ) {
			props.set ( "ListProjectsRegEx", ListProjectsRegEx );
		}
		if ( (ListProjectsCountProperty != null) && !ListProjectsCountProperty.isEmpty() ) {
			props.set ( "ListProjectsCountProperty", ListProjectsCountProperty );
		}
		// List repositories.
		if ( (ListRepositoriesRegEx != null) && !ListRepositoriesRegEx.isEmpty() ) {
			props.set ( "ListRepositoriesRegEx", ListRepositoriesRegEx );
		}
		if ( (ListRepositoriesCountProperty != null) && !ListRepositoriesCountProperty.isEmpty() ) {
			props.set ( "ListRepositoriesCountProperty", ListRepositoriesCountProperty );
		}
		// List repository issues.
		if ( (Assignee != null) && !Assignee.isEmpty() ) {
			props.set ( "Assignee", Assignee );
		}
		if ( (IncludeOpenIssues != null) && !IncludeOpenIssues.isEmpty() ) {
			props.set ( "IncludeOpenIssues", IncludeOpenIssues );
		}
		if ( (IncludeClosedIssues != null) && !IncludeClosedIssues.isEmpty() ) {
			props.set ( "IncludeClosedIssues", IncludeClosedIssues );
		}
		if ( (IssueProperties != null) && !IssueProperties.isEmpty() ) {
			props.set ( "IssueProperties", IssueProperties );
		}
		if ( (ListRepositoryIssuesRegEx != null) && !ListRepositoryIssuesRegEx.isEmpty() ) {
			props.set ( "ListRepositoryIssuesRegEx", ListRepositoryIssuesRegEx );
		}
		if ( (ListRepositoryIssuesCountProperty != null) && !ListRepositoryIssuesCountProperty.isEmpty() ) {
			props.set ( "ListRepositoryIssuesCountProperty", ListRepositoryIssuesCountProperty );
		}
		// Output.
	    if ( (OutputTableID != null) && !OutputTableID.isEmpty() ) {
	        props.set ( "OutputTableID", OutputTableID );
	    }
	    if ( (OutputFile != null) && !OutputFile.isEmpty() ) {
	        props.set ( "OutputFile", OutputFile );
	    }
	    if ( (AppendOutput != null) && !AppendOutput.isEmpty() ) {
	        props.set ( "AppendOutput", AppendOutput );
	    }
	    /*
		if ( IfInputNotFound.length() > 0 ) {
			props.set ( "IfInputNotFound", IfInputNotFound );
		}
		*/
		if ( !Timeout.isEmpty() ) {
			props.set ( "Timeout", Timeout );
		}
		try {
			// This will warn the user.
			__command.checkCommandParameters ( props, null, 1 );
		}
		catch ( Exception e ) {
			// The warning would have been printed in the check code.
			__error_wait = true;
		}
	}

	/**
	Commit the edits to the command.
	In this case the command parameters have already been checked and no errors were detected.
	*/
	private void commitEdits () {
		// General (top).
		String DataStore = __DataStore_JComboBox.getSelected();
		String BitbucketCommand = __BitbucketCommand_JComboBox.getSelected();
		// List projects.
		String ListProjectsRegEx = __ListProjectsRegEx_JTextField.getText().trim();
		String ListProjectsCountProperty = __ListProjectsCountProperty_JTextField.getText().trim();
		// List repositories.
		String ListRepositoriesRegEx = __ListRepositoriesRegEx_JTextField.getText().trim();
		String ListRepositoriesCountProperty = __ListRepositoriesCountProperty_JTextField.getText().trim();
		// List repository issues.
		String Assignee = __Assignee_JTextField.getText().trim();
		String IncludeOpenIssues = __IncludeOpenIssues_JComboBox.getSelected();
		String IncludeClosedIssues = __IncludeClosedIssues_JComboBox.getSelected();
		String IssueProperties = __IssueProperties_JTextField.getText().trim();
		String ListRepositoryIssuesRegEx = __ListRepositoryIssuesRegEx_JTextField.getText().trim();
		String ListRepositoryIssuesCountProperty = __ListRepositoryIssuesCountProperty_JTextField.getText().trim();
		// Output
		String OutputTableID = __OutputTableID_JComboBox.getSelected();
	    String OutputFile = __OutputFile_JTextField.getText().trim();
		String AppendOutput = __AppendOutput_JComboBox.getSelected();
		//String IfInputNotFound = __IfInputNotFound_JComboBox.getSelected();
		// General (bottom).
		String Timeout = __Timeout_JTextField.getText().trim();

	    // General (top).
	    __command.setCommandParameter ( "DataStore", DataStore );
		__command.setCommandParameter ( "BitbucketCommand", BitbucketCommand );
		// List projects.
		__command.setCommandParameter ( "ListProjectsRegEx", ListProjectsRegEx );
		__command.setCommandParameter ( "ListProjectsCountProperty", ListProjectsCountProperty );
		// List repositories.
		__command.setCommandParameter ( "ListRepositoriesRegEx", ListRepositoriesRegEx );
		__command.setCommandParameter ( "ListRepositoriesCountProperty", ListRepositoriesCountProperty );
		// List repository issues.
		__command.setCommandParameter ( "Assignee", Assignee );
		__command.setCommandParameter ( "IncludeOpenIssues", IncludeOpenIssues );
		__command.setCommandParameter ( "IncludeClosedIssues", IncludeClosedIssues );
		__command.setCommandParameter ( "IssueProperties", IssueProperties );
		__command.setCommandParameter ( "ListRepositoryIssuesRegEx", ListRepositoryIssuesRegEx );
		__command.setCommandParameter ( "ListRepositoryIssuesCountProperty", ListRepositoryIssuesCountProperty );
		// Output.
		__command.setCommandParameter ( "OutputTableID", OutputTableID );
		__command.setCommandParameter ( "OutputFile", OutputFile );
		__command.setCommandParameter ( "AppendOutput", AppendOutput );
		//__command.setCommandParameter ( "IfInputNotFound", IfInputNotFound );
		__command.setCommandParameter ( "Timeout", Timeout );
	}

	/**
	Instantiates the GUI components.
	@param parent JFrame class instantiating this class.
	@param command Command to edit.
	@param tableIDChoices list of tables to choose from, used if appending
	*/
	private void initialize ( JFrame parent, Bitbucket_Command command, List<String> tableIDChoices ) {
		String routine = getClass().getSimpleName() + ".initialize";
		this.__command = command;
		//this.__parent = parent;
		CommandProcessor processor =__command.getCommandProcessor();

		__working_dir = TSCommandProcessorUtil.getWorkingDirForCommand ( processor, __command );

		addWindowListener( this );

	    Insets insetsTLBR = new Insets(2,2,2,2);

		// Main panel.

		JPanel main_JPanel = new JPanel();
		main_JPanel.setLayout( new GridBagLayout() );
		getContentPane().add ( "North", main_JPanel );
		int y = -1;

	    JGUIUtil.addComponent(main_JPanel, new JLabel ("Run a Bitbucket command."),
			0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel (
	    	"Features are implemented to list projects, repositories and repository issues."),
			0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    if ( __working_dir != null ) {
	    	JGUIUtil.addComponent(main_JPanel, new JLabel (
			"It is recommended that file and folders on the local computer are specified relative to the working directory, which is:"),
			0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    	JGUIUtil.addComponent(main_JPanel, new JLabel ("    " + __working_dir),
			0, ++y, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    }

	   	this.ignoreEvents = true; // So that a full pass of initialization can occur.

	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Bitbucket datastore:"),
	        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __DataStore_JComboBox = new SimpleJComboBox ( false );
	    TSCommandProcessor tsProcessor = (TSCommandProcessor)processor;
	    Message.printStatus(2, routine, "Getting datastores for BitbucketDataStore class");
	    List<DataStore> dataStoreList = tsProcessor.getDataStoresByType( BitbucketDataStore.class );
	    // Datastore is required, so no blank.
	    List<String> datastoreChoices = new ArrayList<>();
	    for ( DataStore dataStore: dataStoreList ) {
	    	datastoreChoices.add ( dataStore.getName() );
	    }
	    __DataStore_JComboBox.setData(datastoreChoices);
	    if ( datastoreChoices.size() > 0 ) {
	    	__DataStore_JComboBox.select ( 0 );
	    }
	    __DataStore_JComboBox.addItemListener ( this );
	    JGUIUtil.addComponent(main_JPanel, __DataStore_JComboBox,
	        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel("Required - Bitbucket datastore."),
	        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Bitbucket command:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__BitbucketCommand_JComboBox = new SimpleJComboBox ( false );
		__BitbucketCommand_JComboBox.setToolTipText("Bitbucket command to execute.");
		List<String> commandChoices = BitbucketCommandType.getChoicesAsStrings(false);
		__BitbucketCommand_JComboBox.setData(commandChoices);
		__BitbucketCommand_JComboBox.select ( 0 );
		__BitbucketCommand_JComboBox.addActionListener ( this );
	    JGUIUtil.addComponent(main_JPanel, __BitbucketCommand_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel("Required - Bitbucket command to run (see tabs below)."),
			3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    __main_JTabbedPane = new JTabbedPane ();
	    __main_JTabbedPane.addChangeListener(this);
	    JGUIUtil.addComponent(main_JPanel, __main_JTabbedPane,
	        0, ++y, 7, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	    // Panel for 'List Projects' parameters.
	    int yListProjects = -1;
	    JPanel libProjects_JPanel = new JPanel();
	    libProjects_JPanel.setLayout( new GridBagLayout() );
	    __main_JTabbedPane.addTab ( "List Projects", libProjects_JPanel );

	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel ("List all projects that are visible to the user based on the datastore configuration."),
			0, ++yListProjects, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel ("Use * in the regular expression as wildcards to filter the results."),
			0, ++yListProjects, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel ("See the 'Output' tab to specify the output table and/or file for the project list."),
			0, ++yListProjects, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(libProjects_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
	    	0, ++yListProjects, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel ( "Regular expression:"),
	        0, ++yListProjects, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __ListProjectsRegEx_JTextField = new JTextField ( "", 30 );
	    __ListProjectsRegEx_JTextField.setToolTipText("Regular expression to filter results, default=glob (*) style");
	    __ListProjectsRegEx_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(libProjects_JPanel, __ListProjectsRegEx_JTextField,
	        1, yListProjects, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel ( "Optional - regular expression filter (default=list all)."),
	        3, yListProjects, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel("List projects count property:"),
	        0, ++yListProjects, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __ListProjectsCountProperty_JTextField = new JTextField ( "", 30 );
	    __ListProjectsCountProperty_JTextField.setToolTipText("Specify the property name for the project list result size, can use ${Property} notation");
	    __ListProjectsCountProperty_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(libProjects_JPanel, __ListProjectsCountProperty_JTextField,
	        1, yListProjects, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(libProjects_JPanel, new JLabel ( "Optional - processor property to set as repository count." ),
	        3, yListProjects, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    // Panel for 'List Repositories' parameters.
	    int yListRepos = -1;
	    JPanel listRepos_JPanel = new JPanel();
	    listRepos_JPanel.setLayout( new GridBagLayout() );
	    __main_JTabbedPane.addTab ( "List Repositories", listRepos_JPanel );

	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ("List all repositories that are visible to the user based on the datastore configuration."),
			0, ++yListRepos, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ("A filter can also be provided to use when listing repository issues."),
			0, ++yListRepos, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ("Use * in the regular expression as wildcards to filter the results."),
			0, ++yListRepos, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ("See the 'Output' tab to specify the output table and/or file for the repository list."),
			0, ++yListRepos, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listRepos_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
	    	0, ++yListRepos, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ( "Regular expression:"),
	        0, ++yListRepos, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __ListRepositoriesRegEx_JTextField = new JTextField ( "", 30 );
	    __ListRepositoriesRegEx_JTextField.setToolTipText("Regular expression to filter results, default=glob (*) style");
	    __ListRepositoriesRegEx_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(listRepos_JPanel, __ListRepositoriesRegEx_JTextField,
	        1, yListRepos, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ( "Optional - regular expression filter (default=list all)."),
	        3, yListRepos, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel("List repositories count property:"),
	        0, ++yListRepos, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __ListRepositoriesCountProperty_JTextField = new JTextField ( "", 30 );
	    __ListRepositoriesCountProperty_JTextField.setToolTipText("Specify the property name for the repository list result size, can use ${Property} notation");
	    __ListRepositoriesCountProperty_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(listRepos_JPanel, __ListRepositoriesCountProperty_JTextField,
	        1, yListRepos, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listRepos_JPanel, new JLabel ( "Optional - processor property to set as repository count." ),
	        3, yListRepos, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    // Panel for 'List Repository Issues' parameters:
	    // - this includes filtering
	    int yListIssues = -1;
	    JPanel listIssues_JPanel = new JPanel();
	    listIssues_JPanel.setLayout( new GridBagLayout() );
	    __main_JTabbedPane.addTab ( "List Repository Issues", listIssues_JPanel );

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ("List all repository issues that are visible to the user based on the datastore configuration."),
			0, ++yListIssues, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ("See the 'Output' tab to specify the output file and/or table for the bucket object list."),
			0, ++yListIssues, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
	    	0, ++yListIssues, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Assignee:"),
	        0, ++yListIssues, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __Assignee_JTextField = new JTextField ( "", 30 );
	    __Assignee_JTextField.setToolTipText("Assignee to match, can use ${Property}.");
	    __Assignee_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(listIssues_JPanel, __Assignee_JTextField,
	        1, yListIssues, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Optional - assignee to match (default=list all)."),
	        3, yListIssues, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Include open issues?:"),
			0, ++yListIssues, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__IncludeOpenIssues_JComboBox = new SimpleJComboBox ( false );
		__IncludeOpenIssues_JComboBox.setToolTipText("Include open issues (state is 'new' or 'open')?");
		List<String> openIssueChoices = new ArrayList<>();
		openIssueChoices.add("");
		openIssueChoices.add(command._False);
		openIssueChoices.add(command._True);
		__IncludeOpenIssues_JComboBox.setData(openIssueChoices);
		__IncludeOpenIssues_JComboBox.select ( 0 );
		__IncludeOpenIssues_JComboBox.addActionListener ( this );
	    JGUIUtil.addComponent(listIssues_JPanel, __IncludeOpenIssues_JComboBox,
			1, yListIssues, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel("Optional - whether to include open issues (default=" + command._True + ")."),
			3, yListIssues, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Include closed issues?:"),
			0, ++yListIssues, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__IncludeClosedIssues_JComboBox = new SimpleJComboBox ( false );
		__IncludeClosedIssues_JComboBox.setToolTipText("Include closed issues (state is other than 'new' or 'open')?");
		List<String> closedIssueChoices = new ArrayList<>();
		closedIssueChoices.add("");
		closedIssueChoices.add(command._False);
		closedIssueChoices.add(command._True);
		__IncludeClosedIssues_JComboBox.setData(closedIssueChoices);
		__IncludeClosedIssues_JComboBox.select ( 0 );
		__IncludeClosedIssues_JComboBox.addActionListener ( this );
	    JGUIUtil.addComponent(listIssues_JPanel, __IncludeClosedIssues_JComboBox,
			1, yListIssues, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel("Optional - whether to include closed issues (default=" + command._False + ")."),
			3, yListIssues, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Issue properties to parse:"),
	        0, ++yListIssues, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __IssueProperties_JTextField = new JTextField ( "", 30 );
	    __IssueProperties_JTextField.setToolTipText("Names of properties to parse from issue // lines.");
	    __IssueProperties_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(listIssues_JPanel, __IssueProperties_JTextField,
	        1, yListIssues, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Optional - names of issue properties to parse (default=not parsed)."),
	        3, yListIssues, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Regular expression:"),
	        0, ++yListIssues, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __ListRepositoryIssuesRegEx_JTextField = new JTextField ( "", 30 );
	    __ListRepositoryIssuesRegEx_JTextField.setToolTipText("Regular expression to filter results, default=glob (*) style");
	    __ListRepositoryIssuesRegEx_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(listIssues_JPanel, __ListRepositoryIssuesRegEx_JTextField,
	        1, yListIssues, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Optional - regular expression filter (default=list all)."),
	        3, yListIssues, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel("List repository issues count property:"),
	        0, ++yListIssues, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __ListRepositoryIssuesCountProperty_JTextField = new JTextField ( "", 30 );
	    __ListRepositoryIssuesCountProperty_JTextField.setToolTipText("Specify the property name for the bucket object list result size, can use ${Property} notation");
	    __ListRepositoryIssuesCountProperty_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(listIssues_JPanel, __ListRepositoryIssuesCountProperty_JTextField,
	        1, yListIssues, 1, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(listIssues_JPanel, new JLabel ( "Optional - processor property to set as object count." ),
	        3, yListIssues, 3, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    // Panel for output.
	    int yOutput = -1;
	    JPanel output_JPanel = new JPanel();
	    output_JPanel.setLayout( new GridBagLayout() );
	    __main_JTabbedPane.addTab ( "Output", output_JPanel );

	    JGUIUtil.addComponent(output_JPanel, new JLabel (
	    	"The following parameters are used with 'List Projects', 'List Repositories' and 'List Repository Issues' commands."),
			0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel ("An output table and/or file can be created."),
			0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel ("An existing table will be appended to if found."),
			0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel ("The output file uses the specified table (or a temporary table) to create the output file."),
			0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel ("Specify the output file name with extension to indicate the format: csv"),
			0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel ("See also other commands to write tables."),
			0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JSeparator(SwingConstants.HORIZONTAL),
	    	0, ++yOutput, 8, 1, 0, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(output_JPanel, new JLabel ( "Output Table ID:" ),
	        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __OutputTableID_JComboBox = new SimpleJComboBox ( 12, true ); // Allow edit.
	    __OutputTableID_JComboBox.setToolTipText("Table for output, available for List Buckets and List Objects");
	    tableIDChoices.add(0,""); // Add blank to ignore table.
	    __OutputTableID_JComboBox.setData ( tableIDChoices );
	    __OutputTableID_JComboBox.addItemListener ( this );
	    __OutputTableID_JComboBox.getJTextComponent().addKeyListener ( this );
	    //__OutputTableID_JComboBox.setMaximumRowCount(tableIDChoices.size());
	    JGUIUtil.addComponent(output_JPanel, __OutputTableID_JComboBox,
	        1, yOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel( "Optional - table for output."),
	        3, yOutput, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(output_JPanel, new JLabel ("Output file:" ),
	        0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __OutputFile_JTextField = new JTextField ( 50 );
	    __OutputFile_JTextField.setToolTipText(
	    	"Output file, available for List Projects, List Repositories, and List Issues, can use ${Property} notation.");
	    __OutputFile_JTextField.addKeyListener ( this );
	    // Output file layout fights back with other rows so put in its own panel.
		JPanel OutputFile_JPanel = new JPanel();
		OutputFile_JPanel.setLayout(new GridBagLayout());
	    JGUIUtil.addComponent(OutputFile_JPanel, __OutputFile_JTextField,
			0, 0, 1, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
		__browseOutput_JButton = new SimpleJButton ( "...", this );
		__browseOutput_JButton.setToolTipText("Browse for file");
	    JGUIUtil.addComponent(OutputFile_JPanel, __browseOutput_JButton,
			1, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.CENTER);
		if ( __working_dir != null ) {
			// Add the button to allow conversion to/from relative path.
			__pathOutput_JButton = new SimpleJButton( __RemoveWorkingDirectory,this);
			JGUIUtil.addComponent(OutputFile_JPanel, __pathOutput_JButton,
				2, 0, 1, 1, 0.0, 0.0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
		}
		JGUIUtil.addComponent(output_JPanel, OutputFile_JPanel,
			1, yOutput, 6, 1, 1.0, 0.0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

	   JGUIUtil.addComponent(output_JPanel, new JLabel ( "Append output?:"),
			0, ++yOutput, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__AppendOutput_JComboBox = new SimpleJComboBox ( false );
		__AppendOutput_JComboBox.setToolTipText("Append output to existing table or file?");
		List<String> appendChoices = new ArrayList<>();
		appendChoices.add ( "" );	// Default.
		appendChoices.add ( __command._False );
		appendChoices.add ( __command._True );
		__AppendOutput_JComboBox.setData(appendChoices);
		__AppendOutput_JComboBox.select ( 0 );
		__AppendOutput_JComboBox.addActionListener ( this );
	    JGUIUtil.addComponent(output_JPanel, __AppendOutput_JComboBox,
			1, yOutput, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(output_JPanel, new JLabel(
			"Optional - append to output (default=" + __command._False + ")."),
			3, yOutput, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    /*
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "If input not found?:"),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__IfInputNotFound_JComboBox = new SimpleJComboBox ( false );
		List<String> notFoundChoices = new ArrayList<>();
		notFoundChoices.add ( "" );	// Default.
		notFoundChoices.add ( __command._Ignore );
		notFoundChoices.add ( __command._Warn );
		notFoundChoices.add ( __command._Fail );
		__IfInputNotFound_JComboBox.setData(notFoundChoices);
		__IfInputNotFound_JComboBox.select ( 0 );
		__IfInputNotFound_JComboBox.addActionListener ( this );
	    JGUIUtil.addComponent(main_JPanel, __IfInputNotFound_JComboBox,
			1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel(
			"Optional - action if input file is not found (default=" + __command._Warn + ")."),
			3, y, 2, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);
			*/

	    // General (bottom).

	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Timeout:"),
	        0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
	    __Timeout_JTextField = new JTextField ( "", 20 );
	    __Timeout_JTextField.setToolTipText("Timeout for connection and read, in seconds.");
	    __Timeout_JTextField.addKeyListener ( this );
	    JGUIUtil.addComponent(main_JPanel, __Timeout_JTextField,
	        1, y, 2, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);
	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Optional - timeout for requests, seconds (default = 300 = 5 minutes)."),
	        3, y, 4, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.WEST);

	    JGUIUtil.addComponent(main_JPanel, new JLabel ( "Command:" ),
			0, ++y, 1, 1, 0, 0, insetsTLBR, GridBagConstraints.NONE, GridBagConstraints.EAST);
		__command_JTextArea = new JTextArea ( 4, 60 );
		__command_JTextArea.setLineWrap ( true );
		__command_JTextArea.setWrapStyleWord ( true );
		__command_JTextArea.addKeyListener ( this );
		__command_JTextArea.setEditable ( false );
		JGUIUtil.addComponent(main_JPanel, new JScrollPane(__command_JTextArea),
			1, y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST);

		// South Panel: North
		JPanel button_JPanel = new JPanel();
		button_JPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
	        JGUIUtil.addComponent(main_JPanel, button_JPanel,
			0, ++y, 8, 1, 1, 0, insetsTLBR, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);

	    button_JPanel.add ( __ok_JButton = new SimpleJButton("OK", this) );
		__ok_JButton.setToolTipText("Save changes to command");
		button_JPanel.add(__cancel_JButton = new SimpleJButton("Cancel", this));
		__cancel_JButton.setToolTipText("Cancel without saving changes to command");
		button_JPanel.add ( __help_JButton = new SimpleJButton("Help", this) );
		__help_JButton.setToolTipText("Show command documentation in web browser");

		setTitle ( "Edit " + __command.getCommandName() + "() command" );

	    this.ignoreEvents = false; // After initialization of components let events happen to dynamically cause cascade.

		// Refresh the contents.
	    refresh ();

	    pack();
	    JGUIUtil.center( this );
		// Dialogs do not need to be resizable.
		setResizable ( false );
	    super.setVisible( true );
	}

	/**
	Handle ItemEvent events.
	@param e ItemEvent to handle.
	*/
	public void itemStateChanged ( ItemEvent e ) {
		if ( this.ignoreEvents ) {
	        return; // Startup.
	    }
		Object o = e.getSource();
	    if ( o == this.__BitbucketCommand_JComboBox ) {
	    	setTabForBitbucketCommand();
	    }
		refresh();
	}

	/**
	Respond to KeyEvents.
	*/
	public void keyPressed ( KeyEvent event ) {
		int code = event.getKeyCode();

		if ( code == KeyEvent.VK_ENTER ) {
			refresh ();
		}
	}

	public void keyReleased ( KeyEvent event ) {
		refresh();
	}

	public void keyTyped ( KeyEvent event ) {
	}

	/**
	Indicate if the user pressed OK (cancel otherwise).
	*/
	public boolean ok () {
		return __ok;
	}

	/**
	Refresh the command from the other text field contents.
	*/
	private void refresh () {
		String routine = getClass().getSimpleName() + ".refresh";
		// General (top).
		String DataStore = "";
		String BitbucketCommand = "";
		// List projects.
		String ListProjectsRegEx = "";
		String ListProjectsCountProperty = "";
		// List repositories.
		String ListRepositoriesRegEx = "";
		String ListRepositoriesCountProperty = "";
		// List repository issues.
		String Assignee = "";
		String IncludeOpenIssues = "";
		String IncludeClosedIssues = "";
		String IssueProperties = "";
		String ListRepositoryIssuesRegEx = "";
		String ListRepositoryIssuesCountProperty = "";
		// Output.
		String OutputTableID = "";
		String OutputFile = "";
		String AppendOutput = "";
		//String IfInputNotFound = "";
		// General (bottom).
		String Timeout = "";
	    PropList parameters = null;
		if ( __first_time ) {
			__first_time = false;
	        parameters = __command.getCommandParameters();
	        // General (top).
		    DataStore = parameters.getValue ( "DataStore" );
			BitbucketCommand = parameters.getValue ( "BitbucketCommand" );
			// List projects.
			ListProjectsRegEx = parameters.getValue ( "ListProjectsRegEx" );
			ListProjectsCountProperty = parameters.getValue ( "ListProjectsCountProperty" );
			// List repositories.
			ListRepositoriesRegEx = parameters.getValue ( "ListRepositoriesRegEx" );
			ListRepositoriesCountProperty = parameters.getValue ( "ListRepositoriesCountProperty" );
			// List repository issues.
			Assignee = parameters.getValue ( "Assignee" );
			IncludeOpenIssues = parameters.getValue ( "IncludeOpenIssues" );
			IncludeClosedIssues = parameters.getValue ( "IncludeClosedIssues" );
			IssueProperties = parameters.getValue ( "IssueProperties" );
			ListRepositoryIssuesRegEx = parameters.getValue ( "ListRepositoryIssuesRegEx" );
			ListRepositoryIssuesCountProperty = parameters.getValue ( "ListRepositoryIssuesCountProperty" );
			// Output
			OutputTableID = parameters.getValue ( "OutputTableID" );
			OutputFile = parameters.getValue ( "OutputFile" );
			AppendOutput = parameters.getValue ( "AppendOutput" );
			//IfInputNotFound = parameters.getValue ( "IfInputNotFound" );
			// General (bottom).
			Timeout = parameters.getValue ( "Timeout" );

			// General (top).
	        // The data store list is set up in initialize() but is selected here.
	        if ( JGUIUtil.isSimpleJComboBoxItem(__DataStore_JComboBox, DataStore, JGUIUtil.NONE, null, null ) ) {
	            __DataStore_JComboBox.select ( null ); // To ensure that following causes an event.
	            __DataStore_JComboBox.select ( DataStore ); // This will trigger getting the DMI for use in the editor.
	        }
	        else {
	            if ( (DataStore == null) || DataStore.equals("") ) {
	                // New command...select the default.
	                __DataStore_JComboBox.select ( null ); // To ensure that following causes an event.
	                if ( __DataStore_JComboBox.getItemCount() > 0 ) {
	                	__DataStore_JComboBox.select ( 0 );
	                }
	            }
	            else {
	                // Bad user command.
	                Message.printWarning ( 1, routine, "Existing command references an invalid\n"+
	                  "DataStore parameter \"" + DataStore + "\".  Select a\ndifferent value or Cancel." );
	            }
	        }
			if ( JGUIUtil.isSimpleJComboBoxItem(__BitbucketCommand_JComboBox, BitbucketCommand,JGUIUtil.NONE, null, null ) ) {
				__BitbucketCommand_JComboBox.select ( BitbucketCommand );
			}
			else {
	            if ( (BitbucketCommand == null) || BitbucketCommand.equals("") ) {
					// New command...select the default.
					__BitbucketCommand_JComboBox.select ( 0 );
				}
				else {
					// Bad user command.
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n"+
					"BitbucketCommand parameter \"" + BitbucketCommand + "\".  Select a value or Cancel." );
				}
			}
	        if ( ListProjectsRegEx != null ) {
	            __ListProjectsRegEx_JTextField.setText ( ListProjectsRegEx );
	        }
	        if ( ListProjectsCountProperty != null ) {
	            __ListProjectsCountProperty_JTextField.setText ( ListProjectsCountProperty );
	        }
	        if ( ListRepositoriesRegEx != null ) {
	            __ListRepositoriesRegEx_JTextField.setText ( ListRepositoriesRegEx );
	        }
	        if ( ListRepositoriesCountProperty != null ) {
	            __ListRepositoriesCountProperty_JTextField.setText ( ListRepositoriesCountProperty );
	        }
	        if ( Assignee != null ) {
	            __Assignee_JTextField.setText ( Assignee );
	        }
			if ( JGUIUtil.isSimpleJComboBoxItem(__IncludeOpenIssues_JComboBox, IncludeOpenIssues,JGUIUtil.NONE, null, null ) ) {
				__IncludeOpenIssues_JComboBox.select ( IncludeOpenIssues );
			}
			else {
	            if ( (IncludeOpenIssues == null) || IncludeOpenIssues.equals("") ) {
					// New command...select the default.
					__IncludeOpenIssues_JComboBox.select ( 0 );
				}
				else {
					// Bad user command.
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n"+
					"IncludeOpenIssues parameter \"" + IncludeOpenIssues + "\".  Select a value or Cancel." );
				}
			}
			if ( JGUIUtil.isSimpleJComboBoxItem(__IncludeClosedIssues_JComboBox, IncludeClosedIssues,JGUIUtil.NONE, null, null ) ) {
				__IncludeClosedIssues_JComboBox.select ( IncludeClosedIssues );
			}
			else {
	            if ( (IncludeClosedIssues == null) || IncludeClosedIssues.equals("") ) {
					// New command...select the default.
					__IncludeClosedIssues_JComboBox.select ( 0 );
				}
				else {
					// Bad user command.
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n"+
					"IncludeClosedIssues parameter \"" + IncludeClosedIssues + "\".  Select a value or Cancel." );
				}
			}
	        if ( IssueProperties != null ) {
	            __IssueProperties_JTextField.setText ( IssueProperties );
	        }
	        if ( ListRepositoryIssuesRegEx != null ) {
	            __ListRepositoryIssuesRegEx_JTextField.setText ( ListRepositoryIssuesRegEx );
	        }
	        if ( ListRepositoryIssuesCountProperty != null ) {
	            __ListRepositoryIssuesCountProperty_JTextField.setText ( ListRepositoryIssuesCountProperty );
	        }
	        if ( OutputTableID == null ) {
	            // Select default.
	            __OutputTableID_JComboBox.select ( 0 );
	        }
	        else {
	            if ( JGUIUtil.isSimpleJComboBoxItem( __OutputTableID_JComboBox,OutputTableID, JGUIUtil.NONE, null, null ) ) {
	                __OutputTableID_JComboBox.select ( OutputTableID );
	            }
	            else {
	                // Creating new table so add in the first position.
	                if ( __OutputTableID_JComboBox.getItemCount() == 0 ) {
	                    __OutputTableID_JComboBox.add(OutputTableID);
	                }
	                else {
	                    __OutputTableID_JComboBox.insert(OutputTableID, 0);
	                }
	                __OutputTableID_JComboBox.select(0);
	            }
	        }
	        if ( OutputFile != null ) {
	            __OutputFile_JTextField.setText ( OutputFile );
	        }
			if ( JGUIUtil.isSimpleJComboBoxItem(__AppendOutput_JComboBox, AppendOutput,JGUIUtil.NONE, null, null ) ) {
				__AppendOutput_JComboBox.select ( AppendOutput );
			}
			else {
	            if ( (AppendOutput == null) ||	AppendOutput.equals("") ) {
					// New command...select the default.
					__AppendOutput_JComboBox.select ( 0 );
				}
				else {
					// Bad user command.
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n"+
					"AppendOutput parameter \"" + AppendOutput + "\".  Select a value or Cancel." );
				}
			}
	        /*
			if ( JGUIUtil.isSimpleJComboBoxItem(__IfInputNotFound_JComboBox, IfInputNotFound,JGUIUtil.NONE, null, null ) ) {
				__IfInputNotFound_JComboBox.select ( IfInputNotFound );
			}
			else {
	            if ( (IfInputNotFound == null) ||	IfInputNotFound.equals("") ) {
					// New command...select the default.
					__IfInputNotFound_JComboBox.select ( 0 );
				}
				else {
					// Bad user command.
					Message.printWarning ( 1, routine,
					"Existing command references an invalid\n"+
					"IfInputNotFound parameter \"" + IfInputNotFound + "\".  Select a value or Cancel." );
				}
			}
			*/
		    if ( Timeout != null ) {
		    	__Timeout_JTextField.setText ( Timeout );
		    }
			// Set the tab for selected Bitbucket command.
			setTabForBitbucketCommand();
		}
		// Regardless, reset the command from the fields.
		// This is only  visible information that has not been committed in the command.
		// General (top).
	    DataStore = __DataStore_JComboBox.getSelected();
	    if ( DataStore == null ) {
	        DataStore = "";
	    }
		BitbucketCommand = __BitbucketCommand_JComboBox.getSelected();
		// List projects.
		ListProjectsRegEx = __ListProjectsRegEx_JTextField.getText().trim();
		ListProjectsCountProperty = __ListProjectsCountProperty_JTextField.getText().trim();
		// List repositories.
		ListRepositoriesRegEx = __ListRepositoriesRegEx_JTextField.getText().trim();
		ListRepositoriesCountProperty = __ListRepositoriesCountProperty_JTextField.getText().trim();
		// List repository issues.
		Assignee = __Assignee_JTextField.getText().trim();
		IncludeOpenIssues = __IncludeOpenIssues_JComboBox.getSelected();
		IncludeClosedIssues = __IncludeClosedIssues_JComboBox.getSelected();
		IssueProperties = __IssueProperties_JTextField.getText().trim();
		ListRepositoryIssuesRegEx = __ListRepositoryIssuesRegEx_JTextField.getText().trim();
		ListRepositoryIssuesCountProperty = __ListRepositoryIssuesCountProperty_JTextField.getText().trim();
		// Output
		OutputTableID = __OutputTableID_JComboBox.getSelected();
		OutputFile = __OutputFile_JTextField.getText().trim();
		AppendOutput = __AppendOutput_JComboBox.getSelected();
	    // General (bottom).
		Timeout = __Timeout_JTextField.getText().trim();
		//IfInputNotFound = __IfInputNotFound_JComboBox.getSelected();
		PropList props = new PropList ( __command.getCommandName() );
	    // General (top).
	    props.add ( "DataStore=" + DataStore );
		props.add ( "BitbucketCommand=" + BitbucketCommand );
		// List projects.
		props.add ( "ListProjectsRegEx=" + ListProjectsRegEx );
		props.add ( "ListProjectsCountProperty=" + ListProjectsCountProperty );
		// List repositories.
		props.add ( "ListRepositoriesRegEx=" + ListRepositoriesRegEx );
		props.add ( "ListRepositoriesCountProperty=" + ListRepositoriesCountProperty );
		// List repository issues.
		props.add ( "Assignee=" + Assignee );
		props.add ( "IncludeOpenIssues=" + IncludeOpenIssues );
		props.add ( "IncludeClosedIssues=" + IncludeClosedIssues );
		props.add ( "IssueProperties=" + IssueProperties );
		props.add ( "ListRepositoryIssuesRegEx=" + ListRepositoryIssuesRegEx );
		props.add ( "ListRepositoryIssuesCountProperty=" + ListRepositoryIssuesCountProperty );
		// Output.
		props.add ( "OutputTableID=" + OutputTableID );
		props.add ( "OutputFile=" + OutputFile );
		props.add ( "AppendOutput=" + AppendOutput );
		//props.add ( "IfInputNotFound=" + IfInputNotFound );
	    // General (bottom).
		props.add ( "Timeout=" + Timeout );
		__command_JTextArea.setText( __command.toString(props).trim() );
		// Check the path and determine what the label on the path button should be.
	    if ( __pathOutput_JButton != null ) {
			if ( (OutputFile != null) && !OutputFile.isEmpty() ) {
				__pathOutput_JButton.setEnabled ( true );
				File f = new File ( OutputFile );
				if ( f.isAbsolute() ) {
					__pathOutput_JButton.setText ( __RemoveWorkingDirectory );
					__pathOutput_JButton.setToolTipText("Change path to relative to command file");
				}
				else {
	            	__pathOutput_JButton.setText ( __AddWorkingDirectory );
	            	__pathOutput_JButton.setToolTipText("Change path to absolute");
				}
			}
			else {
				__pathOutput_JButton.setEnabled(false);
			}
	    }
	}

	/**
	React to the user response.
	@param ok if false, then the edit is canceled.  If true, the edit is committed and the dialog is closed.
	*/
	public void response ( boolean ok ) {
		__ok = ok;
		if ( ok ) {
			// Commit the changes.
			commitEdits ();
			if ( __error_wait ) {
				// Not ready to close out.
				return;
			}
		}
		// Now close out.
		setVisible( false );
		dispose();
	}

	/**
	 * Set the parameter tab based on the selected command.
	 */
	private void setTabForBitbucketCommand() {
		String command = __BitbucketCommand_JComboBox.getSelected();
		if ( command.equalsIgnoreCase("" + BitbucketCommandType.LIST_PROJECTS) ) {
			__main_JTabbedPane.setSelectedIndex(this.listProjectsTabIndex);
		}
		else if ( command.equalsIgnoreCase("" + BitbucketCommandType.LIST_REPOSITORIES) ) {
			__main_JTabbedPane.setSelectedIndex(this.listRepositoriesTabIndex);
		}
		else if ( command.equalsIgnoreCase("" + BitbucketCommandType.LIST_REPOSITORY_ISSUES) ) {
			__main_JTabbedPane.setSelectedIndex(this.listRepositoryIssuesTabIndex);
		}
	}

	/**
	 * Handle JTabbedPane changes.
	 */
	public void stateChanged ( ChangeEvent event ) {
		//JTabbedPane sourceTabbedPane = (JTabbedPane)event.getSource();
		//int index = sourceTabbedPane.getSelectedIndex();
	}

	/**
	Responds to WindowEvents.
	@param event WindowEvent object
	*/
	public void windowClosing( WindowEvent event ) {
		response ( false );
	}

	public void windowActivated( WindowEvent evt ) {
	}

	public void windowClosed( WindowEvent evt ) {
	}

	public void windowDeactivated( WindowEvent evt ) {
	}

	public void windowDeiconified( WindowEvent evt ) {
	}

	public void windowIconified( WindowEvent evt ) {
	}

	public void windowOpened( WindowEvent evt ) {
	}

}