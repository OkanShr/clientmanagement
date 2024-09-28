# Xeramed Client Management System (Backend)

The Xeramed Client Management System is the backend system responsible for managing clients, appointments, and related media (PDFs, images, etc.) in the Xeramed application. It uses Spring Boot with JWT for security, JPA for persistence, and JDBC for direct database interactions. The system stores sensitive data such as appointment details, client documents, and images in an Amazon S3 bucket.

## Features

- Client Management: Create, update, and delete client information.
- Appointment Management: Create, update, and delete appointment records for clients.
- Media Upload: Upload and manage client-related files, such as PDFs and images, stored securely in Amazon S3.
- Authentication & Authorization: Secured endpoints using JWT with user roles (USER, ADMIN).
- Reports: Get appointment and client statistics.

## Technologies Used

- Spring Boot: Backend framework.
- JWT: Authentication using JSON Web Tokens.
- JPA & Hibernate: Object-relational mapping.
- JDBC: Direct database interactions.
- Amazon S3: Cloud storage for media files.
- AES Encryption: To secure sensitive data.


## Prerequisites

- Java 17+
- MySQL Database
- Amazon S3 Bucket
- Maven (For building the project)

## Project Setup

1. Clone the Repository
  ```bash
    git clone https://github.com/your-repo/xeramed-client-management.git
    cd xeramed-client-management 
```


2. Configure the Application

Ensure that the application.properties file is set up correctly. This file is essential for running the app and is excluded from Git for security reasons.

Here’s an example application.properties:
    ```java
    # Profile and JPA settings
    spring.profiles.active=dev
    spring.jpa.properties.hibernate.dialect=org.hibernate.dialect. MySQLDialect
    spring.jpa.hibernate.ddl-auto=update

    # Amazon S3 Configuration
    amazon.access.key=your_amazon_access_key
    amazon.secret.key=your_amazon_secret_key
    amazon.s3.bucketName=xeramedimages
    amazon.s3.region=eu-central-1

    # Encryption settings
    encryption.algorithm=AES
    encryption.key=MySuperSecretKey

    # File upload limits
    spring.servlet.multipart.max-file-size=10MB
    spring.servlet.multipart.max-request-size=10MB


3. Build the Project
    ```bash
    mvn clean install
    ```

4. Run the Application
    ```bash
    mvn spring-boot:run
    ```

Ensure you have the MySQL database set up and accessible with the credentials provided in the application.properties.

## Endpoints

1. Authentication

- POST /api/v1/auth/signup: Register a new doctor/admin.
- POST /api/v1/auth/signin: Log in and get a JWT token.
- POST /api/v1/auth/refresh: Refresh the JWT token.

2. Client Management

- POST /api/client: Create a new client.
- GET /api/client/{id}: Get details of a client by ID.
- PUT /api/client/{id}: Update a client’s information.
- DELETE /api/client/{id}: Delete a client.
- GET /api/client: Get a list of all clients.

3. Appointment Management

- POST /api/appointment: Create a new appointment.
- GET /api/appointment/{id}: Get appointment details.
- PUT /api/appointment/{id}: Update an appointment.
- DELETE /api/appointment/{clientId}/{appointmentId}: Delete an appointment.
- GET /api/appointment/client/{clientId}: Get all appointments for a specific client.

4. File Management

Client PDFs
- POST /api/client-pdfs/{clientId}/upload: Upload a PDF for a client.
- GET /api/client-pdfs/{clientId}: Get PDFs related to a client.
- DELETE /api/client-pdfs/{clientId}/delete/{pdfId}: Delete a specific PDF.
Client Images
- POST /api/client-images/{clientId}/upload: Upload an image for a client.
- GET /api/client-images/{clientId}: Get images related to a client.
- DELETE /api/client-images/{clientId}/delete/{imageId}: Delete a specific image.
Appointment PDFs
- POST /api/appointment-pdfs/{clientId}/{appointmentId}/upload: Upload a PDF for an appointment.
- GET /api/appointment-pdfs/{clientId}/{appointmentId}: Get PDFs related to an appointment.
- DELETE /api/appointment-pdfs/{clientId}/{appointmentId}/delete/{pdfId}: Delete a specific PDF for an appointment.

5. Statistics

- GET /api/client/total: Get total number of clients.
- GET /api/client/created/forEachLast6Months: Get number of clients created in each of the last six months.
- GET /api/appointment/countByType: Get count of appointments categorized by type.

# Security

- JWT is used to secure the endpoints, with roles of USER and ADMIN.
- Ensure that the correct Bearer token is included in the Authorization header when making requests to protected endpoints.

# Notes

- Database Configuration: Ensure the MySQL database is running, and the credentials match the configuration in application.properties.
- AWS S3: Make sure the S3 bucket exists and the access keys provided have the necessary permissions to upload, read, and delete files from the bucket.

# License
This project is licensed under the MIT License.

