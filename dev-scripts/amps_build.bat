call %~dp0\env.bat

pushd %SCRIPTPATH%\..\collection-management-system
	pushd repo-amp
		call mvn clean && call mvn package && copy /Y target\collection-management-system-repo-amp.amp ..\..\release_amps\
	popd
	pushd share-amp
		call mvn clean && call mvn package && copy /Y target\collection-management-system-share-amp.amp ..\..\release_amps\
	popd
popd
pause