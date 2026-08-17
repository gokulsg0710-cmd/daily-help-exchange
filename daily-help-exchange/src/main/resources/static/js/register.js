const form = document.getElementById('registerForm');
const message = document.getElementById('message');
const loader = document.getElementById('loader');

form.addEventListener('submit', async event => {
    event.preventDefault();
    showMessage('', '');

    const name = document.getElementById('name').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
        showMessage('Passwords do not match.', 'error');
        return;
    }

    loader.classList.add('show');
    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, phone, password })
        });
        const data = await response.json();
        if (!response.ok) throw new Error(data.message || 'Registration failed.');

        sessionStorage.setItem('registrationMessage', 'Registration successful. Please log in.');
        window.location.replace('/auth/login.html');
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
