const TASK_API = '/api/tasks';

let currentUser = null;
let tasks = [];
let activeTab = 'all';
let selectedPriority = 3;

const page = document.getElementById('page');
const message = document.getElementById('message');
const loader = document.getElementById('loader');
const loaderText = document.getElementById('loaderText');
const tasksGrid = document.getElementById('tasksGrid');

document.addEventListener('DOMContentLoaded', initialize);

async function initialize() {
    document.getElementById('dueDate').valueAsDate = new Date();
    bindEvents();

    if (!await loadCurrentUser()) return;

    page.hidden = false;
    await fetchData();
    window.setInterval(fetchData, 10000);
}

function bindEvents() {
    document.getElementById('logoutButton').addEventListener('click', logout);
    document.getElementById('taskForm').addEventListener('submit', createTask);
    document.getElementById('sortSelect').addEventListener('change', renderTasks);

    document.querySelectorAll('.priority-button').forEach(button => {
        button.addEventListener('click', () => setPriority(Number(button.dataset.priority)));
    });

    document.querySelectorAll('.tab').forEach(button => {
        button.addEventListener('click', () => setTab(button.dataset.tab));
    });

    tasksGrid.addEventListener('click', event => {
        const button = event.target.closest('button[data-action]');
        if (!button) return;

        const id = Number(button.dataset.id);
        if (button.dataset.action === 'claim') claimTask(id);
        if (button.dataset.action === 'complete') completeTask(id);
        if (button.dataset.action === 'reopen') reopenTask(id);
        if (button.dataset.action === 'delete') deleteTask(id);
    });
}

async function loadCurrentUser() {
    try {
        const response = await fetch('/api/auth/me');
        if (!response.ok) {
            window.location.replace('/auth/login.html');
            return false;
        }

        currentUser = await response.json();
        document.getElementById('userName').textContent = currentUser.name;
        return true;
    } catch (error) {
        window.location.replace('/auth/login.html');
        return false;
    }
}

async function logout() {
    try {
        await fetch('/api/auth/logout', { method: 'POST' });
    } finally {
        window.location.replace('/auth/login.html');
    }
}

async function fetchData() {
    try {
        const [tasksResponse, statsResponse] = await Promise.all([
            fetch(TASK_API),
            fetch(`${TASK_API}/dashboard`)
        ]);

        if (tasksResponse.status === 401 || statsResponse.status === 401) {
            window.location.replace('/auth/login.html');
            return;
        }
        if (!tasksResponse.ok || !statsResponse.ok) {
            throw new Error('Could not load task information.');
        }

        tasks = await tasksResponse.json();
        updateStats(await statsResponse.json());
        renderTasks();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

function updateStats(stats) {
    document.getElementById('totalTask').textContent = stats.totalTask;
    document.getElementById('openTask').textContent = stats.openTask;
    document.getElementById('claimedTask').textContent = stats.claimedTask;
    document.getElementById('completedTask').textContent = stats.completedTask;
    document.getElementById('totalPoints').textContent = stats.totalRewardPoints;
}

function setPriority(priority) {
    selectedPriority = priority;
    document.querySelectorAll('.priority-button').forEach(button => {
        button.classList.toggle('active', Number(button.dataset.priority) === priority);
    });
}

function setTab(tab) {
    activeTab = tab;
    document.querySelectorAll('.tab').forEach(button => {
        button.classList.toggle('active', button.dataset.tab === tab);
    });
    renderTasks();
}

function renderTasks() {
    const sortBy = document.getElementById('sortSelect').value;
    let visibleTasks = [...tasks];

    if (activeTab === 'open') {
        visibleTasks = visibleTasks.filter(task => task.status === 'OPEN');
    } else if (activeTab === 'my-claimed') {
        visibleTasks = visibleTasks.filter(task =>
            task.status === 'CLAIMED' && isClaimedByCurrentUser(task)
        );
    } else if (activeTab === 'completed') {
        visibleTasks = visibleTasks.filter(task => task.status === 'COMPLETED');
    }

    visibleTasks.sort((first, second) => {
        if (sortBy === 'dueDate') return new Date(first.dueDate) - new Date(second.dueDate);
        if (sortBy === 'priority-desc') return second.priority - first.priority;
        if (sortBy === 'priority-asc') return first.priority - second.priority;
        if (sortBy === 'points-desc') return second.rewardPoints - first.rewardPoints;
        return 0;
    });

    if (visibleTasks.length === 0) {
        tasksGrid.innerHTML = '<div class="empty"><h3>No tasks found</h3><p>Create a task or choose another filter.</p></div>';
        return;
    }

    tasksGrid.innerHTML = visibleTasks.map(taskCard).join('');
}

function taskCard(task) {
    let action = '';
    if (task.status === 'OPEN') {
        action = actionButton('claim', task.id, 'Claim', 'button');
    } else if (task.status === 'CLAIMED' && isClaimedByCurrentUser(task)) {
        action = actionButton('complete', task.id, 'Complete', 'button success');
    } else if (task.status === 'CLAIMED') {
        action = `<span>Claimed by ${escapeHTML(task.claimedBy)}</span>`;
    } else if (task.status === 'COMPLETED') {
        action = actionButton('reopen', task.id, 'Reopen', 'button warning');
    }

    return `
        <article class="task-card">
            <div>
                <div class="task-head">
                    <div class="badges">
                        <span class="badge priority">P${task.priority}</span>
                        <span class="badge status-${task.status.toLowerCase()}">${task.status}</span>
                    </div>
                    <button class="delete-button" data-action="delete" data-id="${task.id}"
                            type="button" title="Delete task">✕</button>
                </div>
                <h3 class="task-title">${escapeHTML(task.title)}</h3>
                <p class="task-description">${escapeHTML(task.description || 'No description provided.')}</p>
            </div>
            <div>
                <div class="task-meta">
                    <div class="task-meta-row">
                        <span>Creator: ${escapeHTML(task.createdBy)}</span>
                        <span>+${task.rewardPoints}</span>
                    </div>
                    <p>Due: ${formatDate(task.dueDate)}</p>
                </div>
                <div class="task-actions">${action}</div>
            </div>
        </article>`;
}

function actionButton(action, id, label, className) {
    return `<button class="${className}" data-action="${action}" data-id="${id}" type="button">${label}</button>`;
}

async function createTask(event) {
    event.preventDefault();
    setLoading(true, 'Creating task...');

    const task = {
        title: document.getElementById('title').value.trim(),
        description: document.getElementById('description').value.trim(),
        dueDate: document.getElementById('dueDate').value,
        priority: selectedPriority,
        rewardPoints: Number(document.getElementById('rewardPoints').value)
    };

    try {
        await apiRequest(TASK_API, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(task)
        });

        document.getElementById('taskForm').reset();
        document.getElementById('dueDate').valueAsDate = new Date();
        document.getElementById('rewardPoints').value = 10;
        setPriority(3);
        showMessage('Task created successfully.', 'success');
        await fetchData();
    } catch (error) {
        showMessage(error.message, 'error');
    } finally {
        setLoading(false);
    }
}

async function claimTask(id) {
    await runTaskAction(`${TASK_API}/${id}/claim`, 'PATCH', 'Task claimed successfully.');
}

function isClaimedByCurrentUser(task) {
    if (task.claimedByUserId !== null && task.claimedByUserId !== undefined) {
        return Number(task.claimedByUserId) === Number(currentUser.id);
    }

    // Keeps tasks claimed before this update visible in "Claimed by Me".
    return task.claimedBy === currentUser.name;
}

async function completeTask(id) {
    await runTaskAction(`${TASK_API}/${id}/complete`, 'PATCH', 'Task completed successfully.');
}

async function reopenTask(id) {
    await runTaskAction(`${TASK_API}/${id}/reopen`, 'PATCH', 'Task reopened successfully.');
}

async function deleteTask(id) {
    if (!window.confirm('Delete this task permanently?')) return;
    await runTaskAction(`${TASK_API}/${id}`, 'DELETE', 'Task deleted successfully.');
}

async function runTaskAction(url, method, successMessage) {
    setLoading(true, 'Updating task...');
    try {
        await apiRequest(url, { method });
        showMessage(successMessage, 'success');
        await fetchData();
    } catch (error) {
        showMessage(error.message, 'error');
    } finally {
        setLoading(false);
    }
}

async function apiRequest(url, options) {
    const response = await fetch(url, options);
    if (response.status === 401) {
        window.location.replace('/auth/login.html');
        throw new Error('Your session expired. Please log in again.');
    }
    if (!response.ok) {
        let errorMessage = 'Request failed.';
        try {
            const body = await response.json();
            errorMessage = body.message || errorMessage;
        } catch (_) {
            // Response did not contain JSON.
        }
        throw new Error(errorMessage);
    }
    return response;
}

function formatDate(dateString) {
    if (!dateString) return 'No date';
    return new Date(`${dateString}T00:00:00`).toLocaleDateString();
}

function escapeHTML(value) {
    return String(value ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function setLoading(show, text = 'Loading...') {
    loaderText.textContent = text;
    loader.classList.toggle('show', show);
}

function showMessage(text, type) {
    message.textContent = text;
    message.className = `message show ${type}`;
    window.setTimeout(() => message.className = 'message', 4000);
}
