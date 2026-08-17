const form = document.getElementById('loginForm');
const message = document.getElementById('message');
const loader = document.getElementById('loader');

const registrationMessage = sessionStorage.getItem('registrationMessage');
if (registrationMessage) {
    showMessage(registrationMessage, 'success');
    sessionStorage.removeItem('registrationMessage');
}

checkExistingSession();

async function checkExistingSession() {
    try {
        const response = await fetch('/api/auth/me');
        if (response.ok) window.location.replace('/tasks/home.html');
    } catch (error) {
        console.error('Session check failed:', error);
    }
}

form.addEventListener('submit', async event => {
    event.preventDefault();
    showMessage('', '');
    loader.classList.add('show');

    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ phone, password })
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Invalid phone number or password.');
        window.location.replace('/tasks/home.html');
    } catch (error) {
        showMessage(error.message, 'error');
    } finally {
        loader.classList.remove('show');
    }
});

function showMessage(text, type) {
    message.textContent = text;
    message.className = type ? `message show ${type}` : 'message';
}
