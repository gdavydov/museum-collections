call %~dp0\env.bat
pushd %ALF_HOME%\bin
	call apply_amps.bat nowait
popd
