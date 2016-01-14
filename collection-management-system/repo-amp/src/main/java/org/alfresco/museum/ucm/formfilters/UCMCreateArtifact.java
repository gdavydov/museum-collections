package org.alfresco.museum.ucm.formfilters;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

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
		boolean isArtifact = item.getName().equals(UCMConstants.TYPE_UCM_ARTIFACT_QNAME);
		if (isArtifact) {
			resolvePossibleFilenameConflict(item, data);
		}
	}

	/**
	 * Store "cm:content" property value, which is ignored by default handler.<br/>
	 * Create "media" folder as an attachment.
	 */
	@Override
	public void afterPersist(TypeDefinition item, FormData data, NodeRef persistedObject) {
		boolean isArtifact = item.getName().equals(UCMConstants.TYPE_UCM_ARTIFACT_QNAME);
		if (isArtifact) {
			writeContent(item, data, persistedObject);
			// artifact width should be no less than 510 px
			this.resizeImage(persistedObject, 510);

			getOrCreateArtistMediaFolder(persistedObject);

			//set ucm:artifact_name
			Serializable name = this.getNodeService().getProperty(persistedObject, ContentModel.PROP_NAME);
			if (name != null) {
				this.getNodeService().setProperty(persistedObject, UCMConstants.PROP_UCM_ARTIFACT_QNAME, name);
			}
		}
	}
}
