package org.alfresco.museum.ucm.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.tika.io.IOUtils;
import org.springframework.extensions.surf.util.Content;

/**
 *  To detect field MIME type and get content it is required to read content field's input stream twice.
 *  Unfortunately implementations of InputStream used in Content doesn't support {@code reset()} method.
 *  This implementation returns new instance of InputStream on each invocation of {@ code getInputStream()},
 *  also it uses ByteArrayInputStream as InputStream implementation, so {@code reset()} method is supported.
 *  Drawback is increased memory consumption - whole content is stored in memory.
 *  Alternative is to use temporary file
 */
public class UCMContentImpl implements Content {
	private final byte[] content;
	private final String mimetype;
	private final String encoding;

	/**
	 * Constructs content impl from FormField implementation used in web scripts.
	 * @throws IOException
	 */
	public UCMContentImpl(org.springframework.extensions.webscripts.servlet.FormData.FormField formField) throws IOException {
		this.content = IOUtils.toByteArray(formField.getInputStream());
		this.mimetype = formField.getContent().getMimetype();
		this.encoding = formField.getContent().getEncoding();
	}

	/*
	 * TODO: ideally metadata should be extracted in the same way it is done in
	 * upload.post.js:extractMetadata
	 * "actions.create("extract-metadata").execute(file, false, false)"
	 * equivalent Java code would be
	 * "create("extract-metadata").execute(persistedObject, false, false);"
	 * Are there any workarounds? private ScriptAction create(String actionName)
	 * { ScriptAction scriptAction = null; ActionService actionService =
	 * serviceRegistry.getActionService(); ActionDefinition actionDef =
	 * actionService.getActionDefinition(actionName); if (actionDef != null) {
	 * Action action = actionService.createAction(actionName); scriptAction =
	 * new ScriptAction(this.serviceRegistry, action, actionDef);
	 * scriptAction.setScope(new BaseScopableProcessorExtension().getScope()); }
	 * return scriptAction; }
	 */
	public UCMContentImpl(MimetypeService mimetypeService, org.alfresco.repo.forms.FormData.FieldData contentField) throws IOException {
		// org.alfresco.repo.forms.FormData doesn't contain encoding information
		this.encoding = StandardCharsets.UTF_8.name();
		this.content = IOUtils.toByteArray(contentField.getInputStream());
		this.mimetype = mimetypeService.guessMimetype(contentField.getName(), this.getInputStream());
	}

	/**
	 * Constructs content impl from FormData implementation used in FormFilter methods.
	 */
	public UCMContentImpl(org.alfresco.repo.forms.FormData.FieldData contentField, String mimetype) throws IOException {
		// org.alfresco.repo.forms.FormData doesn't contain encoding information
		this.encoding = StandardCharsets.UTF_8.name();
		this.content = IOUtils.toByteArray(contentField.getInputStream());
		this.mimetype = ObjectUtils.defaultIfNull(mimetype, MimetypeMap.MIMETYPE_TEXT_PLAIN);
	}

	@Override
	public String getContent() throws IOException {
		//TODO: don't rely on encoding?
		return new String(content, encoding);
	}

	@Override
	public String getMimetype() {
		return mimetype;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public long getSize() {
		return content.length;
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(content);
	}

	@Override
	public Reader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}


	public boolean isEmpty() {
		return getSize() == 0;
	}
}