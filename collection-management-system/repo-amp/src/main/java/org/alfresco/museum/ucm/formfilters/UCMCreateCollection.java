package org.alfresco.museum.ucm.formfilters;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.museum.ucm.UCMConstants;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Drag-and-dropping file into artist or collection folder should initialize
 * artifact creation. To instruct "Uploader Plus" plugin to do so we should put
 * value "ucm:artifact" into (multivalued) property "up:allowedTypes" of
 * Collections and Artist.
 */
public class UCMCreateCollection extends UCMGenericFilter<TypeDefinition> {

	public static final String UCM_ARTIFACT = UCMConstants.ALFRESCO_NAMESPACE_SHORT + ":"
			+ UCMConstants.TYPE_UCM_ARTIFACT;
	public static final String UCM_ARTIST = UCMConstants.ALFRESCO_NAMESPACE_SHORT + ":" + UCMConstants.TYPE_UCM_ARTIST;

	public static final List<String> ALLOWED_TYPES = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2540740835163382240L;

		{
			this.add(UCM_ARTIFACT);
			//TODO: this.add(UCM_ARTIST);
		}
	};

	/**
	 * Put "ucm:artifact" into property "up:allowedTypes" of ucm:collection and
	 * ucm:Artist.
	 */
	@Override
	public void afterPersist(TypeDefinition item, FormData data, NodeRef persistedObject) {
		boolean isCollection = this.getDictionaryService().isSubClass(item.getName(),
				UCMConstants.TYPE_UCM_COLLECTION_QNAME);
		if (isCollection) {
			afterCreateCollection(persistedObject, this.getNodeService());
		}
	}
	
	public static void afterCreateCollection(NodeRef collection, NodeService nodeService) {
		nodeService.setProperty(collection, UCMConstants.PROP_UPLOADER_ALLOWED_TYPES, (Serializable) ALLOWED_TYPES);
	}
}
