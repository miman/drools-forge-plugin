drools-prj-plugin
=================

JBoss Forge plugin to modify a project to be a JBoss Drools project

This plugin is used to add the dependencies for JBoss Drools and Spring to the pom, it also creates a Drools config file.

Usage of the plugin commands
----------------------------

### Command: setup
The setup command adds the Maven dependencies and Maven plugins needed for adding JBoss Drools rules to the project.

It also adds the rules folder where the rules should be placed (**${prj-root}\src\main\resources\rules**)

### Command: add-example-rule

This command creates an example rule to the project.
It also adds an example DTO & a test JUnit class for this rule.

OBS ! - Needed Maven project dependency
---------------------------------------
This project has a dependency to the **miman-forge-plugin-util** project.
So to build this project you must download (and build) version 1.0.1 of this project first
[Found here](https://github.com/mikaelth/miman-forge-plugin-util)
