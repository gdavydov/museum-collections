package com.alfresco.museum.ucm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.ModelObject;
import org.springframework.extensions.surf.ModelObjectService;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.surf.types.Component;
import org.springframework.extensions.surf.types.Page;
import org.springframework.extensions.surf.types.TemplateInstance;
import org.springframework.extensions.surf.util.XMLUtil;
import org.springframework.extensions.webscripts.SearchPath;
import org.springframework.extensions.webscripts.Store;

/**
 * All changes required to create site presets are done inside call to
 * {@link org.springframework.extensions.surf.PresetsManager#constructPreset(String, Map)
 * PresetsManager.constructPreset}.<br/>
 * E.g.
 * {@code presetsManager.constructPreset("ucm-site-dashboard", Collections.singletonMap("siteid", siteName))}
 * <br/>
 * This class duplicates behavior of
 * {@link org.springframework.extensions.surf.PresetsManager#constructPreset(String, Map)
 * PresetsManager.constructPreset} except that it returns presets data instead
 * of actually persisting changes.
 */
public class UCMPresetsManager {
	/**
	 * See {@link org.springframework.extensions.surf.PresetsManager#searchPath
	 * PresetsManager.searchPath}.
	 */
	private SearchPath searchPath;

	/**
	 * See
	 * {@link org.springframework.extensions.surf.PresetsManager#fileSuffices
	 * PresetsManager.fileSuffices}.
	 */
	private List<String> fileSuffices;

	/**
	 * See {@link org.springframework.extensions.surf.PresetsManager#documents
	 * PresetsManager.documents}.
	 */
	private Document[] documents;

	/**
	 * See
	 * {@link org.springframework.extensions.surf.PresetsManager#modelObjectService
	 * PresetsManager.modelObjectService}.
	 */
	private ModelObjectService modelObjectService;

	public void setSearchPath(SearchPath searchPath) {
		this.searchPath = searchPath;
	}

	public void setFiles(List<String> files) {
		this.fileSuffices = files;
	}

	public ModelObjectService getModelObjectService() {
		return modelObjectService;
	}

	public void setModelObjectService(ModelObjectService modelObjectService) {
		this.modelObjectService = modelObjectService;
	}

	/**
	 * See {@link org.springframework.extensions.surf.PresetsManager#init()
	 * PresetsManager.init()}.
	 */
	private void init() {
		if (this.searchPath == null || this.fileSuffices == null) {
			throw new IllegalArgumentException("SearchPath and Files list are mandatory.");
		}

		// Search for our preset XML descriptor documents

		// Find all the preset configuration files in all the configured stores.
		// In order to maintain
		// a sensible precedence order we will search the stores in order and
		// then check every
		// document path against the list of suffices. This is not the most
		// efficient way of processing
		// the configuration files but as this only happens at application
		// startup it is not a major
		// problem...
		List<Document> docs = new ArrayList<Document>(4);
		for (Store store : this.searchPath.getStores()) {
			// For the current storee...
			for (String path : store.getAllDocumentPaths()) {
				// ...get all the documents...
				for (String fileSuffix : this.fileSuffices) {
					// ...and see if each ends with the current file suffix...
					if (path.endsWith(fileSuffix)) {
						try {
							docs.add(XMLUtil.parse(store.getDocument(path)));
						} catch (IOException ioe) {
							throw new PlatformRuntimeException("Error loading presets XML file: " + fileSuffix
									+ " in store: " + store.toString(), ioe);
						} catch (DocumentException de) {
							de.printStackTrace();
							throw new PlatformRuntimeException("Error processing presets XML file: " + fileSuffix
									+ " in store: " + store.toString(), de);
						}
						break; // No point in carrying on around the loop, we've
								// already added the file.
					}
				}

			}
		}
		this.documents = docs.toArray(new Document[docs.size()]);
	}

	/**
	 * See
	 * {@link org.springframework.extensions.surf.PresetsManager#constructPreset(String, Map)
	 * PresetsManager.constructPreset()}.
	 * 
	 * @throws JSONException
	 */
	@SuppressWarnings("unchecked")
	public String constructPreset(String id, Map<String, String> tokens) throws JSONException {
		if (id == null) {
			throw new IllegalArgumentException("Preset ID is mandatory.");
		}

		// perform one time init - this cannot be perform in an app handler or
		// by the
		// framework init - as it requires the Alfresco server to be started...
		synchronized (this) {
			if (this.documents == null) {
				init();
			}
		}

		// UCM modification:
		JSONArray componentsList = new JSONArray();
		JSONArray pagesList = new JSONArray();
		JSONArray templatesList = new JSONArray();

		boolean foundPreset = false;
		for (int i = 0; (!foundPreset && i < this.documents.length); i++) {
			Iterator<Element> presets = ((List<Element>) this.documents[i].getRootElement().elements("preset"))
					.iterator();
			while (!foundPreset && presets.hasNext()) {
				Element preset = presets.next();
				if (id.equals(preset.attributeValue("id"))) {
					// Found our preset, this will be out last iteration of both
					// inner and outer loop...
					foundPreset = true;

					// any components in the preset?
					Element components = preset.element("components");
					if (components != null) {
						for (Element c : (List<Element>) components.elements("component")) {
							// apply token replacement to each value as it is
							// retrieved
							String title = replace(c.elementTextTrim(Component.PROP_TITLE), tokens);
							String titleId = replace(c.elementTextTrim(Component.PROP_TITLE_ID), tokens);
							String description = replace(c.elementTextTrim(Component.PROP_DESCRIPTION), tokens);
							String descriptionId = replace(c.elementTextTrim(Component.PROP_DESCRIPTION_ID), tokens);
							String typeId = replace(c.elementTextTrim(Component.PROP_COMPONENT_TYPE_ID), tokens);
							String scope = replace(c.elementTextTrim(Component.PROP_SCOPE), tokens);
							String regionId = replace(c.elementTextTrim(Component.PROP_REGION_ID), tokens);
							String sourceId = replace(c.elementTextTrim(Component.PROP_SOURCE_ID), tokens);
							String url = replace(c.elementTextTrim(Component.PROP_URL), tokens);
							String uri = replace(c.elementTextTrim(Component.PROP_URI), tokens);
							String chrome = replace(c.elementTextTrim(Component.PROP_CHROME), tokens);

							// validate mandatory values
							if (scope == null || scope.length() == 0) {
								throw new IllegalArgumentException(
										"Scope is a mandatory property for a component preset.");
							}
							if (regionId == null || regionId.length() == 0) {
								throw new IllegalArgumentException(
										"RegionID is a mandatory property for a component preset.");
							}
							if (sourceId == null || sourceId.length() == 0) {
								throw new IllegalArgumentException(
										"SourceID is a mandatory property for a component preset.");
							}

							// generate component
							Component component = modelObjectService.newComponent(scope, regionId, sourceId);
							component.setComponentTypeId(typeId);
							component.setTitle(title);
							component.setTitleId(titleId);
							component.setDescription(description);
							component.setDescriptionId(descriptionId);
							component.setURL(url);
							component.setURI(uri); // Set both URI and URL to
													// support consistency
													// between component types
							component.setChrome(chrome);

							// apply arbituary custom properties
							if (c.element("properties") != null) {
								for (Element prop : (List<Element>) c.element("properties").elements()) {
									String propName = replace(prop.getName(), tokens);
									String propValue = replace(prop.getTextTrim(), tokens);
									component.setCustomProperty(propName, propValue);
								}
							}

							// UCM modification:
							componentsList.put(createJSONObject(component));
						}
					}

					// any pages in the preset?
					Element pages = preset.element("pages");
					if (pages != null) {
						for (Element p : (List<Element>) pages.elements("page")) {
							// apply token replacement to each value as it is
							// retrieved
							String pageId = replace(p.attributeValue(Page.PROP_ID), tokens);
							String title = replace(p.elementTextTrim(Page.PROP_TITLE), tokens);
							String titleId = replace(p.elementTextTrim(Page.PROP_TITLE_ID), tokens);
							String description = replace(p.elementTextTrim(Page.PROP_DESCRIPTION), tokens);
							String descriptionId = replace(p.elementTextTrim(Page.PROP_DESCRIPTION_ID), tokens);
							String typeId = replace(p.elementTextTrim(Page.PROP_PAGE_TYPE_ID), tokens);
							String auth = replace(p.elementTextTrim(Page.PROP_AUTHENTICATION), tokens);
							String template = replace(p.elementTextTrim(Page.PROP_TEMPLATE_INSTANCE), tokens);

							// validate mandatory values
							if (pageId == null || pageId.length() == 0) {
								throw new IllegalArgumentException("ID is a mandatory attribute for a page preset.");
							}
							if (template == null || template.length() == 0) {
								throw new IllegalArgumentException(
										"Template is a mandatory property for a page preset.");
							}

							// generate page
							Page page = modelObjectService.newPage(pageId);
							page.setPageTypeId(typeId);
							page.setTitle(title);
							page.setTitleId(titleId);
							page.setDescription(description);
							page.setDescriptionId(descriptionId);
							page.setAuthentication(auth);
							page.setTemplateId(template);

							// apply arbituary custom properties
							if (p.element("properties") != null) {
								for (Element prop : (List<Element>) p.element("properties").elements()) {
									String propName = replace(prop.getName(), tokens);
									String propValue = replace(prop.getTextTrim(), tokens);
									page.setCustomProperty(propName, propValue);
								}
							}

							// UCM modification:
							pagesList.put(createJSONObject(page));
						}
					}

					// any template instances in the preset?
					Element templates = preset.element("template-instances");
					if (templates != null) {
						for (Element t : (List<Element>) templates.elements("template-instance")) {
							// apply token replacement to each value as it is
							// retrieved
							String templateId = replace(t.attributeValue(TemplateInstance.PROP_ID), tokens);
							String title = replace(t.elementTextTrim(TemplateInstance.PROP_TITLE), tokens);
							String titleId = replace(t.elementTextTrim(TemplateInstance.PROP_TITLE_ID), tokens);
							String description = replace(t.elementTextTrim(TemplateInstance.PROP_DESCRIPTION), tokens);
							String descriptionId = replace(t.elementTextTrim(TemplateInstance.PROP_DESCRIPTION_ID),
									tokens);
							String templateType = replace(t.elementTextTrim(TemplateInstance.PROP_TEMPLATE_TYPE),
									tokens);

							// validate mandatory values
							if (templateId == null || templateId.length() == 0) {
								throw new IllegalArgumentException(
										"ID is a mandatory attribute for a template-instance preset.");
							}
							if (templateType == null || templateType.length() == 0) {
								throw new IllegalArgumentException(
										"Template is a mandatory property for a page preset.");
							}

							// generate template-instance
							TemplateInstance template = modelObjectService.newTemplate(templateId);
							template.setTitle(title);
							template.setTitleId(titleId);
							template.setDescription(description);
							template.setDescriptionId(descriptionId);
							template.setTemplateTypeId(templateType);

							// apply arbituary custom properties
							if (t.element("properties") != null) {
								for (Element prop : (List<Element>) t.element("properties").elements()) {
									String propName = replace(prop.getName(), tokens);
									String propValue = replace(prop.getTextTrim(), tokens);
									template.setCustomProperty(propName, propValue);
								}
							}

							// UCM modification:
							templatesList.put(createJSONObject(template));
						}
					}

					// TODO: any chrome, associations, types, themes etc. in the
					// preset...

					// UCM modification

					// found our preset - no need to process further
					break;
				}
			}
		}

		// UCM modification:
		JSONObject result = new JSONObject();
		result.put("components", componentsList);
		result.put("pages", pagesList);
		result.put("templates", templatesList);

		return result.toString();
	}

	/**
	 * See
	 * {@link org.springframework.extensions.surf.PresetsManager#replace(String, Map)
	 * PresetsManager.replace()}.
	 */
	private static String replace(String s, Map<String, String> tokens) {
		if (s != null && tokens != null) {
			for (Entry<String, String> entry : tokens.entrySet()) {
				String key = "${" + entry.getKey() + "}";
				String value = entry.getValue();
				if (s.indexOf(key) != -1 && value != null) {
					// There is no point attempting to replace the key if it
					// isn't present,
					// and if we attempt to replace a key with a null then we'll
					// just end up with
					// a NullPointerException
					s = s.replace(key, value);
				}
			}
		}
		return s;
	}

	private JSONObject createJSONObject(ModelObject object) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("id", object.getId());
		String xml = object.toXML();
		result.put("xml", xml);
		return result;
	}
}
