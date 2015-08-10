call %~dp0\env.bat

pushd %SCRIPTPATH%\..\collection-management-system\solr\solr_home
	RMDIR workspace\SpacesStore\ /S /Q
	RMDIR archive\SpacesStore\ /S /Q

	RMDIR workspace-SpacesStore\alfrescoModels /S /Q
	RMDIR archive-SpacesStore\alfrescoModels /S /Q
popd
