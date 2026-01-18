document.addEventListener('DOMContentLoaded', () => {
    fetchUserInfo();
    setupEventListeners();
    fetchTasks();
});

let currentUser = null;
let allUsers = [];
let currentView = 'list'; // 'list' or 'calendar'
let currentWeekStart = getStartOfWeek(new Date());

function getStartOfWeek(date) {
    const d = new Date(date);
    const day = d.getDay(); // 0 (Sun) to 6 (Sat)
    const diff = d.getDate() - day;
    return new Date(d.setDate(diff));
}

function fetchUserInfo() {
    fetch('/api/auth/me')
        .then(res => res.json())
        .then(user => {
            currentUser = user;
            document.getElementById('currentUser').textContent = `Logged in as: ${user.username} (${user.roles[0].authority})`;

            const role = user.roles[0].authority;
            // Admin and Manager can manage users
            if (role === 'ROLE_ADMIN' || role === 'ROLE_MANAGER') {
                document.getElementById('adminSection').style.display = 'block';
                fetchUsers();
            }
            // Load users for assignment dropdown logic if needed, or do it when opening modal
            if (role === 'ROLE_ADMIN' || role === 'ROLE_MANAGER') {
                fetchAllUsersForAssignment();
            }
        });
}

function fetchAllUsersForAssignment() {
    // Reuse the same endpoint, backend filters for manager
    fetch('/api/users')
        .then(res => res.json())
        .then(users => {
            allUsers = users;
        });
}

function setupEventListeners() {
    // Modal controls
    const modal = document.getElementById('taskModal');
    const btn = document.getElementById('createTaskBtn');
    const span = document.getElementsByClassName("close")[0];

    btn.onclick = () => {
        modal.style.display = "block";
        populateAssigneeDropdown();
    };
    span.onclick = () => modal.style.display = "none";
    window.onclick = (event) => {
        if (event.target == modal) modal.style.display = "none";
        if (event.target == document.getElementById('editUserModal')) document.getElementById('editUserModal').style.display = "none";
    }

    // Export CSV
    document.getElementById('exportCsvBtn').addEventListener('click', () => {
        fetch('/api/tasks/export')
            .then(response => {
                if (!response.ok) throw new Error('Export failed');
                return response.blob();
            })
            .then(blob => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = 'tasks.csv';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            })
            .catch(err => alert('Error exporting CSV: ' + err.message));
    });

    // Create Task
    document.getElementById('newTaskForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const assigneeId = document.getElementById('taskAssignee').value;
        const task = {
            title: document.getElementById('taskTitle').value,
            description: document.getElementById('taskDesc').value,
            priority: document.getElementById('taskPriority').value,
            assignedUserId: assigneeId ? parseInt(assigneeId) : null
        };

        fetch('/api/tasks', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(task)
        }).then(res => {
            if (!res.ok) {
                return res.text().then(text => { throw new Error(text) });
            }
            return res.json();
        }).then(() => {
            modal.style.display = "none";
            document.getElementById('newTaskForm').reset();
            fetchTasks();
        }).catch(err => alert("Error: " + err.message));
    });

    // Filters
    document.getElementById('statusFilter').addEventListener('change', fetchTasks);
    document.getElementById('sortBy').addEventListener('change', fetchTasks);

    // View Switching
    document.getElementById('viewListBtn').addEventListener('click', () => switchView('list'));
    document.getElementById('viewCalendarBtn').addEventListener('click', () => switchView('calendar'));

    // Calendar Navigation
    document.getElementById('prevWeekBtn').addEventListener('click', () => changeWeek(-7));
    document.getElementById('nextWeekBtn').addEventListener('click', () => changeWeek(7));
    document.getElementById('jumpToDate').addEventListener('change', (e) => {
        if (e.target.value) {
            currentWeekStart = getStartOfWeek(new Date(e.target.value));
            fetchTasks(); // Helper to re-render
        }
    });

    // Create User (Admin/Manager)
    const createUserForm = document.getElementById('createUserForm');
    if (createUserForm) {
        createUserForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const newUser = {
                username: document.getElementById('newUsername').value,
                password: document.getElementById('newPassword').value,
                role: document.getElementById('newRole').value,
                email: document.getElementById('newEmail').value
            };

            fetch('/api/users', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newUser)
            }).then(res => {
                if (!res.ok) return res.text().then(text => { throw new Error(text) });
                return res.json();
            }).then(() => {
                document.getElementById('createUserForm').reset();
                fetchUsers();
                if (currentUser.roles[0].authority !== 'ROLE_USER') fetchAllUsersForAssignment();
            }).catch(e => alert(e.message));
        });
    }

    // Edit User Submit
    document.getElementById('editUserForm').addEventListener('submit', (e) => {
        e.preventDefault();
        const id = document.getElementById('editUserId').value;
        const updates = {
            email: document.getElementById('editEmail').value,
            password: document.getElementById('editPassword').value
        };

        fetch(`/api/users/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updates)
        }).then(res => {
            if (!res.ok) return res.text().then(text => { throw new Error(text) });
            return res.json();
        }).then(() => {
            document.getElementById('editUserModal').style.display = "none";
            fetchUsers();
        }).catch(e => alert(e.message));
    });
}

function populateAssigneeDropdown() {
    const select = document.getElementById('taskAssignee');
    select.innerHTML = '';

    // Self option always there
    const selfOption = document.createElement('option');
    selfOption.value = ''; // We handle null or ID check in backend, or passing self ID? 
    // Backend defaults to creator if null. Let's send null for self.
    // Wait, backend logic: "If task.getAssignedUserId() == null task.setAssignedUserId(creator.getId())".
    // So value="" maps to null.
    selfOption.textContent = 'Myself';
    select.appendChild(selfOption);

    if (currentUser.roles[0].authority === 'ROLE_USER') {
        // Only self, done
    } else {
        // Manager/Admin can assign to others
        allUsers.forEach(u => {
            // Don't duplicate self?
            // Actually, usually managers can assign to anyone they manage.
            const opt = document.createElement('option');
            opt.value = u.id;
            opt.textContent = `${u.username} (${u.role})`;
            select.appendChild(opt);
        });
    }
}

function fetchTasks() {
    fetch('/api/tasks')
        .then(res => res.json())
        .then(tasks => {
            const statusFilter = document.getElementById('statusFilter').value;
            const sortBy = document.getElementById('sortBy').value;

            if (statusFilter !== 'ALL') {
                tasks = tasks.filter(t => t.status === statusFilter);
            }

            tasks.sort((a, b) => {
                if (sortBy === 'priority') {
                    return a.priority - b.priority;
                } else {
                    return new Date(b.createdDate) - new Date(a.createdDate);
                }
            });

            if (currentView === 'list') {
                document.getElementById('taskList').style.display = 'grid';
                document.getElementById('calendarSection').style.display = 'none';
                // Sort logic only needed for list view really, but fine to keep
                tasks.sort((a, b) => {
                    if (sortBy === 'priority') {
                        return a.priority - b.priority;
                    } else {
                        return new Date(b.createdDate) - new Date(a.createdDate);
                    }
                });
                renderTaskList(tasks);
            } else {
                document.getElementById('taskList').style.display = 'none';
                document.getElementById('calendarSection').style.display = 'block';
                renderCalendar(tasks);
            }
        });
}

function renderTaskList(tasks) {
    const list = document.getElementById('taskList');
    list.innerHTML = '';

    tasks.forEach(task => {
        const card = document.createElement('div');
        card.className = `task-card ${task.status.toLowerCase()}`;

        let actions = '';
        const role = currentUser ? currentUser.roles[0].authority : '';

        if ((role === 'ROLE_MANAGER' || role === 'ROLE_ADMIN') && task.status === 'PENDING') {
            actions = `
                <div class="task-actions">
                    <button class="btn-approve" onclick="updateStatus(${task.id}, 'APPROVED')">Approve</button>
                    <button class="btn-reject" onclick="updateStatus(${task.id}, 'REJECTED')">Reject</button>
                </div>
            `;
        }

        // Use user objects if returned (populated by JoinColumn/Eager or manually in DTO)
        // Spring Data REST or standard Serialize might serialize the relationships if they are loaded.
        // If FetchType is EAGER for createUser/assignedUser on Task, they will be in JSON.
        // Default conversion might include them.
        const creatorName = task.createUser ? task.createUser.username : 'Unknown';
        const assigneeName = task.assignedUser ? task.assignedUser.username : 'Unassigned';

        card.innerHTML = `
            <div class="task-header">
                <h3>${task.title}</h3>
                <span class="task-status status-${task.status.toLowerCase()}">${task.status}</span>
            </div>
            <p>${task.description || 'No description'}</p>
            <div style="margin-top: 0.5rem; font-size: 0.875rem; color: #666;">
                Priority: ${task.priority} | Created: ${new Date(task.createdDate).toLocaleDateString()}
            </div>
            <div style="margin-top: 0.25rem; font-size: 0.8rem; color: #555;">
                Creator: <b>${creatorName}</b> | Assignee: <b>${assigneeName}</b>
            </div>
            ${actions}
            <div style="margin-top: 10px;">
                 ${canDelete(task) ? `<button class="btn-delete" onclick="deleteTask(${task.id})">Delete Task</button>` : ''}
            </div>
        `;
        list.appendChild(card);
    });
}

function canDelete(task) {
    if (!currentUser) return false;
    const role = currentUser.roles[0].authority;
    if (role === 'ROLE_ADMIN') return true;
    if (task.createUser && task.createUser.id === currentUser.id) return true;
    return false;
}

function deleteTask(id) {
    if (confirm("Are you sure you want to delete this task?")) {
        fetch(`/api/tasks/${id}`, { method: 'DELETE' })
            .then(res => {
                if (res.ok) {
                    fetchTasks();
                } else {
                    res.json().then(json => alert("Failed to delete: " + (json.message || json.error)))
                        .catch(() => res.text().then(text => alert("Failed to delete: " + text)));
                }
            })
            .catch(err => alert("Error: " + err));
    }
}

function updateStatus(id, status) {
    fetch(`/api/tasks/${id}/approve`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: status })
    }).then(() => fetchTasks());
}

function fetchUsers() {
    fetch('/api/users')
        .then(res => res.json())
        .then(users => {
            const list = document.getElementById('userList');
            list.innerHTML = '';

            // Header
            const header = document.createElement('div');
            header.className = 'user-item header';
            header.style.fontWeight = 'bold';
            header.innerHTML = `
                 <span style="flex:1">Username</span>
                 <span style="flex:1">Role</span>
                 <span style="flex:1">Manager</span>
                 <span style="flex:1">Email</span>
                 <span style="width: 120px">Actions</span>
            `;
            list.appendChild(header);

            users.forEach(user => {
                const div = document.createElement('div');
                div.className = 'user-item';
                const managerName = user.manager ? user.manager.username : '-';
                const email = user.email || '-';

                div.innerHTML = `
                    <span style="flex:1">${user.username}</span>
                    <span style="flex:1">${user.role}</span>
                    <span style="flex:1">${managerName}</span>
                    <span style="flex:1">${email}</span>
                    <div style="width: 120px; display:flex; gap: 5px;">
                        <button class="btn-edit" onclick="openEditUser(${user.id}, '${email}')">Edit</button>
                        <button class="btn-delete" onclick="deleteUser(${user.id})">Delete</button>
                    </div>
                `;
                list.appendChild(div);
            });
        });
}

function deleteUser(id) {
    if (confirm('Are you sure?')) {
        fetch(`/api/users/${id}`, { method: 'DELETE' })
            .then(res => {
                if (!res.ok) {
                    return res.json().then(json => alert(json.message || json.error))
                        .catch(() => res.text().then(text => alert(text)));
                }
                fetchUsers();
                if (currentUser.roles[0].authority !== 'ROLE_USER') fetchAllUsersForAssignment();
            });
    }
}

function openEditUser(id, currentEmail) {
    document.getElementById('editUserId').value = id;
    document.getElementById('editEmail').value = currentEmail === '-' ? '' : currentEmail;
    document.getElementById('editPassword').value = '';
    document.getElementById('editUserModal').style.display = 'block';
}


function switchView(view) {
    currentView = view;

    // Update buttons
    const listBtn = document.getElementById('viewListBtn');
    const calBtn = document.getElementById('viewCalendarBtn');

    if (view === 'list') {
        listBtn.classList.add('active');
        calBtn.classList.remove('active');
    } else {
        listBtn.classList.remove('active');
        calBtn.classList.add('active');
    }

    fetchTasks(); // Refresh display
}

function changeWeek(days) {
    currentWeekStart.setDate(currentWeekStart.getDate() + days);
    fetchTasks();
}

function renderCalendar(tasks) {
    const calendarBody = document.getElementById('calendarBody');
    calendarBody.innerHTML = '';

    const weekEnd = new Date(currentWeekStart);
    weekEnd.setDate(currentWeekStart.getDate() + 6);

    // Display range
    const options = { month: 'short', day: 'numeric' };
    document.getElementById('currentWeekRange').textContent =
        `${currentWeekStart.toLocaleDateString(undefined, options)} - ${weekEnd.toLocaleDateString(undefined, options)}`;

    // Create 7 columns
    for (let i = 0; i < 7; i++) {
        const currentDate = new Date(currentWeekStart);
        currentDate.setDate(currentWeekStart.getDate() + i);

        const col = document.createElement('div');
        col.className = 'cal-day-col';

        // Highlight today
        const today = new Date();
        if (currentDate.toDateString() === today.toDateString()) {
            col.classList.add('today');
        }

        // Date label
        const dateLabel = document.createElement('div');
        dateLabel.className = 'cal-date-label';
        dateLabel.textContent = currentDate.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
        col.appendChild(dateLabel);

        // Filter tasks for this day
        // Using createdDate. Need to match YYYY-MM-DD
        const dayTasks = tasks.filter(t => {
            const tDate = new Date(t.createdDate);
            return tDate.toDateString() === currentDate.toDateString();
        });

        // Render tasks
        dayTasks.forEach(task => {
            const taskEl = document.createElement('div');
            taskEl.className = `cal-task-card ${task.status.toLowerCase()}`;
            taskEl.title = `${task.title}\nStatus: ${task.status}\nPriority: ${task.priority}`;

            // Simple click to list view or similar? Or just simple display
            // Let's scroll to it in list view? Or just alert details?
            // For now, simple alerts or maybe reusing edit modal if we had one.
            // Requirement didn't specify interaction, just view.

            taskEl.innerHTML = `<div class="cal-task-title">${task.title}</div>`;
            col.appendChild(taskEl);
        });

        calendarBody.appendChild(col);
    }
}

window.updateStatus = updateStatus;
window.deleteUser = deleteUser;
window.deleteTask = deleteTask;
window.openEditUser = openEditUser;
window.switchView = switchView; // not strictly needed for onclick unless I put it in HTML

