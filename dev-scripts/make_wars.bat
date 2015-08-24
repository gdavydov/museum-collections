call %~dp0\env.bat

set RELEASE=%SCRIPTPATH%\..\release
mkdir %RELEASE%

pushd %SCRIPTPATH%\..\collection-management-system
	pushd repo
		call mvn install
		copy /Y target\collection-management-system-repo.war %RELEASE%
	popd
	pushd share
		call mvn install
		copy /Y target\collection-management-system-share.war %RELEASE%
	popd
	pushd solr
		call mvn install
		copy /Y target\collection-management-system-solr.war %RELEASE%
	popd
popd

pushd %SCRIPTPATH%\..\uploader-plus-master
rem 	call mvn install
	copy /Y release\*.amp %RELEASE%
popd

pushd %SCRIPTPATH%
	"%JAVA_HOME%\bin\java" -jar alfresco-mmt.jar install "%RELEASE%\uploader-plus-repo-1.2.amp" "%RELEASE%\collection-management-system-repo.war" -force
	rem "%JAVA_HOME%\bin\java" -jar alfresco-mmt.jar list "%RELEASE%\collection-management-system-repo.war"
	"%JAVA_HOME%\bin\java" -jar alfresco-mmt.jar install "%RELEASE%\uploader-plus-surf-1.2.amp" "%RELEASE%\collection-management-system-share.war" -force
	rem "%JAVA_HOME%\bin\java" -jar alfresco-mmt.jar list "%RELEASE%\collection-management-system-share.war"
popd

pushd %RELEASE%
	del *.amp
popd
pause