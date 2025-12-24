// Detect environment
const API_URL = window.location.hostname === 'localhost'
    ? 'http://localhost:8080/api'
    : '/api';

// Authentication state
let currentUser = null;

// Check auth on page load
window.addEventListener('DOMContentLoaded', async () => {
    await checkAuth();
});

async function checkAuth() {
    try {
        const response = await fetch(`${API_URL}/auth/me`, {
            credentials: 'include'
        });
        if (response.ok) {
            currentUser = await response.json();
            updateUIForAuthState(true);
        } else {
            currentUser = null;
            updateUIForAuthState(false);
        }
    } catch (error) {
        console.error('Auth check failed:', error);
        currentUser = null;
        updateUIForAuthState(false);
    }
}

function updateUIForAuthState(isLoggedIn) {
    const createBtn = Array.from(document.querySelectorAll('.tab-btn')).find(
        btn => btn.textContent.toLowerCase().includes('create')
    );

    if (isLoggedIn) {
        document.getElementById('logged-in-state').style.display = 'flex';
        document.getElementById('logged-out-state').style.display = 'none';
        document.getElementById('user-display').textContent = `Hello, ${currentUser.name}!`;
        if (createBtn) createBtn.disabled = false;
    } else {
        document.getElementById('logged-in-state').style.display = 'none';
        document.getElementById('logged-out-state').style.display = 'flex';
        if (createBtn) createBtn.disabled = true;
    }
}

// Auth modal functions
function showAuthModal(mode) {
    document.getElementById('auth-modal').style.display = 'block';
    if (mode === 'login') {
        showLogin();
    } else {
        showRegister();
    }
}

function closeAuthModal() {
    document.getElementById('auth-modal').style.display = 'none';
}

function showLogin() {
    document.getElementById('login-view').style.display = 'block';
    document.getElementById('register-view').style.display = 'none';
}

function showRegister() {
    document.getElementById('login-view').style.display = 'none';
    document.getElementById('register-view').style.display = 'block';
}

// Login function
async function login(email, password) {
    try {
        const response = await fetch(`${API_URL}/auth/login`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            credentials: 'include',
            body: JSON.stringify({email, password})
        });

        if (response.ok) {
            await checkAuth();
            closeAuthModal();
            alert('Login successful!');
        } else {
            const error = await response.json();
            alert(error.error || 'Login failed');
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed');
    }
}

// Register function
async function register(name, email, password) {
    try {
        const response = await fetch(`${API_URL}/auth/register`, {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({name, email, password})
        });

        if (response.ok) {
            alert('Registration successful! Please login.');
            showLogin();
        } else {
            const error = await response.json();
            alert(error.error || 'Registration failed');
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert('Registration failed');
    }
}

// Logout function
async function logout() {
    try {
        await fetch(`${API_URL}/auth/logout`, {
            method: 'POST',
            credentials: 'include'
        });
        currentUser = null;
        updateUIForAuthState(false);
        showTab('events');
    } catch (error) {
        console.error('Logout error:', error);
    }
}

function showTab(tabName, event) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    document.getElementById(`${tabName}-tab`).classList.add('active');

    if (event && event.target) {
        event.target.classList.add('active');
    } else {
        const btn = Array.from(document.querySelectorAll('.tab-btn')).find(
            btn => btn.textContent.toLowerCase().includes(tabName)
        );
        if (btn) btn.classList.add('active');
    }

    if (tabName === 'events') {
        loadEvents();
    }
}

async function loadEvents() {
    try {
        const response = await fetch(`${API_URL}/events`);
        const events = await response.json();

        const eventsList = document.getElementById('events-list');

        if (events.length === 0) {
            eventsList.innerHTML = '<div class="empty-state">No events yet. Create your first event!</div>';
            return;
        }

        eventsList.innerHTML = events.map(event => {
            const eventDate = new Date(event.eventDate);
            return `
                <div class="event-card" onclick="showEventDetails(${event.id})">
                    <h3>${event.title}</h3>
                    <p>${event.description || 'No description'}</p>
                    <div class="event-meta">
                        <span>📅 ${eventDate.toLocaleDateString()} ${eventDate.toLocaleTimeString()}</span>
                        <span>📍 ${event.location}</span>
                        <span>👤 ${event.organizerName}</span>
                    </div>
                </div>
            `;
        }).join('');
    } catch (error) {
        console.error('Error loading events:', error);
    }
}

async function showEventDetails(eventId) {
    try {
        window.history.pushState({eventId}, '', `?event=${eventId}`);

        const [eventResponse, rsvpsResponse, driversResponse] = await Promise.all([
            fetch(`${API_URL}/events/${eventId}`),
            fetch(`${API_URL}/rsvps/event/${eventId}`),
            fetch(`${API_URL}/drivers/event/${eventId}`)
        ]);

        const event = await eventResponse.json();
        const rsvps = await rsvpsResponse.json();
        const drivers = await driversResponse.json();

        const eventDate = new Date(event.eventDate);

        const yesCount = rsvps.filter(r => r.status === 'YES').length;
        const noCount = rsvps.filter(r => r.status === 'NO').length;

        const shareUrl = `${window.location.origin}/?event=${event.id}`;

        document.getElementById('home-view').style.display = 'none';
        document.getElementById('event-view').style.display = 'block';

        document.getElementById('event-details').innerHTML = `
            <header>
                <h1>${event.title}</h1>
                <p>${event.description || 'No description'}</p>
            </header>

            <div class="event-info">
                <div class="info-item">
                    <strong>📅 Date & Time:</strong>
                    <span>${eventDate.toLocaleDateString()} at ${eventDate.toLocaleTimeString()}</span>
                </div>
                <div class="info-item">
                    <strong>📍 Location:</strong>
                    <span>${event.location}</span>
                </div>
                <div class="info-item">
                    <strong>👤 Organizer:</strong>
                    <span>${event.organizerName} (${event.organizerEmail})</span>
                </div>
            </div>

            <div class="share-link-container">
                <label><strong>Share this event:</strong></label>
                <div class="share-link-box">
                    <input type="text" id="share-url" value="${shareUrl}" readonly>
                    <button class="btn btn-primary" onclick="copyShareLink()">Copy Link</button>
                </div>
            </div>

            <div class="rsvp-stats">
                <div class="stat">
                    <div class="stat-value">${yesCount}</div>
                    <div class="stat-label">Attending</div>
                </div>
                <div class="stat">
                    <div class="stat-value">${noCount}</div>
                    <div class="stat-label">Not Attending</div>
                </div>
            </div>
        `;

        const carpoolFields = event.carpoolMode === 'AUTO' ? `
            <div class="carpool-required-notice">
                <strong>⚠️ Carpool Information Required</strong>
                <p>This event uses automatic carpooling. Please provide your transportation details.</p>
            </div>
            <div class="form-group">
                <label>Are you driving? *</label>
                <div style="display: flex; gap: 15px;">
                    <label><input type="radio" name="isDriver" value="true" required onchange="toggleSeatsField(true)"> Yes, I can drive</label>
                    <label><input type="radio" name="isDriver" value="false" required onchange="toggleSeatsField(false)"> No, I need a ride</label>
                </div>
            </div>
            <div id="driver-seats-field" style="display: none;">
                <div class="form-group">
                    <label for="seats">Available Seats *</label>
                    <input type="number" id="seats" min="1" max="8" value="4">
                </div>
            </div>
            <div class="form-group">
                <label for="arrivalTime">Your Arrival Time *</label>
                <input type="datetime-local" id="arrivalTime" step="900" required>
            </div>
        ` : '';

        document.getElementById('rsvp-form-container').innerHTML = `
            <h3>RSVP to this Event</h3>
            <form id="rsvp-form" onsubmit="submitRsvp(event, ${eventId})">
                <div class="form-group">
                    <label for="guestName">Your Name *</label>
                    <input type="text" id="guestName" required>
                </div>
                <div class="form-group">
                    <label for="guestEmail">Your Email *</label>
                    <input type="email" id="guestEmail" required>
                </div>
                ${carpoolFields}
                <div class="form-group">
                    <label for="message">Message (optional)</label>
                    <textarea id="message" rows="2"></textarea>
                </div>
                <div style="display: flex; gap: 10px;">
                    <button type="submit" class="btn btn-success" onclick="setRsvpStatus('YES')">I'll be there!</button>
                    <button type="submit" class="btn btn-danger" onclick="setRsvpStatus('NO')">Can't make it</button>
                </div>
            </form>

            ${event.carpoolMode === 'MANUAL' ? `
                <div class="driver-section">
                    <h3>Offer a Ride</h3>
                    <form id="driver-form" onsubmit="submitDriver(event, ${eventId})">
                        <div class="form-group">
                            <label for="driverName">Your Name *</label>
                            <input type="text" id="driverName" required>
                        </div>
                        <div class="form-group">
                            <label for="driverEmail">Your Email *</label>
                            <input type="email" id="driverEmail" required>
                        </div>
                        <div class="form-group">
                            <label for="departureTime">Departure Time *</label>
                            <input type="datetime-local" id="departureTime" step="900" required>
                        </div>
                        <div class="form-group">
                            <label for="capacity">Available Seats *</label>
                            <input type="number" id="capacity" min="1" max="8" required>
                        </div>
                        <div class="form-group">
                            <label for="carDetails">Car Details (optional)</label>
                            <input type="text" id="carDetails" placeholder="e.g., Blue Toyota Camry">
                        </div>
                        <button type="submit" class="btn btn-primary">Become a Driver</button>
                    </form>
                </div>
            ` : ''}

            ${event.carpoolMode !== 'NONE' ? `
                <div id="drivers-list">
                    <h3>${event.carpoolMode === 'AUTO' ? 'Carpool Assignments' : 'Available Rides'} (${drivers.length})</h3>
                    ${drivers.length === 0 ? `<p class="empty-state">${event.carpoolMode === 'AUTO' ? 'No carpools assigned yet. RSVP to get assigned!' : 'No drivers yet. Be the first to offer a ride!'}</p>` :
                        drivers.map(driver => `
                            <div class="driver-card">
                                <div class="driver-header">
                                    <h4>🚗 ${driver.driverName}</h4>
                                    <span class="departure-time">Leaving: ${new Date(driver.departureTime).toLocaleString()}</span>
                                </div>
                                ${driver.carDetails ? `<p class="car-details">${driver.carDetails}</p>` : ''}
                                <div class="passenger-info">
                                    <span class="seats-available">${driver.spotsAvailable} of ${driver.capacity} seats available</span>
                                    ${driver.passengers.length > 0 ? `
                                        <div class="passengers-list">
                                            <strong>Passengers:</strong>
                                            <ul>
                                                ${driver.passengers.map(p => `<li>${p.name}</li>`).join('')}
                                            </ul>
                                        </div>
                                    ` : ''}
                                </div>
                                ${event.carpoolMode === 'MANUAL' ? `
                                    <div class="driver-actions">
                                        <input type="email" id="join-email-${driver.id}" placeholder="Your email" class="join-email-input">
                                        <button onclick="joinCar(${driver.id}, ${eventId})" class="btn btn-success btn-sm" ${driver.spotsAvailable === 0 ? 'disabled' : ''}>
                                            Join This Ride
                                        </button>
                                        <button onclick="leaveCar(${driver.id}, ${eventId})" class="btn btn-danger btn-sm">
                                            Leave Ride
                                        </button>
                                    </div>
                                ` : ''}
                            </div>
                        `).join('')
                    }
                </div>
            ` : ''}
        `;

        document.getElementById('rsvps-list').innerHTML = `
            <h3>Responses (${rsvps.length})</h3>
            ${rsvps.length === 0 ? '<p class="empty-state">No responses yet</p>' :
                rsvps.map(rsvp => `
                    <div class="rsvp-item ${rsvp.status.toLowerCase()}">
                        <h4>${rsvp.guestName} - ${rsvp.status === 'YES' ? 'Attending' : 'Not Attending'}</h4>
                        ${rsvp.message ? `<p>${rsvp.message}</p>` : ''}
                        <p style="font-size: 0.8em; margin-top: 5px;">
                            Responded: ${new Date(rsvp.respondedAt).toLocaleString()}
                        </p>
                    </div>
                `).join('')
            }
        `;

        // Apply 15-minute rounding to dynamically created datetime inputs
        const arrivalTimeInput = document.getElementById('arrivalTime');
        if (arrivalTimeInput) {
            roundToNearest15(arrivalTimeInput);
        }

        const departureTimeInput = document.getElementById('departureTime');
        if (departureTimeInput) {
            roundToNearest15(departureTimeInput);
        }

        window.scrollTo(0, 0);
    } catch (error) {
        console.error('Error loading event details:', error);
    }
}

function goHome() {
    document.getElementById('event-view').style.display = 'none';
    document.getElementById('home-view').style.display = 'block';
    window.history.pushState({}, '', '/');
    loadEvents();
}

let selectedRsvpStatus = 'YES';

function setRsvpStatus(status) {
    selectedRsvpStatus = status;
}

function toggleSeatsField(isDriver) {
    const seatsField = document.getElementById('driver-seats-field');
    if (seatsField) {
        seatsField.style.display = isDriver ? 'block' : 'none';
    }
}

async function submitRsvp(event, eventId) {
    event.preventDefault();

    const rsvpData = {
        eventId: eventId,
        guestName: document.getElementById('guestName').value,
        guestEmail: document.getElementById('guestEmail').value,
        status: selectedRsvpStatus,
        message: document.getElementById('message').value
    };

    // Add carpool fields if they exist (AUTO mode)
    const isDriverRadio = document.querySelector('input[name="isDriver"]:checked');
    if (isDriverRadio) {
        rsvpData.isDriver = isDriverRadio.value === 'true';
        rsvpData.arrivalTime = document.getElementById('arrivalTime').value;
        if (rsvpData.isDriver) {
            rsvpData.seats = parseInt(document.getElementById('seats').value);
        }
    }

    try {
        const response = await fetch(`${API_URL}/rsvps`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(rsvpData)
        });

        if (response.ok) {
            showEventDetails(eventId);
        } else {
            alert('Error submitting RSVP');
        }
    } catch (error) {
        console.error('Error submitting RSVP:', error);
        alert('Error submitting RSVP');
    }
}

document.getElementById('create-event-form').addEventListener('submit', async (e) => {
    e.preventDefault();

    if (!currentUser) {
        alert('Please login to create events');
        showAuthModal('login');
        return;
    }

    const eventData = {
        title: document.getElementById('title').value,
        description: document.getElementById('description').value,
        eventDate: document.getElementById('eventDate').value,
        location: document.getElementById('location').value,
        carpoolMode: document.getElementById('carpoolMode').value
    };

    try {
        const response = await fetch(`${API_URL}/events`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(eventData)
        });

        if (response.ok) {
            const event = await response.json();
            document.getElementById('create-event-form').reset();
            showEventDetails(event.id);
        } else {
            alert('Error creating event. Please make sure you are logged in.');
        }
    } catch (error) {
        console.error('Error creating event:', error);
        alert('Error creating event');
    }
});

window.addEventListener('popstate', (event) => {
    if (event.state && event.state.eventId) {
        showEventDetails(event.state.eventId);
    } else {
        goHome();
    }
});

function copyShareLink() {
    const shareUrl = document.getElementById('share-url');
    shareUrl.select();
    shareUrl.setSelectionRange(0, 99999);
    navigator.clipboard.writeText(shareUrl.value);

    const btn = event.target;
    const originalText = btn.textContent;
    btn.textContent = 'Copied!';
    setTimeout(() => {
        btn.textContent = originalText;
    }, 2000);
}

function checkForSharedEvent() {
    const urlParams = new URLSearchParams(window.location.search);
    const eventId = urlParams.get('event');
    if (eventId) {
        showEventDetails(parseInt(eventId));
    }
}

async function submitDriver(event, eventId) {
    event.preventDefault();

    const driverData = {
        eventId: eventId,
        driverName: document.getElementById('driverName').value,
        driverEmail: document.getElementById('driverEmail').value,
        departureTime: document.getElementById('departureTime').value,
        capacity: parseInt(document.getElementById('capacity').value),
        carDetails: document.getElementById('carDetails').value
    };

    try {
        const response = await fetch(`${API_URL}/drivers`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(driverData)
        });

        if (response.ok) {
            document.getElementById('driver-form').reset();
            showEventDetails(eventId);
        } else {
            alert('Error creating driver');
        }
    } catch (error) {
        console.error('Error creating driver:', error);
        alert('Error creating driver');
    }
}

async function joinCar(driverId, eventId) {
    const email = document.getElementById(`join-email-${driverId}`).value;
    if (!email) {
        alert('Please enter your email');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/drivers/${driverId}/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ guestEmail: email })
        });

        const result = await response.json();
        if (response.ok) {
            showEventDetails(eventId);
        } else {
            alert(result.error || 'Error joining car');
        }
    } catch (error) {
        console.error('Error joining car:', error);
        alert('Error joining car');
    }
}

async function leaveCar(driverId, eventId) {
    const email = document.getElementById(`join-email-${driverId}`).value;
    if (!email) {
        alert('Please enter your email');
        return;
    }

    try {
        const response = await fetch(`${API_URL}/drivers/${driverId}/leave`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ guestEmail: email })
        });

        if (response.ok) {
            showEventDetails(eventId);
        } else {
            alert('Error leaving car');
        }
    } catch (error) {
        console.error('Error leaving car:', error);
        alert('Error leaving car');
    }
}

// Round datetime to nearest 15 minutes
function roundToNearest15(dateTimeInput) {
    dateTimeInput.addEventListener('blur', function() {
        if (!this.value) return;

        const date = new Date(this.value);
        const minutes = date.getMinutes();
        const roundedMinutes = Math.round(minutes / 15) * 15;
        date.setMinutes(roundedMinutes);
        date.setSeconds(0);
        date.setMilliseconds(0);

        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const mins = String(date.getMinutes()).padStart(2, '0');

        this.value = `${year}-${month}-${day}T${hours}:${mins}`;
    });
}

// Apply to event date input on page load
document.addEventListener('DOMContentLoaded', function() {
    const eventDateInput = document.getElementById('eventDate');
    if (eventDateInput) {
        roundToNearest15(eventDateInput);
    }
});

// Form handlers
document.getElementById('login-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    await login(email, password);
});

document.getElementById('register-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = document.getElementById('register-name').value;
    const email = document.getElementById('register-email').value;
    const password = document.getElementById('register-password').value;
    await register(name, email, password);
});

loadEvents();
checkForSharedEvent();
