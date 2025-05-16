// BitbucketAuthenticationType - Bitbucket authentication type enumeration

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

import java.util.ArrayList;
import java.util.List;

/**
Bitbucket authentication type.
*/
public enum BitbucketAuthenticationType {

	/**
	App password.
	*/
	APP_PASSWORD ( "AppPassword", "Application password" );

	// TODO smalers 2025-05-11 enable later.
	/**
	OAuth 2.0.
	*/
	//OAUTH2 ( "OAuth2.0", "OAuth 2.0" );

	/**
	The name that is used for choices and other technical code (terse).
	*/
	private final String name;

	/**
	The description, useful for UI notes.
	*/
	private final String description;

	/**
	Construct an enumeration value.
	@param name name that should be displayed in choices, etc.
	@param descritpion command description.
	*/
	private BitbucketAuthenticationType ( String name, String description ) {
    	this.name = name;
    	this.description = description;
	}

	/**
	Get the list of command types, in appropriate order.
	@return the list of command types.
	*/
	public static List<BitbucketAuthenticationType> getChoices() {
    	List<BitbucketAuthenticationType> choices = new ArrayList<>();
    	choices.add ( BitbucketAuthenticationType.APP_PASSWORD );
    	return choices;
	}

	/**
	Get the list of command type as strings.
	@return the list of command types as strings.
	@param includeNote Currently not implemented.
	*/
	public static List<String> getChoicesAsStrings( boolean includeNote ) {
    	List<BitbucketAuthenticationType> choices = getChoices();
    	List<String> stringChoices = new ArrayList<>();
    	for ( int i = 0; i < choices.size(); i++ ) {
        	BitbucketAuthenticationType choice = choices.get(i);
        	String choiceString = "" + choice;
        	//if ( includeNote ) {
            //	choiceString = choiceString + " - " + choice.toStringVerbose();
        	//}
        	stringChoices.add ( choiceString );
    	}
    	return stringChoices;
	}

	/**
	Get the description.
	@return the enumeration description.
	*/
	public String getDescription() {
    	return this.description;
	}

	/**
	Return the command name for the type.  This is the same as the value.
	@return the display name.
	*/
	@Override
	public String toString() {
    	return this.name;
	}

	/**
	Return the enumeration value given a string name (case-independent).
	@param name the name to match
	@return the enumeration value given a string name (case-independent), or null if not matched.
	*/
	public static BitbucketAuthenticationType valueOfIgnoreCase (String name) {
	    if ( name == null ) {
        	return null;
    	}
    	BitbucketAuthenticationType [] values = values();
    	for ( BitbucketAuthenticationType t : values ) {
        	if ( name.equalsIgnoreCase(t.toString()) )  {
            	return t;
        	}
    	}
    	return null;
	}

}