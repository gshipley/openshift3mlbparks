package org.openshift.mlbparks.mongo;

import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.JSONFunctions;

@Named
@ApplicationScoped
public class DBConnection {

	private DB mongoDB;

	public DBConnection() {
		super();
	}

	@PostConstruct
	public void afterCreate() {
		String mongoHost = System.getenv("MONGODB_SERVICE_HOST");
		String mongoPort = System.getenv("MONGODB_SERVICE_PORT");
		String mongoUser = System.getenv("MONGODB_USER");
		String mongoPassword = System.getenv("MONGODB_PASSWORD");
		String mongoDBName = System.getenv("MONGODB_DATABASE");
		int port = Integer.decode(mongoPort);

		Mongo mongo = null;
		try {
			mongo = new Mongo(mongoHost, port);
			System.out.println("Connected to database");
		} catch (UnknownHostException e) {
			System.out.println("Couldn't connect to MongoDB: " + e.getMessage()
					+ " :: " + e.getClass());
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
		DBCollection parkListCollection = mongoDB.getCollection("teams");
		if(parkListCollection.count() < 1) {
			System.out.println("The database is empty.  We need to populate it");
			//URL url - new URL(http://)
		}
	}

}
