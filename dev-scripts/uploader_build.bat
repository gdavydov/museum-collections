call %~dp0\env.bat
pushd %~dp0\..\uploader-plus-master
	call mvn package
	copy /Y	repo\target\uploader-plus-repo.amp %ALF_HOME%\amps
	copy /Y	surf\target\uploader-plus-surf.amp %ALF_HOME%\amps_share
popd
