package org.openshift.geoapp.util;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

@Named
@ApplicationScoped
public class Config {
	private static final String DATA_POS_FIELD = "data.pos.field";
	private static final String DATA_POPUP_TEMPLATE = "data.popup.template";
	private static final String DATA_FILE = "data.file";
	
	private PropertiesConfiguration config;
	
	private static final Logger LOG = Logger.getLogger(Config.class.getName());
	
	@PostConstruct
	public void init() throws ConfigurationException {
		config = new PropertiesConfiguration("config.properties");
		LOG.info("GeoApp configuration initialized.");
	}
	
	public String getDataFile() {
		return config.getString(DATA_FILE, "");
	}
	
	public String getPopupTemplate() {
		return config.getString(DATA_POPUP_TEMPLATE);
	}
	
	public String getPositionField() {
		return config.getString(DATA_POS_FIELD, "coordinates");
	}
}
