# Sample application for OpenShift 3


## Install template

Load the template with cluster-admin user:

````
# oc create -f mlbparks-template.json -n openshift
````

## Create a new app
Create a new app in you user`s project. Provide your own values

````
$ oc new-app mlbparks -p APPLICATION_NAME=mlbparks,APPLICATION_HOSTNAME=mlbparks.cloudapps.example.com
````

