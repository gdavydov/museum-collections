/**
 * 
 */
package org.alfresco.museum.ucm.model;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel;
import org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Aspects;
import org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Aspects.Aspect;
import org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Types;
import org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Types.Type;
import org.alfresco.museum.ucm.model.config.UCMConfigurator;
//import org.alfresco.i18n.I18NUtil;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;
import org.alfresco.repo.forms.processor.node.UCMGenericFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import javax.faces.model.SelectItem;

/**
 * @author gdavydov
 *         http://forums.alfresco.com/forum/developer-discussions/repository-services/custom-constraint-03262008-1636
 */
public abstract class UCMListOfValuesConstraints extends ListOfValuesConstraint implements Serializable {
	private static Log logger = LogFactory.getLog(UCMListOfValuesConstraints.class);// LogFactory.getLog(BaseComponentGenerator.class);

	private static final long serialVersionUID = 1;

	private List<String> allowedLabels;
	private DataModel dm = null;
	private FileInputStream fis = null;

	public void setAllowedValues(List<String> allowedValues) 
	{
		List<String> av = allowedValues;
		if (av == null || av.size() == 0 ) {
			av = getLOVData();
			if (av == null) {
				av = new ArrayList<String>(1);
				av.add("Values Not Found");
			}
		}
		super.setAllowedValues(av);
	}

	public void setCaseSensitive(boolean caseSensitive) {
		super.setCaseSensitive(caseSensitive);
	}

	public void initialize() {
		super.setCaseSensitive(false);
		getLOVData();
	}

	public List<String> getAllowedLabels() {
		return this.allowedLabels;
	}

	public void setAllowedLabels(List<String> allowedLabels) {
		this.allowedLabels = allowedLabels;
	}

	public List<SelectItem> getSelectItemList() {
		List<SelectItem> result = new ArrayList<SelectItem>(this.getAllowedValues().size());
		for (int i = 0; i < this.getAllowedValues().size(); i++) {
			result.add(new SelectItem((Object) this.getAllowedValues().get(i), this.allowedLabels.get(i)));
		}
		return result;
	}

	protected List<String> getLOVConfiguration(String typeName, String proprtyName) throws Exception
	{
		List<String> data = null;
		DataModel dm = UCMConfigurator.getDataModelConstraints();

		Types _t = dm.getTypes();
		Aspects _a = dm.getAspects();

		List<Type> types = _t.getType();
		for (Type type : types) {
			if (type.getName().equals(typeName)) {
				List<org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Types.Type.Property> _p = type.getProperty();
				for (org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Types.Type.Property prop : _p) {
					if (prop.getName().equals(proprtyName))
						return prop.getValue();
				}
			}
		}
		List<Aspect> aspects = _a.getAspect();
		for (Aspect aspect : aspects) {
			List<org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Aspects.Aspect.Property> _p = aspect.getProperty();
			for (org.alfresco.museum.ucm.config.autogen.UCMConfig.DataModel.Aspects.Aspect.Property prop : _p) {
				if (prop.getName().equals(proprtyName))
					return prop.getValue();
			}
		}
		return null;

	}
	protected abstract List<String> getLOVData();

}
