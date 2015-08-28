#!/bin/bash
ALF_MMT="$JAVA_HOME/bin/java -jar alfresco-mmt.jar"
SCRIPTPATH="$(cd "$(dirname "$0")" && pwd -P)"
ROOT="$SCRIPTPATH/../"
RELEASE="$ROOT/release"
mkdir $RELEASE

set -e

cd $ROOT/collection-management-system/repo
mvn install
cp -f target/collection-management-system-repo.war $RELEASE

cd $ROOT/collection-management-system/share
mvn install
cp -f target/collection-management-system-share.war $RELEASE

cd $ROOT/collection-management-system/solr
mvn install
cp -f target/collection-management-system-solr.war $RELEASE

cp -f $ROOT/uploader-plus-master/release/*.amp $RELEASE

cd $SCRIPTPATH
"$JAVA_HOME/bin/java" -jar alfresco-mmt.jar install "$RELEASE/uploader-plus-repo-1.2.amp" "$RELEASE/collection-management-system-repo.war" -force
"$JAVA_HOME/bin/java" -jar alfresco-mmt.jar install "$RELEASE/uploader-plus-surf-1.2.amp" "$RELEASE/collection-management-system-share.war" -force

rm $RELEASE/*.amp
#read -p "Press any key..."
