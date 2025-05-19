// BitbucketDataStoreFactory - class to create a BitbucketDataStore instance

/* NoticeStart

OWF TSTool Bitbucket Plugin
Copyright (C) 2024-2025 Open Water Foundation

OWF TSTool Bitbucket plugin is free software:  you can redistribute it and/or modify
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

import java.net.URI;

import RTi.Util.IO.PropList;
import RTi.Util.Message.Message;
import riverside.datastore.DataStore;
import riverside.datastore.DataStoreFactory;

public class BitbucketDataStoreFactory implements DataStoreFactory {

	/**
	Create a Bitbucket instance.
	@param props datastore configuration properties, such as read from the configuration file
	*/
	public DataStore create ( PropList props ) {
	    String name = props.getValue ( "Name" );
	    String description = props.getValue ( "Description" );
	    if ( description == null ) {
	        description = "";
	    }
	    // New convention is to use ServiceRootURL
	    String serviceRootURL = props.getValue ( "ServiceRootURL" );
	    if ( serviceRootURL == null ) {
	    	System.out.println("Bitbucket ServiceRootURL is not defined in the datastore configuration file.");
	    }
	    // Workspace and WorkspaceAccessToken are used together to grant access to a workspace.
	    String workspace = props.getValue ( "Workspace" );
	    if ( workspace == null ) {
	    	System.out.println("Bitbucket Workspace is not defined in the datastore configuration file.");
	    }
	    String workspaceAccessToken = props.getValue ( "WorkspaceAccessToken" );
	    if ( workspaceAccessToken == null ) {
	    	System.out.println("Bitbucket WorkspaceAccessToken is not defined in the datastore configuration file.");
	    }
	    try {
	        DataStore ds = new BitbucketDataStore ( name, description, new URI(serviceRootURL), props );
	        return ds;
	    }
	    catch ( Exception e ) {
	        Message.printWarning(3,"",e);
	        throw new RuntimeException ( e );
	    }
	}
}