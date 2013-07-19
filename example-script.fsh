@/* run this by starting a forge console & write 'run example-script.fsh' */;

@/* Clear the screen */;
clear;

@/* This means less typing. If a script is automated, or is not meant to be interactive, use this command */;
set ACCEPT_DEFAULTS true;

@/* Create root project */;
new-project --named test-prj --topLevelPackage se.comp.test;
drools setup
drools add-example-rule --name TestDto --path ~.dto
