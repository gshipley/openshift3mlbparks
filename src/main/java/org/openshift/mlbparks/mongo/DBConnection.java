package org.openshift.mlbparks.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;


@Named
@ApplicationScoped
public class DBConnection {

	private static final String FILENAME = "/parks.json";

	private static final String COLLECTION_NAME = "parks";

	private MongoDatabase mongoDB;

	
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
		
		try {
		MongoCredential credential = MongoCredential.createCredential(mongoUser, mongoDBName, mongoPassword.toCharArray());
		MongoClient mongoClient = new MongoClient(new ServerAddress(mongoHost, Integer.parseInt(mongoPort)), Arrays.asList(credential));
		mongoDB = mongoClient.getDatabase(mongoDBName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		

		this.initDatabase(mongoDB);

	}

	public MongoDatabase getDB() {
		return mongoDB;
	}

	public MongoCollection getCollection() {
		return mongoDB.getCollection(COLLECTION_NAME);
	}

	private void initDatabase(MongoDatabase mongoDB) {
		MongoCollection parkListCollection = getCollection();
		int imported = 0;
		if (parkListCollection.count() < 1) {
			System.out.println("The database is empty.  We need to populate it");
			try {
				String currentLine = new String();
				InputStream is = getClass().getClassLoader().getResourceAsStream(FILENAME);
				BufferedReader in = new BufferedReader(new InputStreamReader(is));
				while ((currentLine = in.readLine()) != null) {
					parkListCollection.insertOne(Document.parse(currentLine.toString()));
					imported++;
				}
				System.out.println("Successfully imported " + imported + " elements.");

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void checkDatabase() {
	    this.initDatabase(mongoDB);
    }

}
