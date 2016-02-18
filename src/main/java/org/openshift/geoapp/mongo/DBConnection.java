package org.openshift.geoapp.mongo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.openshift.geoapp.util.Config;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.util.JSON;
import com.mongodb.util.JSONParseException;

@Named
@ApplicationScoped
public class DBConnection {
	private static final Logger LOG = Logger.getLogger(Config.class.getName());

	public static final String POI_COLLECTION = "poi";

	private DB mongoDB;

	@Inject
	private Config config;

	public DBConnection() {
		super();
	}

	@PostConstruct
	public void afterCreate() {
		String prefix = getApplicaitonName();
		String mongoHost = System.getenv(prefix + "MONGODB_SERVICE_HOST");
		String mongoPort = System.getenv(prefix + "MONGODB_SERVICE_PORT");
		String mongoUser = (System.getenv("DB_USERNAME") == null) ? "geoapp" : System.getenv("DB_USERNAME");
		String mongoPassword = (System.getenv("DB_PASSWORD") == null) ? "geoapp" : System.getenv("DB_PASSWORD");
		String mongoDBName = (System.getenv("DB_DATABASE") == null) ? "geoapp" : System.getenv("DB_DATABASE");

		// Check if mongodb is created separately
		if (mongoHost == null) {
			mongoHost = System.getenv("MONGODB_SERVICE_HOST");
		}
		if (mongoPort == null) {
			mongoPort = System.getenv("MONGODB_SERVICE_PORT");
		}

		// Check if we are using a mongoDB template or mongodb RHEL 7 image
		if (mongoHost == null) {
			mongoHost = System.getenv("MONGODB_24_RHEL7_SERVICE_HOST");
		}
		if (mongoPort == null) {
			mongoPort = System.getenv("MONGODB_24_RHEL7_SERVICE_PORT");
		}
		
		// Default if nothing found
		if (mongoHost == null || mongoPort == null) {
			LOG.info("No MongoDB info found. Setting default values");
			mongoHost = "127.0.0.1";
			mongoPort = "27017";
		}

		int port = Integer.decode(mongoPort);

		Mongo mongo = null;
		try {
			LOG.info("Connecting to MongoDB (" + mongoHost + ":" + mongoPort + ")");
			mongo = new Mongo(mongoHost, port);
			LOG.info("Connected to database");
		} catch (UnknownHostException e) {
			LOG.info("Couldn't connect to MongoDB: " + e.getMessage() + " :: " + e.getClass());
		}

		mongoDB = mongo.getDB(mongoDBName);

		if (mongoDB.authenticate(mongoUser, mongoPassword.toCharArray()) == false) {
			LOG.info("Failed to authenticate DB ");
		}

		this.initDatabase(mongoDB);

	}

	private String getApplicaitonName() {
		String kubeLabels = System.getenv("OPENSHIFT_KUBE_PING_LABELS");
		if (kubeLabels != null) {
			for (String keyVal : kubeLabels.split(",")) {
				int delimiterPos = keyVal.indexOf('=');
				String key = keyVal.substring(0, delimiterPos);
				String value = keyVal.substring(delimiterPos + 1);

				if ("application".equals(key)) {
					return value.toUpperCase() + "_";
				}
			}
			return null;
		}

		return "";
	}

	public DB getDB() {
		return mongoDB;
	}

	private void initDatabase(DB mongoDB) {
		DBCollection parkListCollection = mongoDB.getCollection(POI_COLLECTION);
		int itemsImported = 0;
		if (parkListCollection.count() < 1) {
			LOG.info("The database is empty.  We need to populate it");
			try {
				String currentLine = new String();
				URL jsonFile = new URL(config.getDataFile());
				BufferedReader in = new BufferedReader(new InputStreamReader(jsonFile.openStream()));
				while ((currentLine = in.readLine()) != null) {
					try {
						parkListCollection.insert((DBObject) JSON.parse(currentLine.toString()));
						itemsImported++;
					} catch (JSONParseException ex) {
						LOG.warning("Failed to parse JSON: " + ex.getMessage());
					}
				}
				
				LOG.info("Successfully imported " + itemsImported + " items.");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
