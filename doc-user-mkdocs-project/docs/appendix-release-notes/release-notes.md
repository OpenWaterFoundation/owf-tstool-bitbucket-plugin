# TSTool / Bitbucket Data Web Services Plugin / Release Notes #

Release notes are available for the core TSTool product and plugin.
The core software and plugins are maintained separately and may be updated at different times.
See the [TSTool release notes](http://opencdss.state.co.us/tstool/latest/doc-user/appendix-release-notes/release-notes/).

Plugin release notes are listed below.
The repository issue for release note item is shown where applicable.

*   [Version 1.0.1](#version-101)
*   [Version 1.0.0](#version-100)

----------

## Version 1.0.1 ##

**Maintenance release to clean up the initial features.**

*   Update the [`Bitbucket`](../command-ref/Bitbucket/Bitbucket.md) command:
    +   ![bug](bug.png) [#2] Fix so that the `ListRepositoryIssuesCountProperty` value is actually set
        when listing repository issues.
    +   ![change](change.png) [#4] Change so that date/times are output as a date unless it is the current day.
        Times are typically only needed for recent issues.

## Version 1.0.0 ##

**Feature release - initial production release.**

*   ![new](new.png) [1.0.0] Initial production release:
    +   Main TSTool window includes browsing features to list Bitbucket time series.
    +   The [`Bitbucket`](../command-ref/Bitbucket/Bitbucket.md) command is provided to automate
        reading repository data.
    +   Documentation is available for the [Bitbucket datastore](../datastore-ref/Bitbucket/Bitbucket.md).
