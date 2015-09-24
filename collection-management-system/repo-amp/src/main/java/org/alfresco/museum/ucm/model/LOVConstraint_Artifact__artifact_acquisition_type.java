/**
 * 
 */
package org.alfresco.museum.ucm.model;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.museum.ucm.model.config.UCMConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author
 * 
 * Get allowed values for ucm:artifact->ucm:artifact_medium
 */
public class LOVConstraint_Artifact__artifact_acquisition_type extends UCMListOfValuesConstraints implements Serializable {
	private static Log logger = LogFactory.getLog(LOVConstraint_Artifact__artifact_acquisition_type.class);

	private static final long serialVersionUID = 1;
	private static final String TYPE_NAME = "ucm:artifact";
	private static final String PROPERTY_NAME = "ucm:artifact_acquisition_type";

	protected List<String> getLOVData()
	{
		try {
			List<String> av = getLOVConfiguration(TYPE_NAME, PROPERTY_NAME);
			this.setAllowedValues(av);
			this.setAllowedLabels(av);
			return av;
		}
		catch(Exception e) {
			e.printStackTrace();
			List<String> av = new ArrayList<String>();
			av.add("Error/Exception in a code");
			return av;
		}
	}
}
