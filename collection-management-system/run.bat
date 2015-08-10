REM Downloads the spring-loaded lib if not existing and runs the full all-in-one(Alfresco + Share + Solr) using the runner project
set springloadedfile=%USERPROFILE%\.m2\repository\org\springframework\springloaded\1.2.0.RELEASE\springloaded-1.2.0.RELEASE.jar
if exist %springloadedfile% (
    call mvn validate -Psetup
)
set MAVEN_OPTS=-javaagent:%springloadedfile% -noverify -Xms256m -Xmx2G -XX:PermSize=300m
mvn install -Prun
