# TSTool / Troubleshooting #

Troubleshooting TSTool for Bitbucket involves confirming that the core product and plugin are performing as expected.
Issues may also be related to Bitbucket data.

*   [Troubleshooting Core TSTool Product](#troubleshooting-core-tstool-product)
*   [Troubleshooting Bitbucket TSTool Integration](#troubleshooting-bitbucket-tstool-integration)
    +   [***Commands(Plugin)*** Menu Contains Duplicate Commands](#commandsplugin-menu-contains-duplicate-commands)
    +   [Web Service Datastore Returns no Data](#web-service-datastore-returns-no-data)

------------------

## Troubleshooting Core TSTool Product ##

See the main [TSTool Troubleshooting documentation](https://opencdss.state.co.us/tstool/latest/doc-user/troubleshooting/troubleshooting/).

## Troubleshooting Bitbucket TSTool Integration ##

The following sections summarize typical issues that are encountered when using TSTool with the Bitbucket plugin.
Use the following as resources for troubleshooting:

*   The TSTool ***View / Datastores*** menu item displays the status of datastores.
*   The TSTool ***Tools / Diagnostics - View Log File...*** menu item displays the log file.
    A text editor can also be usd to edit the log file.
*   Set the `Debug=True` property in the
    [datastore configuration](../datastore-ref/Bitbucket/Bitbucket.md#datastore-configuration-file) to turn on more logging messages.
    If the API changes, an error message is typically returned that indicates the problem.

### ***Commands(Plugin)*** Menu Contains Duplicate Commands ###

If the ***Commands(Plugin)*** menu contains duplicate commands,
TSTool is finding multiple plugin `jar` files.
To fix, check the `plugins` folder and subfolders for the software installation folder
and the user's `.tstool/NN/plugins` folder.
Remove extra jar files, leaving only the version that is desired (typically the most recent version).

### Web Service Datastore Returns no Data ###

If the web service datastore returns no data, check the following:

1.  Review the TSTool log file for errors.
    Typically a message will indicate an HTTP error code for the URL that was requested.
2.  Copy and paste the URL into a web browser to confirm the error.
    The browser will typically show a specific web service error message such as a
    missing query parameter or typo.
    Additional authentication may be required.
3.  See the [Bitbucket API documentation](https://developer.atlassian.com/cloud/bitbucket/rest/)
    to check whether the URL is correct.
4.  Add `Debug=True` to the datastore configuration file and restart TSTool.
    Look for API errors and possible changes to the API that require software changes.

If the issue cannot be resolved, contact the [Open Water Foundation](https://openwaterfoundation.org/about-owf/staff/).
