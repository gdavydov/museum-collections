call %~dp0\env.bat

pushd ..\collection-management-system\runner
mvn jetty:run -Prun