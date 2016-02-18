package org.openshift.geoapp.util;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

@Named
@ApplicationScoped
public class Config {
	private static final String DATA_POPUP_TEMPLATE = "data.popup.template";
	private static final String DATA_FILE = "data.file";
	private static final String DATA_FIELDS = "data.fields";
	
	private PropertiesConfiguration config;
	
	@PostConstruct
	public void init() throws ConfigurationException {
		config = new PropertiesConfiguration("config.properties");
		System.out.println("GeoApp configuration initialized.");
	}
	
	public String getDataFile() {
		return config.getString(DATA_FILE, "");
	}
	
	public List<String> getDataFields() {
		return Arrays.asList(config.getStringArray(DATA_FIELDS));
	}
	
	public String getPopupTemplate() {
		return config.getString(DATA_POPUP_TEMPLATE);
	}
}
