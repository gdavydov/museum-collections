call %~dp0\env.bat
rem https://forums.alfresco.com/forum/developer-discussions/content-modeling/best-way-uninstall-amp-module-03192008-1313

pushd %CATALINA_HOME%\webapps
	del *.war
	copy wars.bak\*.war .
	
	rmdir /S /Q alfresco
	rmdir /S /Q share
rem	rmdir /S /Q solr4

	rmdir /S /Q ..\temp
	rmdir /S /Q ..\work
popd
