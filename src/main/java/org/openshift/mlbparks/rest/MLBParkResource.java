package org.openshift.mlbparks.rest;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.openshift.mlbparks.domain.MLBPark;
import org.openshift.mlbparks.mongo.DBConnection;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.ArrayList;
import java.util.List;

@RequestScoped
@Path("/parks")
public class MLBParkResource {

	@Inject
	private DBConnection dbConnection;


	private MLBPark populateParkInformation(Document dataValue) {
		MLBPark thePark = new MLBPark();
		thePark.setName(dataValue.get("name"));
		thePark.setPosition(dataValue.get("coordinates"));
		thePark.setId(dataValue.get("_id").toString());
		thePark.setBallpark(dataValue.get("ballpark"));
		thePark.setLeague(dataValue.get("league"));
		thePark.setPayroll(dataValue.get("payroll"));

		return thePark;
	}

	// get all the mlb parks
	@GET()
	@Produces("application/json")
	public List<MLBPark> getAllParks() {
		ArrayList<MLBPark> allParksList = new ArrayList<MLBPark>();

		MongoCollection mlbParks = dbConnection.getCollection();
		MongoCursor<Document> cursor = mlbParks.find().iterator();
		try {
			while (cursor.hasNext()) {
				allParksList.add(this.populateParkInformation(cursor.next()));
			}
		} finally {
			cursor.close();
		}

		return allParksList;
	}

	@GET
	@Produces("application/json")
	@Path("within")
	public List<MLBPark> findParksWithin(@QueryParam("lat1") float lat1,
			@QueryParam("lon1") float lon1, @QueryParam("lat2") float lat2,
			@QueryParam("lon2") float lon2) {

		ArrayList<MLBPark> allParksList = new ArrayList<MLBPark>();
		MongoCollection mlbParks =  dbConnection.getCollection();

		// make the query object
		BasicDBObject spatialQuery = new BasicDBObject();

		ArrayList<double[]> boxList = new ArrayList<double[]>();
		boxList.add(new double[] { new Float(lon2), new Float(lat2) });
		boxList.add(new double[] { new Float(lon1), new Float(lat1) });

		BasicDBObject boxQuery = new BasicDBObject();
		boxQuery.put("$box", boxList);

		spatialQuery.put("coordinates", new BasicDBObject("$within", boxQuery));
		System.out.println("Using spatial query: " + spatialQuery.toString());

		MongoCursor<Document> cursor = mlbParks.find(spatialQuery).iterator();
		try {
			while (cursor.hasNext()) {
				allParksList.add(this.populateParkInformation(cursor.next()));
			}
		} finally {
			cursor.close();
		}

		return allParksList;
	}
}
