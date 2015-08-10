call %~dp0\env.bat
echo REPO
"%JAVA_HOME%\bin\java" -jar "%ALF_HOME%bin\alfresco-mmt.jar" list "%CATALINA_HOME%\webapps\alfresco.war"
echo SHARE
"%JAVA_HOME%\bin\java" -jar "%ALF_HOME%bin\alfresco-mmt.jar" list "%CATALINA_HOME%\webapps\share.war"
pause