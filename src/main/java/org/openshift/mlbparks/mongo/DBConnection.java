package org.openshift.mlbparks.mongo;

import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import com.mongodb.DB;
import com.mongodb.Mongo;

@Named
@ApplicationScoped
public class DBConnection {

	private DB mongoDB;

	public DBConnection() {
		super();
	}

	@PostConstruct
	public void afterCreate() {
		String mongoHost = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
		String mongoPort = System.getenv("OPENSHIFT_MONGODB_DB_PORT");
		String mongoUser = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
		String mongoPassword = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
		String mongoDBName = System.getenv("OPENSHIFT_APP_NAME");
		int port = Integer.decode(mongoPort);

		Mongo mongo = null;
		try {
			mongo = new Mongo(mongoHost, port);
		} catch (UnknownHostException e) {
			System.out.println("Couldn't connect to MongoDB: " + e.getMessage()
					+ " :: " + e.getClass());
		}

		mongoDB = mongo.getDB(mongoDBName);

		if (mongoDB.authenticate(mongoUser, mongoPassword.toCharArray()) == false) {
			System.out.println("Failed to authenticate DB ");
		}

	}

	public DB getDB() {
		return mongoDB;
	}

}
