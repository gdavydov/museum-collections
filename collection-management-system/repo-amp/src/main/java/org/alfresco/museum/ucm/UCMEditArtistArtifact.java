package org.alfresco.museum.ucm;

import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIST_ARTIFACT_QNAME;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handle artist artifact editing. Artist artifact properties should be
 * synchronized with artist properties.
 */
public class UCMEditArtistArtifact extends UCMGenericFilter<NodeRef> {
	@SuppressWarnings("unused")
	private static Log LOGGER = LogFactory.getLog(UCMEditArtistArtifact.class);

	/**
	 * Filter out non-artist artifact nodes.
	 */
	@Override
	public void afterPersist(NodeRef item, FormData data, NodeRef persistedObject) {
		QName nodeType = this.getNodeService().getType(persistedObject);
		boolean isArtistArtifact = nodeType.equals(TYPE_UCM_ARTIST_ARTIFACT_QNAME);
		if (isArtistArtifact) {
			TypeDefinition artistArtifactType = this.getDictionaryService().getType(
					UCMConstants.TYPE_UCM_ARTIST_ARTIFACT_QNAME);
			writeContent(artistArtifactType, data, persistedObject);
			updateArtist(data, persistedObject);
		}
	}

	/**
	 * Retroactively update artist if about artist got changed.
	 * 
	 * @param data
	 * @param artistArtifactRef
	 */
	protected void updateArtist(FormData data, NodeRef artistArtifactRef) {
		// TODO: LOG
		NodeRef artistNodeRef = this.getNodeService().getPrimaryParent(artistArtifactRef).getParentRef();
		// TODO: what to do with lat/lon?
		super.getUtils().synchronizeUCMPropertyValues(artistArtifactRef, artistNodeRef, UCMConstants.NOT_SYNC_PROPERTIES);
	}
}
