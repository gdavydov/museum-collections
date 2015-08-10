package org.alfresco.museum.ucm;

import static org.alfresco.museum.ucm.UCMConstants.ASPECT_GEOGRAPHICAL_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.MANDATORY_PROP_FILLER;
import static org.alfresco.museum.ucm.UCMConstants.PROP_UCM_ARTIST_ARTIFACT_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.PROP_UCM_ARTIST_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIST_ARTIFACT_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIST_QNAME;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.FormData.FieldData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StringUtils;

/**
 * Handle custom operations during artist creation. Such as creating 'about'
 * folder and artist artifact, which contains image of artist. Artist artifact
 * extends ucm:artifact and thus is cm:content. Artist node contains reference
 * to artist artifact. This is necessary because artist artifact's thumbnail is
 * used also as thumbnail for artist node.
 */
public class UCMCreateArtist extends UCMGenericFilter<TypeDefinition> {
	/**
	 * Fill artist name field. Handle possible file name collisions.
	 */
	@Override
	public void beforePersist(TypeDefinition item, FormData data) {
		resolvePossibleFilenameConflict(item, data);
	}

	/**
	 * Store "cm:content" property value, which is ignored by default handler.<br/>
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
		boolean isArtist = item.getName().equals(TYPE_UCM_ARTIST_QNAME);
		if (isArtist) {
			Serializable artistNameValue = this.getNodeService().getProperty(persistedObject,
					PROP_UCM_ARTIST_QNAME);
			if (!StringUtils.isEmpty(artistNameValue)) {
				String artistName = artistNameValue.toString();

				NodeRef artistArtifact = createArtistArtifact(data, persistedObject, artistName);

				if (artistArtifact != null) {
					// save reference to artist artifact in artist property
					this.getNodeService().setProperty(persistedObject, PROP_UCM_ARTIST_ARTIFACT_QNAME,
							artistArtifact);
					if (this.getNodeService().getAspects(artistArtifact).contains(ASPECT_GEOGRAPHICAL_QNAME)) {
						Serializable lat = this.getNodeService().getProperty(artistArtifact, ContentModel.PROP_LATITUDE); 
						Serializable lon = this.getNodeService().getProperty(artistArtifact, ContentModel.PROP_LONGITUDE);
						HashMap<QName, Serializable> geoProps = new HashMap<QName, Serializable>();
						geoProps.put(ContentModel.PROP_LATITUDE, lat);
						geoProps.put(ContentModel.PROP_LONGITUDE, lon);
						this.getNodeService().addAspect(persistedObject, ASPECT_GEOGRAPHICAL_QNAME, geoProps);
					}
				}
			}
		}
	}

	// <artist>/"About " + artistName
	protected NodeRef createArtistArtifact(FormData data, NodeRef artistFolder, String artistName) {
		// TODO: LOG
		NodeRef artistArtifactRef = null;

		String artistArtifactFilename = "About " + artistName;
		if (artistFolder != null && !StringUtils.isEmpty(artistArtifactFilename)) {
			FileInfo artistImageFile = this.getFileFolderService().create(artistFolder, artistArtifactFilename,
					TYPE_UCM_ARTIST_ARTIFACT_QNAME);

			artistArtifactRef = artistImageFile.getNodeRef();

			TypeDefinition artistArtifactType = this.getDictionaryService().getType(
					TYPE_UCM_ARTIST_ARTIFACT_QNAME);
			inheritProperties(artistArtifactType, artistFolder, artistArtifactRef);
			writeContent(artistArtifactType, data, artistArtifactRef);
			fillMandatoryProperties(artistArtifactType, artistArtifactRef, MANDATORY_PROP_FILLER);
		}
		return artistArtifactRef;
	}
}
