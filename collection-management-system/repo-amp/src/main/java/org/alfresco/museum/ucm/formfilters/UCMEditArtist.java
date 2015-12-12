package org.alfresco.museum.ucm.formfilters;

import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIST_QNAME;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

/**
 * Handle artist update operation. Image selected by user should update artist artifact object instead of artist itself.
 */
public class UCMEditArtist extends UCMGenericFilter<NodeRef> {
	public boolean isArtist(NodeRef node) {
		QName nodeType = this.getNodeService().getType(node);
		return nodeType.equals(TYPE_UCM_ARTIST_QNAME);
	}

	@Override
	public void afterGenerate(NodeRef item, List<String> fields, List<String> forcedFields, Form form, Map<String,Object> context) {
		if (isArtist(item)) {
			Serializable artistArtifactNodeRef = this.getNodeService().getProperty(item, UCMConstants.PROP_UCM_ARTIST_ARTIFACT_QNAME);
			form.getFormData().addFieldData("prop_cm_content", artistArtifactNodeRef.toString(), true);
		}
	}

	/**
	 * Store "cm:content" property value, which is ignored by default handler.
	 */
	@Override
	public void afterPersist(NodeRef item, FormData data, NodeRef persistedObject) {
		if (isArtist(item)) {
			Object artistArtifactValue = this.getNodeService().getProperty(persistedObject, UCMConstants.PROP_UCM_ARTIST_ARTIFACT_QNAME);
			if (!StringUtils.isEmpty(artistArtifactValue)) {
				String artistArtifactString = artistArtifactValue.toString();
				NodeRef artistArtifactRef = new NodeRef(artistArtifactString);
				TypeDefinition artistArtifactType = this.getDictionaryService().getType(
						UCMConstants.TYPE_UCM_ARTIST_ARTIFACT_QNAME);
				writeContent(artistArtifactType, data, artistArtifactRef, false);
			}
		}
	}
}
