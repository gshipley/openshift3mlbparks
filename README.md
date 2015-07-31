# Sample application for OpenShift 3

This sample application will create and deploy a JBoss EAP application server as well as a MongoDB database.  The sample application will display a map and perform geospatial queries to populate the map with all Major League Baseball stadiums in the United States.


## Quick instructions to just get this working on an OpenShift 3 deployment as a normal user

````
$ oc login https://yourOpenShiftServer
$ oc new-project mlbparks
$ oc create -f https://raw.githubusercontent.com/gshipley/openshift3mlbparks/master/mlbparks-template.json
$ oc new-app mlbparks
````
Once the application is deployed and running, you can also scale the number of EAP pods to 3 with the following commands:

````
$ oc scale --replicas=3 rc mlbparks-1
````

## Install template as cluster-admin for everyone to use

Load the template with cluster-admin user:

````
# oc create -f https://raw.githubusercontent.com/gshipley/openshift3mlbparks/master/mlbparks-template.json -n openshift
````

