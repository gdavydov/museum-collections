call %~dp0\env.bat

pushd %~dp0
	call alfresco_stop.bat

	call amps_build.bat
	rem call uploader_build.bat
	call amps_clean.bat
	call amps_install.bat
	
	call alfresco_start.bat
popd
pause