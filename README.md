# Synchrony Project

## Overview

This is a Spring Boot-based REST application that provides endpoints to manage user profiles and images. The application integrates with the Imgur API to allow users to upload, view, and delete images, associating the images with their profiles. User authentication is handled using username and password stored in an in-memory H2 database. The app is designed with scalability, security, and integration in mind.

## Features

- **User Registration**: Allows registration of a user with basic information, including username and password.
- **Image Management**: Users can upload, view, and delete images, with their details being associated with the user profile.
- **Authentication**: Secures the API using JWT for user authentication.
- **Database**: Stores user data (username, password) in an H2 in-memory database, leveraging JPA for easy querying.
- **Imgur Integration**: Integrates with the Imgur API to handle image uploads, viewing, and deletions.  
- **Logging**: Includes logging for tracking application events.
- **Unit Testing**: JUnit test cases are implemented to ensure functionality and correctness.

## Prerequisites

- JDK 17 or higher
- Spring Boot 3.x.x
- H2 Database (in-memory)
- Imgur API Account (Sign in via Twitter/Facebook/Gmail)
- JWT libraries for securing the API


## Setup

1. **Clone the Repository**:
   ```bash
   git clone <repo_url>
   cd synchrony-project
   ```

2. **Dependencies**: The project uses the following key dependencies:
   - Spring Boot 3.3.7
   - Spring Security (JWT)
   - JPA (H2 Database)
   - Imgur API Integration
   - Apache Kafka (for messaging)

3. **Configuration**:
   - Set up your Imgur API credentials by registering your app on Imgur and obtaining the necessary client ID and secret.
   - Add the credentials to the `application.properties` file.

   ```properties
   imgur.client-id=<your-client-id>
   imgur.client-secret=<your-client-secret>
   ```

4. **Run the Application**:
   ```bash
   ./mvnw spring-boot:run
   ```

5. **Testing**: Unit tests are implemented and can be run with:
   ```bash
   ./mvnw test
   ```

## API Endpoints

### 1. Register User

- **POST** `/api/users/register`
- **Request Body**:
  ```json
  {
    "username": "user1",
    "password": "password123"
  }
  ```

### 2. Upload Image

- **POST** `/api/images/upload`
- **Request Body**: 
  - Requires the image file to be uploaded along with user authentication.
  
### 3. View Images

- **GET** `/api/images`
- Returns a list of images associated with the authenticated user.

### 4. Delete Image

- **DELETE** `/api/images/{imageId}`
- Deletes the image associated with the authenticated user.

### 5. View User Profile

- **GET** `/api/users/profile`
- Returns the basic user information and associated images.

## Remaining Features

The following features are still left to be implemented:

1. **Secure API via OAuth2**: Implement OAuth2 for securing the API with proper authentication and authorization mechanisms.
2. **Optimize API for 100K RPM**: Ensure the API can handle high traffic by implementing optimizations like caching, load balancing, and database optimizations.
3. **CI/CD Pipeline**: Set up a Continuous Integration and Continuous Deployment pipeline using open-source tools (e.g., Jenkins, GitLab CI).
4. **Messaging Event**: Implement an event publishing system that sends the username and image name to a messaging platform like Kafka.

## License

MIT License
