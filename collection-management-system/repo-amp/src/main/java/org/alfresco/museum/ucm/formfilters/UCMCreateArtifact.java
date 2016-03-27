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
	 * Create "media" folder as an attachment.<br/>
	 */
	@Override
	public void afterPersist(TypeDefinition item, FormData data, NodeRef artifactRef) {
		boolean isArtifact = item.getName().equals(UCMConstants.TYPE_UCM_ARTIFACT_QNAME);
		if (isArtifact) {
			writeContent(item, data, artifactRef);
			processNewArtifact(artifactRef);
		}
	}

	/**
	 * This method is also called when type of cm:content node is set to ucm:artifact.
	 */
	public void processNewArtifact(NodeRef artifactRef) {
		// artifact width should be no less than 510 px
		this.resizeImage(artifactRef, 510);

		this.getUtils().getOrCreateArtifactMediaFolder(artifactRef);

		//set ucm:artifact_name
		Serializable name = this.getNodeService().getProperty(artifactRef, ContentModel.PROP_NAME);
		if (name != null) {
			this.getNodeService().setProperty(artifactRef, UCMConstants.PROP_UCM_ARTIFACT_QNAME, name);
		}
	}
}
