#!/bin/sh
INSTALLDIR=/opt/alfresco-5.0.d
export ALF_HOME=$INSTALLDIR
export CATALINA_HOME=$ALF_HOME/tomcat

### Download AMPs directly from github

cd $INSTALLDIR/amps
wget https://github.com/gdavydov/museum-collections/raw/master/release_amps/collection-management-system-repo-amp.amp -O collection-management-system-repo-amp.amp
wget https://github.com/gdavydov/museum-collections/raw/master/release_amps/uploader-plus-repo-1.2.amp -O uploader-plus-repo-1.2.amp

cd $INSTALLDIR/amps_share
wget https://github.com/gdavydov/museum-collections/raw/master/release_amps/collection-management-system-share-amp.amp -O collection-management-system-share-amp.amp
wget https://github.com/gdavydov/museum-collections/raw/master/release_amps/uploader-plus-surf-1.2.amp -O uploader-plus-surf-1.2.amp

### Stop Alfresco

$INSTALLDIR/alfresco.sh stop

### Keep original untouched .WARs (only once)

if [ ! -d "$INSTALLDIR/wars.orig" ]; then
	mkdir $INSTALLDIR/wars.orig
	cp $INSTALLDIR/tomcat/webapps/*.war $INSTALLDIR/wars.orig
fi

### Store current backups

#rm -rf /opt/alfresco-5.0.d.bak.old
#mv /opt/alfresco-5.0.d.bak /opt/alfresco-5.0.d.bak.old
rm -rf /opt/alfresco-5.0.d.bak
cp -pr /opt/alfresco-5.0.d /opt/alfresco-5.0.d.bak

### Clean up temporary folders

$INSTALLDIR/bin/clean_tomcat.sh
rm $CATALINA_HOME/webapps/*.war
rm -rf $CATALINA_HOME/webapps/alfresco
rm -rf $CATALINA_HOME/webapps/share

### Put original .WARs into working folder

cp $INSTALLDIR/wars.orig/*.war $CATALINA_HOME/webapps/

### Some checks for JVM version (copied from Alfresco script)

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

### Install new AMPs into original .WARs

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

### Start Alfresco

$INSTALLDIR/alfresco.sh start
