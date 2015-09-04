#!/bin/sh
INSTALLDIR=/Applications/alfresco-5.0.d
GITROOT=/Users/`whoami`/GitHub/
LOCALAMPDIR = $GITROOT/museum-collections/collection-management-system
export ALF_HOME=$INSTALLDIR
export CATALINA_HOME=$ALF_HOME/tomcat

cd $INSTALLDIR/amps
#wget https://github.com/gdavydov/museum-collections/raw/master/release_amps/collection-management-system-repo-amp.amp -O collection-management-system-repo-amp.amp
#wget https://github.com/gdavydov/museum-collections/raw/master/release_amps/uploader-plus-repo-1.2.amp -O uploader-plus-repo-1.2.amp
cp $LOCALAMPDIR/repo-amp/target/collection-management-system-repo-amp.amp .
cp $LOCALAMPDIR/share-amp/target/collection-management-system-share-amp.amp .


if [ ! -d "$INSTALLDIR/wars.orig" ]; then
	mkdir $INSTALLDIR/wars.orig
	cp $INSTALLDIR/tomcat/webapps/*.war $INSTALLDIR/wars.orig
fi

$INSTALLDIR/alfresco.sh stop

$INSTALLDIR/bin/clean_tomcat.sh
rm $CATALINA_HOME/webapps/*.war
rm -rf $CATALINA_HOME/webapps/alfresco
rm -rf $CATALINA_HOME/webapps/share

cp $INSTALLDIR/wars.orig/*.war $CATALINA_HOME/webapps/

# Verify Java installation into ALF_HOME folder
if [ -f $ALF_HOME/java/bin/java ] && [ -x $ALF_HOME/java/bin/java ]; then
    echo
    echo "Found java executable in $ALF_HOME/java"
    _java=$ALF_HOME/java/bin/java

# Verify Java installation into JAVA_HOME
elif [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    echo
    echo "Found java executable in JAVA_HOME: $JAVA_HOME"

    _java="$JAVA_HOME/bin/java"
cd 
# Verify Java installation from linux repositories
elif type -p java;  then
    echo
    echo "Found installed java executable on the system"

    _java=java

else
    echo
    echo "Java is not installed . . . The required Java version is $required_java_version or higher"
    echo "Please install Java and try again. Script will be closed. "
    read DUMMY
    exit 15
fi

{
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar install $ALF_HOME/amps $CATALINA_HOME/webapps/alfresco.war -directory -force
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar list $CATALINA_HOME/webapps/alfresco.war
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar install $ALF_HOME/amps_share $CATALINA_HOME/webapps/share.war -directory -force
$_java -jar $ALF_HOME/bin/alfresco-mmt.jar list $CATALINA_HOME/webapps/share.war
} ||
{
    echo
    echo "Error. Applying of the AMPs is failed. See error message above."
    echo
}

$INSTALLDIR/alfresco.sh start
