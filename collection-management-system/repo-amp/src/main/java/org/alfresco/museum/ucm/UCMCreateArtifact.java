package org.alfresco.museum.ucm;

import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIFACT_QNAME;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Handle custom operations during artifact creation. Such as creating folder
 * for attachments and setting image content as artifact property.
 */
public class UCMCreateArtifact extends UCMGenericFilter<TypeDefinition> {
	/**
	 * Fill file name field. Handle possible file name collisions.
	 */
	@Override
	public void beforePersist(TypeDefinition item, FormData data) {
		resolvePossibleFilenameConflict(item, data);
	}

	/**
	 * Strore "cm:content" property value, which is ignored by default handler.<br/>
	 * Create "media" folder as an attachment.<br/>
	 * See
	 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#persistNode(NodeRef, FormData)
	 * persistNode},
	 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#processPropertyPersist(NodeRef, Map,FieldData, Map, FormData)
	 * processPropertyPersist},
	 * {@link org.alfresco.repo.forms.processor.node.ContentModelFormProcessor#processContentPropertyPersist(NodeRef, FieldData, Map, FormData)
	 * processContentPropertyPersist} and <a href=
	 * "https://forums.alfresco.com/forum/developer-discussions/alfresco-share-development/file-upload-create-content-06282010-2333"
	 * >discussion thread</a>
	 */
	@Override
	public void afterPersist(TypeDefinition item, FormData data, NodeRef persistedObject) {
		writeContent(item, data, persistedObject);
		boolean isArtifact = item.getName().equals(TYPE_UCM_ARTIFACT_QNAME);
		if (isArtifact) {
			getOrCreateArtistMediaFolder(persistedObject);
		}
	}
}
