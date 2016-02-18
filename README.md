# Sample application for OpenShift 3

This sample application will create and deploy a JBoss EAP application server as well as a MongoDB database.  The sample application will display a map and perform geospatial queries to populate the map with geo-spatial data such as League Baseball stadiums in the United States.


## Quick instructions to just get this working on an OpenShift 3 deployment as a normal user

````
$ oc login https://yourOpenShiftServer
$ oc new-project geoapp
$ oc new-app eap64-mongodb-s2i \
	-p APPLICATION_NAME=mlbparks \
	-p SOURCE_REPOSITORY_URL=https://github.com/siamaksade/openshift3geoapp.git \
	-p SOURCE_REPOSITORY_REF=master \
	-p CONTEXT_DIR=.
	
````
Once the application is deployed and running, you can also scale the number of EAP pods to 3 with the following commands:

````
$ oc scale --replicas=3 rc mlbparks-1
````

## Install template as cluster-admin for everyone to use

Load the template with cluster-admin user:

````
$ oc create -f https://raw.githubusercontent.com/siamaksade/openshift3geoapp/master/geoapp-template.json -n openshift
$ oc new-app geoapp
````

## Application Configuration
In order to change the dataset displayed on the map, modify _src/main/resources/config.properties_ to point to the JSON file containing the data 
and update the popup template to match the JSON fields.

