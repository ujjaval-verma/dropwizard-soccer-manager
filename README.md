# Soccer Manger #
This project is intended to demonstrate a set of RESTful APIs for a bare-bones soccer manager service.

## Build ##
* Install and configure <a href="https://maven.apache.org/install.html" target="_blank">Maven</a>
* Open terminal and navigate to folder where project's pom.xml file is.
* Build project and package JAR with following command

`mvn clean package`

## Run ##
Run JAR with following command

`java -jar target/SoccerManager-1.0-SNAPSHOT.jar server ./config.yml`

Alternatively, you can run the service through your IntelliJ IDE using the *soccerApp.run.xml* app config

## APIs ##
List of APIs exposed by this soccer manager service:
### User Resource: ###
- POST    /user/create
- GET     /user/login
- GET     /user
- GET     /user/details

### Team Resource: ### 
- GET     /team
- GET     /team/all
- PUT     /team

### Player Resource: ### 
- GET     /player/{id}
- PUT     /player/{id}
- PUT     /player/bulkUpdate

### Transfer Resource: ### 
- GET     /transfer/list
- POST    /transfer/initiate
- POST    /transfer/purchase/{playerId}

You can also check out this sample <a href="https://www.postman.com/collections/223aa1f0aae57519acd3">Postman APIs Collection</a> for the soccer manager service to better understand its functionalities.