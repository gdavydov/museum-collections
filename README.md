###Introduction
Goal of this project is to create custom Collection management system based on Alfresco content-management system which would be specialized in working with museum artifact collections. It is based on Alfresco SDK, more specifically on "All-in-one" archetype. See http://docs.alfresco.com/5.0/concepts/alfresco-sdk-archetypes.html for details.

###Structure
* `collection-management-system`: contains main Maven project and child Maven project
* `collection-management-system/repo-amp`: A Repository Tier AMP project, demonstrating sample project structure and demo component loading.
* `collection-management-system/repo`: An alfresco.war Repository Extension, overlaying the Alfresco WAR with custom resources / classes and depending on the amp project
* `collection-management-system/share-amp`: A Share Tier AMP project, demonstrating sample project structure and demo component loading.
* `collection-management-system/share`: A share.war extension, overlaying the Share WAR with the custom developed share-amp
* `collection-management-system/solr`: An Alfresco alfresco-\*-\*-solr.zip overlay / customization to configure Apache Solr cores properties
* `collection-management-system/runner`: A Tomcat + H2 runner, capable of running all the aforementioned projects in embedded mode for demo / integration-testing purposes

'repo' and 'share' projects may be used to override global configuration files.
'repo-amp' and 'share-amp' are module projects and so they shouldn't contain files whose name would clash with global configuration files. Otherwise conflict would be possible between this modules and third-party modules.

Many Alfresco tutorials refer to location of configuration files in tomcat directory. Below is mapping between paths inside projects and their target destination in application server directory:
* `repo/src/main/resources/alfresco` -> 'tomcat/webapps/**repo**/WEB-INF/classes/alfresco'
* `repo-amp/src/main/amp/config/alfresco` -> 'tomcat/webapps/**repo**/WEB-INF/classes/alfresco'
* `share/src/main/resources/alfresco` -> 'tomcat/webapps/**share**/WEB-INF/classes/alfresco'
* `share-amp/src/main/amp/config/alfresco` -> 'tomcat/webapps/**share**/WEB-INF/classes/alfresco'
* `share-amp/src/main/resources` -> 'tomcat/webapps/**share**'

Description of main _share_ configuration files may be found in http://docs.alfresco.com/5.0/concepts/share-configuration-files.html

###Build prerequisites
This page lists system requirements for building project: http://docs.alfresco.com/5.0/concepts/alfresco-sdk-install.html
In short:
* JDK >= 1.7
* Maven >= 3.2.5
* Environmental variables JAVA_HOME and MAVEN_OPTS set.

###Build and run
Command line script run.bat (run.sh for Unix) may be used to build project and start it. Maven command line arguments used to build/run/package project may be found here: http://docs.alfresco.com/5.0/concepts/alfresco-sdk-usage-aio.html

###Building distribution WAR files
Run `mvn package` command in 'collection-management-system' directory. Three WAR files will be created:
* collection-management-system/repo/target/collection-management-system-**repo**.war
* collection-management-system/share/target/collection-management-system-**share**.war
* collection-management-system/solr/target/collection-management-system-**solr**.war

You need all of them to be deployed to application server.

###Installation of "Uploader-plus" plugin
[Uploader plus](http://softwareloop.com/uploader-plus-an-alfresco-uploader-that-prompts-for-metadata/) is a freeware Alfresco Share plugin. It adds support for creating new content of custom types by simple drag-and-dropping file to Document library page. It is also possible to fill in property fields of content during creation, so you can create content even if type definition contains obligatory properties. Such as 'ucm:artifact'. While using this plugin isn't compulsory it provides convenient way to create content.
1.  You can get plugin distribution files either by downloading AMP files https://github.com/softwareloop/uploader-plus/archive/v1.2.zip or by building them manually. In order to build it you should run `mvn package` in 'uploader-plus-master' directory. Following AMP files will be created:
  * 'uploader-plus-master/repo/target/uploader-plus-**repo**.amp'
  * 'uploader-plus-master/surf/target/uploader-plus-**surf**.amp'
2. Next step is to apply AMP files to corresponding WAR files. You could think of AMP files as patches and WAR files as a code base. Procedure of installation is described in [official documentation](http://docs.alfresco.com/5.0/tasks/amp-install.html).
You will need `alfresco-mmt.jar` utility which is bundled with alfresco distributuion.
3. Set environment variable `ALF_HOME` to the root of your Alfresco installation.
4. Place Collection management system WAR files and Uploader-plus AMP files in current directory.
5. Apply AMP files to WAR files:
  * java -jar "$ALF_HOME/bin/alfresco-mmt.jar" install uploader-plus-**repo**.amp collection-management-system-repo.war -force
  * java -jar "$ALF_HOME/bin/alfresco-mmt.jar" install uploader-plus-**surf**.amp collection-management-system-share.war -force
6. Deploy patched WAR files (along with collection-management-system-**solr**.war) to application server.
7. Configure plugin using "Tools > Application > Uploader Plus" entry at "Admin tools" page. You should select required folder (e.g. your site root folder) and data types (e.g. "Artifact") which will be created. This is only temperary measure. In future plugin preferences will be set up automatically.

###External utilities setup
Alfresco Share uses external utilities called ImageMagick and Ghostscript to create file thumbnails in Document library.
You should manually install this utilities and configure Alfresco to use it. Otherwise you will see errors like

    2015-06-10 19:00:23,192  ERROR [transform.magick.AbstractImageMagickContentTransformerWorker] [localhost-startStop-1] 
    ImageMagickContentTransformerWorker not available: 05100001 Failed to perform ImageMagick transformation: 
    Execution result: 
       os:         <Your OS name>
       command:    ./ImageMagick/bin/convert
in logs. Also there will be no file thumbnails in Document library. Configuration file you need is `collection-management-system/repo/src/main/properties/local/alfresco-global.properties`.
Detailed instructions on Alfresco configuration may be found here: http://docs.alfresco.com/5.0/tasks/imagemagick-config.html

###Developement tips
If you have configured Eclipse according to [this instructions](http://docs.alfresco.com/sdk2.0/tasks/alfresco-sdk-rad-eclipse-share-project.html) you can run Alfresco in debug mode and use "hot reloading" feature. It allows to change webscripts implementation in Java or JS without need to restart Alfresco. See detailed [hot reloading example  ](http://docs.alfresco.com/5.0/tasks/alfresco-sdk-rad-eclipse-hot-reloading.html). If you have changed not just implementation of webscripts, but also it's structure or created new one you should force reloading of webscripts. This is achieved by clicking "Refresh Web Scripts" on webscripts service page: [Share service page](http://localhost:8080/share/service/index) or [Repo service page](http://localhost:8080/alfresco/service/index), depending on which webscript you want to reload. In this way form and page templates (and most other .ftl files) could be hot-reloaded. Configuration and use of all this features is shown in [this video](https://www.youtube.com/watch?v=utYZaVe9Nd0).

You still may want to restart Alfresco if you have changed XML configs, which aren't related to webscripts, such as `share-config-custom.xml`. If you have updated static resources, like icons, .css or .js files, restarting alfresco may be not enough. You may need to call `mvn clean -Ppurge` after stopping Alfrecsco and before rebuilding it.

###Content model deployment
Custom content model file may be added by path `collection-management-system/repo-amp/src/main/amp/config/alfresco/extension/model`. It should be then registered in Spring config file, e.g. `collection-management-system-clean/repo-amp/src/main/amp/config/alfresco/module/repo-amp/module-context.xml`. For details see https://wiki.alfresco.com/wiki/Data_Dictionary_Guide#Model_Bootstrapping