call %~dp0\env.bat

pushd %SCRIPTPATH%\..\collection-management-system
	pushd ucm-repo-amp
		call mvn package && copy /Y target\collection-management-system-repo-amp.amp %ALF_HOME%\amps
	popd
	pushd ucm-share-amp
		call mvn package && copy /Y target\collection-management-system-share-amp.amp %ALF_HOME%\amps_share
	popd
popd
