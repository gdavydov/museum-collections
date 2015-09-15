set JAXB_HOME=%cd%\jaxb-ri
cd %JAXB_HOME%\bin

xjc -d C:..\..\..\collection-management-system\repo-amp\src\main\java ..\..\..\collection-management-system\repo-amp\src\main\java\org\alfresco\museum\ucm\config\xml\schemas\UCMConfig.xsd -p org.alfresco.museum.ucm.config.autogen
