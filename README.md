# HangoutApp - Event Invitation System

A web application for creating events and managing RSVPs. People can view events and respond with "Yes" or "No" to invitations.

## Features

- Create events with title, description, date, location, and organizer info
- View all upcoming events
- RSVP to events with Yes/No response
- Add optional messages with RSVPs
- View real-time RSVP statistics (attending vs not attending)
- See all responses for each event

## Technology Stack

**Backend:**
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database (in-memory)

**Frontend:**
- HTML5
- CSS3
- Vanilla JavaScript

## Prerequisites

- Java 17 or higher (no other tools needed - Gradle wrapper is included)

## Installation and Setup

1. **Clone or navigate to the project directory:**
   ```bash
   cd /Users/rohan/Coding/HangoutApp
   ```

2. **Build the project:**
   ```bash
   ./gradlew build
   ```

3. **Run the application:**
   ```bash
   ./gradlew bootRun
   ```

4. **Access the application:**
   - Open your browser and go to: `http://localhost:8081`
   - The frontend UI will load automatically

**Note:** The first time you run `./gradlew`, it will automatically download Gradle. This may take a minute.

## API Endpoints

### Events

- `GET /api/events` - Get all events
- `GET /api/events/upcoming` - Get upcoming events
- `GET /api/events/{id}` - Get event by ID
- `POST /api/events` - Create new event
- `PUT /api/events/{id}` - Update event
- `DELETE /api/events/{id}` - Delete event

### RSVPs

- `GET /api/rsvps/event/{eventId}` - Get all RSVPs for an event
- `POST /api/rsvps` - Create or update RSVP

### Example: Create Event

```bash
curl -X POST http://localhost:8081/api/events \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Summer BBQ",
    "description": "Join us for a fun BBQ party!",
    "eventDate": "2025-07-15T18:00:00",
    "location": "Central Park",
    "organizerName": "John Doe",
    "organizerEmail": "john@example.com"
  }'
```

### Example: Submit RSVP

```bash
curl -X POST http://localhost:8081/api/rsvps \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": 1,
    "guestName": "Jane Smith",
    "guestEmail": "jane@example.com",
    "status": "YES",
    "message": "Looking forward to it!"
  }'
```

## Database

The application uses H2 in-memory database. Data is reset when the application restarts.

To access the H2 console (for debugging):
- URL: `http://localhost:8081/h2-console`
- JDBC URL: `jdbc:h2:mem:hangoutdb`
- Username: `sa`
- Password: (leave blank)

## Project Structure

```
HangoutApp/
├── src/
│   ├── main/
│   │   ├── java/com/hangout/
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── model/           # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   └── HangoutApplication.java
│   │   └── resources/
│   │       ├── static/          # Frontend files
│   │       │   ├── index.html
│   │       │   ├── styles.css
│   │       │   └── app.js
│   │       └── application.properties
│   └── test/
├── build.gradle
├── settings.gradle
├── gradlew
└── README.md
```

## Usage

1. **Create an Event:**
   - Click on "Create Event" tab
   - Fill in event details
   - Click "Create Event" button

2. **View Events:**
   - Events are listed on the "Events" tab
   - Click on any event to see details

3. **RSVP to an Event:**
   - Click on an event
   - Enter your name and email
   - Add an optional message
   - Click "I'll be there!" or "Can't make it"

4. **View RSVPs:**
   - Click on an event to see all responses
   - View statistics of who's attending vs not attending

## Future Enhancements

- Persistent database (PostgreSQL/MySQL)
- Email notifications
- User authentication
- Event editing and deletion from UI
- Calendar integration
- Guest list management
- Event reminders

## License

This project is open source and available for personal and educational use.
