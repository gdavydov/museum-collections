package org.alfresco.museum.ucm;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DATA_KEY_SEPARATOR;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import java.util.Collections;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

public class UCMConstants {
	
	public static final String UCM_NAMESPACE = "http://www.ucm.org/model/1.0";

	public static final String ALFRESCO_NAMESPACE = "http://www.alfresco.org/model/content/1.0"; 
	public static final String PROP_CM_CMOBJECT = "cmobject";
	public static final QName PROP_CM_CMOBJECT_QNAME = QName.createQName(ALFRESCO_NAMESPACE, PROP_CM_CMOBJECT);
	public static final String PROP_NODE_CMNAME = "name";
	public static final QName PROP_CM_CMOBJECTNAME_QNAME = QName.createQName(ALFRESCO_NAMESPACE, PROP_NODE_CMNAME);
	public static final String PROP_UCM_SITE = "site";
	public static final QName PROP_UCM_SITE_QNAME = QName.createQName(ALFRESCO_NAMESPACE, PROP_UCM_SITE);
//	public static final QName PROP_UCM_SITE_QNAME = QName.createQName(UCM_NAMESPACE, PROP_UCM_SITE);
	

	public static final String CONTENT_PROP_DATA = PROP_DATA_PREFIX + "cm" + DATA_KEY_SEPARATOR
			+ ContentModel.PROP_CONTENT.getLocalName();
	
	public static final String NAME_PROP_DATA = PROP_DATA_PREFIX + "cm" + DATA_KEY_SEPARATOR
			+ ContentModel.PROP_NAME.getLocalName();

	public static final String DEFAULT_CONTENT_MIMETYPE = "image/jpeg";

	public static final String PROP_UCM_ARTIST = "artist_name";
	public static final QName PROP_UCM_ARTIST_QNAME = QName.createQName(UCM_NAMESPACE, PROP_UCM_ARTIST);

	public static final String PROP_UCM_ARTIST_ARTIFACT = "about_artist_artifact_reference";
	public static final QName PROP_UCM_ARTIST_ARTIFACT_QNAME = QName.createQName(UCM_NAMESPACE,
			PROP_UCM_ARTIST_ARTIFACT);

	public static final String TYPE_UCM_ARTIFACT = "artifact";
	public static final QName TYPE_UCM_ARTIFACT_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_ARTIFACT);

	public static final String TYPE_UCM_ARTIST = "artist";
	public static final QName TYPE_UCM_ARTIST_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_ARTIST);

	public static final String TYPE_UCM_ARTIST_ARTIFACT = "about_artist_artifact";
	public static final QName TYPE_UCM_ARTIST_ARTIFACT_QNAME = QName.createQName(UCM_NAMESPACE,
			TYPE_UCM_ARTIST_ARTIFACT);

	public static final String ASSOC_UCM_ARTIFACT_CONTAINS = "artifact_contains";
	public static final QName ASSOC_UCM_ARTIFACT_CONTAINS_QNAME = QName.createQName(UCM_NAMESPACE,
			ASSOC_UCM_ARTIFACT_CONTAINS);

	public static final String TYPE_UCM_DOCUMENT = "document";
	public static final QName TYPE_UCM_DOCUMENT_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_DOCUMENT);

	public static final String SYSTEM_FOLDER_NAME = "system";

	public static final String MEDIA_FOLDER_NAME = "artifact_attachments";

	public static final String MANDATORY_PROP_FILLER = "N/A";

	public static final String SITE_TYPE = "st:site";
	public static final String TYPE_UCM_CONFIG = "cm:document";
	public static final String UCM_CONFIG_FILE_NAME = "cm:ucm-config.xml";
	
	public static final QName UCM_CONFIG_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_CONFIG);
		
	public static final String ASPECT_GEOGRAPHICAL = ContentModel.ASPECT_GEOGRAPHIC.getLocalName();
	public static final QName ASPECT_GEOGRAPHICAL_QNAME = ContentModel.ASPECT_GEOGRAPHIC;
	
	public static final String TYPE_UCM_MEDIA_ATTACHMENT = "attached_file";
	public static final QName TYPE_UCM_MEDIA_ATTACHMENT_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_MEDIA_ATTACHMENT);
	
	public static final String TYPE_UCM_FOLDER = "folder";
	public static final QName TYPE_UCM_FOLDER_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_FOLDER);
	
	public static final String TYPE_UCM_COLLECTION = "collection";
	public static final QName TYPE_UCM_COLLECTION_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_COLLECTION);
	
	public static final String PROP_UCM_COLLECTION_ID = "collection_id";
	public static final QName PROP_UCM_COLLECTION_ID_QNAME = QName.createQName(UCM_NAMESPACE, PROP_UCM_COLLECTION_ID);
	
//	public static final String PROP_UCM_COLLECTION_NAME = "collection_name";
//	public static final QName PROP_UCM_COLLECTION_NAME_QNAME = QName.createQName(UCM_NAMESPACE, PROP_UCM_COLLECTION_NAME);
	
	public static final String TYPE_UCM_SITE = "site";
	public static final QName TYPE_UCM_SITE_QNAME = QName.createQName(UCM_NAMESPACE, TYPE_UCM_SITE);
	
	public static final String ASPECT_SITE = "site_aspect";
	public static final QName ASPECT_SITE_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_SITE);
	
	public static final String ASPECT_PROP_UCM_SITE_NAME = "site_name";
	public static final QName ASPECT_PROP_UCM_SITE_NAME_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_NAME);
	
	public static final String ASPECT_PROP_UCM_SITE_ADDRESS = "site_address";
	public static final QName ASPECT_PROP_UCM_SITE_ADDRESS_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ADDRESS);
	
	public static final String ASPECT_PROP_UCM_SITE_BUILD_YEAR = "site_buildYear";
	public static final QName ASPECT_PROP_UCM_SITE_BUILD_YEAR_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_BUILD_YEAR);
	
	public static final String ASPECT_PROP_UCM_SITE_ASPECT_TYPE = "site_aspect_type";
	public static final QName ASPECT_PROP_UCM_SITE_ASPECT_TYPE_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ASPECT_TYPE);
	
	public static final String ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PERSON = "site_aspect_contact_person";
	public static final QName ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PERSON_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PERSON);
	
	public static final String ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_TWEED = "site_aspect_contact_tweed";
	public static final QName ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_TWEED_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_TWEED);
	
	public static final String ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_EMAIL = "site_aspect_contact_email";
	public static final QName ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_EMAIL_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_EMAIL);
	
	public static final String ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PHONE = "site_aspect_contact_phone";
	public static final QName ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PHONE_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_PHONE);
	
//	public static final String ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_FAX = "site_aspect_contact_fax";
//	public static final QName ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_FAX_QNAME = QName.createQName(UCM_NAMESPACE, ASPECT_PROP_UCM_SITE_ASPECT_CONTACT_FAX);
	
	public static final Set<QName> NOT_SYNC_PROPERTIES = Collections.singleton(PROP_UCM_ARTIST_ARTIFACT_QNAME);
}
