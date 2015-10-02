package org.alfresco.museum.ucm;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Handle media attachment creation.
 */
public class UCMCreateMediaAttachment extends UCMGenericFilter<TypeDefinition> {
	/**
	 * Fill file name field. Handle possible file name collisions.
	 */
	@Override
	public void beforePersist(TypeDefinition item, FormData data) {
		boolean isMedia = item.getName().equals(UCMConstants.TYPE_UCM_MEDIA_ATTACHMENT_QNAME);
		if (isMedia) {
			resolvePossibleFilenameConflict(item, data);
		}
	}

	/**
	 * Store "cm:content" property value, which is ignored by default handler.<br/>
	 * Create "media" folder as an attachment.
	 */
	@Override
	public void afterPersist(TypeDefinition item, FormData data, NodeRef persistedObject) {
		boolean isMedia = item.getName().equals(UCMConstants.TYPE_UCM_MEDIA_ATTACHMENT_QNAME);
		if (isMedia) {
			writeContent(item, data, persistedObject);
		}
	}
}
