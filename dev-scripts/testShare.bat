pushd ..\collection-management-system\share
	call mvn integration-test -Prun -Dmaven.tomcat.port=8081
popd