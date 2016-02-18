package org.openshift.geoapp.mongo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;

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

@Named
@ApplicationScoped
public class DBConnection {

	public static final String POI_COLLECTION = "poi";

	private DB mongoDB;
	
	@Inject
	private Config config;

	public DBConnection() {
		super();
	}

	@PostConstruct
	public void afterCreate() {
		String mongoHost = (System.getenv("MONGODB_SERVICE_HOST") == null) ? "127.0.0.1" : System.getenv("MONGODB_SERVICE_HOST");
		String mongoPort = (System.getenv("MONGODB_SERVICE_PORT") == null) ? "27017" : System.getenv("MONGODB_SERVICE_PORT"); 
		String mongoUser = (System.getenv("MONGODB_USER")== null) ? "mlbparks" : System.getenv("MONGODB_USER");
		String mongoPassword = (System.getenv("MONGODB_PASSWORD") == null) ? "mlbparks" : System.getenv("MONGODB_PASSWORD");
		String mongoDBName = (System.getenv("MONGODB_DATABASE") == null) ? "mlbparks" : System.getenv("MONGODB_DATABASE");
		// Check if we are using a mongoDB template or mongodb RHEL 7 image
		if (mongoHost == null) {
			mongoHost = System.getenv("MONGODB_24_RHEL7_SERVICE_HOST");
		} 
		if (mongoPort == null) {
			mongoPort = System.getenv("MONGODB_24_RHEL7_SERVICE_PORT");
		}
		
		int port = Integer.decode(mongoPort);
		
		Mongo mongo = null;
		try {
			mongo = new Mongo(mongoHost, port);
			System.out.println("Connected to database");
		} catch (UnknownHostException e) {
			System.out.println("Couldn't connect to MongoDB: " + e.getMessage() + " :: " + e.getClass());
		}

		mongoDB = mongo.getDB(mongoDBName);

		if (mongoDB.authenticate(mongoUser, mongoPassword.toCharArray()) == false) {
			System.out.println("Failed to authenticate DB ");
		}

		this.initDatabase(mongoDB);

	}

	public DB getDB() {
		return mongoDB;
	}

	private void initDatabase(DB mongoDB) {
		DBCollection parkListCollection = mongoDB.getCollection(POI_COLLECTION);
		int itemsImported = 0;
		if (parkListCollection.count() < 1) {
			System.out.println("The database is empty.  We need to populate it");
			try {
				String currentLine = new String();
				URL jsonFile = new URL(config.getDataFile());
				BufferedReader in = new BufferedReader(new InputStreamReader(jsonFile.openStream()));
				while ((currentLine = in.readLine()) != null) {
					parkListCollection.insert((DBObject) JSON.parse(currentLine.toString()));
					itemsImported++;
				}
				System.out.println("Successfully imported " + itemsImported + " items.");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
