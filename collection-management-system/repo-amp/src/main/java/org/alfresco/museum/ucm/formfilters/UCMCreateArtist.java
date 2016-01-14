package org.alfresco.museum.ucm.formfilters;

import static org.alfresco.museum.ucm.UCMConstants.MANDATORY_PROP_FILLER;
import static org.alfresco.museum.ucm.UCMConstants.PROP_UCM_ARTIST_ARTIFACT_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.PROP_UCM_ARTIST_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIST_ARTIFACT_QNAME;
import static org.alfresco.museum.ucm.UCMConstants.TYPE_UCM_ARTIST_QNAME;

import java.io.Serializable;

import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
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
		boolean isArtist = item.getName().equals(TYPE_UCM_ARTIST_QNAME);
		if (isArtist) {
			resolvePossibleFilenameConflict(item, data);
		}
	}

	/**
	 * Save submitted content into "artist artifact".
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
			super.getUtils().synchronizeUCMPropertyValues(artistFolder, artistArtifactRef);
			writeContent(artistArtifactType, data, artistArtifactRef);
			// artist artifact width should be no less than 510 px
			this.resizeImage(artistArtifactRef, 510);
			super.getUtils().fillMandatoryProperties(artistArtifactType, artistArtifactRef, MANDATORY_PROP_FILLER);
			getOrCreateArtistMediaFolder(artistArtifactRef);
		}
		return artistArtifactRef;
	}
}
