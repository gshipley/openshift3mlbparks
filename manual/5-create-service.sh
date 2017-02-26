oc expose dc dc5 --port=8080
oc expose dc/mydc --name=mlb-http --type=NodePort
