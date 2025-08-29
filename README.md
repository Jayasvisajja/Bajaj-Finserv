# Bajaj-Finserv
This is my submission of the task given
Bajaj Finserv Health | Qualifier 1 | JAVA
📌 Overview

This Spring Boot application is built as part of the Bajaj Finserv Health Qualifier 1 (Java) challenge.

The app performs the following steps automatically on startup (no controller/endpoint triggers the flow):

Generates a webhook by sending a POST request to the given API.

Receives the response containing a webhook URL and an access token.

Determines the assigned SQL question based on the last two digits of the registration number.

Odd → Question 1

Even → Question 2

Solves the SQL problem and stores the solution in an in-memory H2 database (solutions table).

Submits the final SQL query back to the provided webhook URL using the JWT access token in the Authorization header.

🚀 How to Run

Clone the repository:

git clone https://github.com/<your-username>/<your-repo>.git
cd <your-repo>


Run the JAR file:

java -jar dist/demo-0.0.1-SNAPSHOT.jar


On startup, the app will:

Generate the webhook

Select the appropriate SQL question

Save the solution to the H2 database

Submit the final query to the test webhook

🗄️ H2 Database Access

The app uses an in-memory H2 database.

Console URL: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:webhookdb

Username: SA

Password: (leave blank)

Run the following query to check stored solutions:

SELECT * FROM solutions;

🛠️ Technologies Used

Java 17

Spring Boot 3.5.5

Spring Data JPA + H2 Database

RestTemplate/WebClient for API calls

JWT Authorization header for webhook submission

📦 Repository Contents

src/ → Source code

dist/ → Packaged JAR file for submission

README.md → Project documentation
