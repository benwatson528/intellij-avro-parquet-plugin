# IntelliJ Avro Plugin

## Overview

A Tool Window plugin for IntelliJ that displays Avro `.avro` files in human-readable JSON format.

Features include:
 - Foldable schema viewing ([RSyntaxTextArea](https://github.com/bobbylight/RSyntaxTextArea))
 - Tablulated or raw viewing of records
 - Column sorting in table view
 - Automatic flattening of nested records in table view ([json-flattener](https://github.com/wnameless/json-flattener))
 - Configurable number of records to show
 - Simple drag-and-drop interface
 - Automatic support for IntelliJ's default and Darcula themes


## Installing

Following a [JetBrains policy change](https://blog.jetbrains.com/platform/2018/05/legal-news-plugin-license-is-required-for-all-plugins/),
it is not possible for me to upload this plugin to the JetBrains plugin repo. As such, installation is manual. 

1. [Download the plugin](https://github.com/benwatson528/intellij-avro-plugin/raw/master/releases/intellij-avro-viewer-1.0.0.zip) (or check out and build this project yourself).
2. In IntelliJ, go to `File -> Settings -> Plugins -> settings cog -> Install Plugin from Disk...` and select the
downloaded zip.
3. After a restart, the `Avro Viewer` Tool Window should be visible at the bottom of IntelliJ.


## Images

![table view](images/table-view.png "Table view")

![raw view](images/raw-view.png "Raw view")

![schema view](images/schema-view.png "Schema view")


## Improvements/New Features

Raise a [PR](https://github.com/benwatson528/intellij-avro-plugin/pulls) or [Issue](https://github.com/benwatson528/intellij-avro-plugin/issues).
