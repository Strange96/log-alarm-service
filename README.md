# Log-Alarm-Service
LogOps Alarm Service - A service to deal with triggered alarms

# Setup
The service needs a Mongo DB on port 27019. This can be started with docker-compose by running:

`docker-compose up -d mongo`

The easiest way to run the Alarm-Service is to use the Docker image. With docker-compose:

`docker-compose up -d alarmservice`

This will start the service on port 8085.

Otherwise, you need to have Micronaut installed. You can the build the project by running:

`./gradlew assemble`

To start the service on port 8085, run:

`./gradlew run`

To run the tests, run the command:

`./gradlew test`

# Usage
To store a log send a POST request to `/alarms` with a body similar to (this is an example):

`{ "name": "My Alarm", "severity": "bad", "customer_id": "customer42", "log_id": "42" }`

If the triple (name, severity, customer_id) exists, the log_id is appended to the alarm's list of log_ids. A new alarm is created otherwise.

To retrieve all alarms, send a GET request to `/alarms`.

To get a certain alarm, send a GET request to `/alarms/{alarm_id}`, e.g. `/alarms/42`

To get all alarms of a certain customer, send a GET request similar to `/alarms?customer_id=customer42`

To get all alarms of a certain customer with a specific status, send a GET request similar to `/alarms?customer_id=customer42&status=NEW`

The available statuses are: 'NEW', 'RESOLVED', 'ESCALATED'

To get all alarms of a certain customer with a specific severity, send a GET request similar to `/alarms?customer_id=customer42&severity=bad`

To change the status and write a comment for a certain alarm, send a PATCH request to `/alarms/{alarm_id}`, e.g. `/alarms/42`, with a body similar to:

`{ "status": "RESOLVED", "comment": "This was handled by doing X and Y" }`
