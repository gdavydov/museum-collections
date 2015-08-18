package org.alfresco.museum.ucm;

import static org.alfresco.repo.forms.processor.node.FormFieldConstants.DATA_KEY_SEPARATOR;
import static org.alfresco.repo.forms.processor.node.FormFieldConstants.PROP_DATA_PREFIX;

import java.util.Collections;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

public class UCMConstants {
	public static final String UCM_NAMESPACE = "http://www.ucm.org/model/1.0";

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

	public static final String SYSTEM_FOLDER_NAME = "system";

	public static final String MEDIA_FOLDER_NAME = "artifact_attachments";

	public static final String MANDATORY_PROP_FILLER = "N/A";

	public static final String ASPECT_GEOGRAPHICAL = ContentModel.ASPECT_GEOGRAPHIC.getLocalName();
	public static final QName ASPECT_GEOGRAPHICAL_QNAME = ContentModel.ASPECT_GEOGRAPHIC;

	public static final Set<QName> NOT_SYNC_PROPERTIES = Collections.singleton(PROP_UCM_ARTIST_ARTIFACT_QNAME);
}
