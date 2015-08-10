call %~dp0\env.bat

pushd %ALF_HOME%
	call servicerun.bat START
popd