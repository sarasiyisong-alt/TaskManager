function app() {
    return {
        auth: {
            user: null,
            token: false // using boolean to track if logged in based on /me
        },
        loading: true,
        loginForm: {
            username: '',
            password: ''
        },
        loginError: '',

        view: 'list', // 'list' or 'calendar'

        // Data
        tasks: [],
        allUsers: [],
        userList: [], // for admin

        // Filters
        filters: {
            status: 'ALL',
            sortBy: 'date'
        },

        // Task Modal
        showTaskModal: false,
        newTask: {
            title: '',
            description: '',
            priority: 3,
            assignedUserId: ''
        },

        // User Modal
        showUserModal: false,
        newUser: {
            username: '',
            password: '',
            email: '',
            role: 'USER'
        },

        // Calendar State
        currentWeekStart: new Date(),

        initApp() {
            // Align currentWeekStart to Sunday
            const d = new Date();
            const day = d.getDay();
            const diff = d.getDate() - day;
            this.currentWeekStart = new Date(d.setDate(diff));

            this.checkAuth();
        },

        // --- Auth ---

        async checkAuth() {
            this.loading = true;
            try {
                const res = await fetch('/api/auth/me');
                if (res.ok) {
                    const user = await res.json();
                    this.auth.user = user;
                    this.auth.token = true; // logged in
                    this.fetchTasks();

                    if (this.hasRole('ADMIN') || this.hasRole('MANAGER')) {
                        this.fetchUsers(); // for list and dropdown
                        this.fetchAllUsersForDropdown();
                    }
                } else {
                    this.auth.user = null;
                    this.auth.token = false;
                }
            } catch (e) {
                console.error("Auth check failed", e);
                this.auth.token = false;
            } finally {
                this.loading = false;
            }
        },

        async login() {
            this.loading = true;
            this.loginError = '';

            const formData = new FormData();
            formData.append('username', this.loginForm.username);
            formData.append('password', this.loginForm.password);

            try {
                const res = await fetch('/login', {
                    method: 'POST',
                    body: formData
                });

                if (res.ok || res.redirected) {
                    // Successful login usually redirects or returns OK
                    await this.checkAuth();
                    if (this.auth.token) {
                        this.loginForm.username = '';
                        this.loginForm.password = '';
                    } else {
                        // Even if 200, maybe not auth?
                        // If standard login page redirect, we might be scraping HTML. 
                        // Assuming backend respects REST or standard cookie session.
                        this.loginError = 'Login failed. Please check credentials.';
                    }
                } else {
                    this.loginError = 'Invalid username or password';
                }
            } catch (e) {
                this.loginError = 'Network error during login';
            } finally {
                this.loading = false;
            }
        },

        async logout() {
            try {
                await fetch('/logout', { method: 'POST' });
                this.auth.user = null;
                this.auth.token = false;
                this.view = 'list';
            } catch (e) {
                console.error("Logout failed", e);
            }
        },

        hasRole(role) {
            return this.auth.user?.roles?.some(r => r.authority === 'ROLE_' + role);
        },

        getRoleLabel() {
            if (!this.auth.user?.roles?.length) return '';
            return this.auth.user.roles[0].authority.replace('ROLE_', '');
        },

        // --- Users ---

        async fetchUsers() {
            // For admin table
            try {
                const res = await fetch('/api/users');
                if (res.ok) {
                    this.userList = await res.json();
                }
            } catch (e) {
                console.error(e);
            }
        },

        async fetchAllUsersForDropdown() {
            try {
                const res = await fetch('/api/users'); // reuse same endpoint
                if (res.ok) {
                    this.allUsers = await res.json();
                }
            } catch (e) {
                console.error(e);
            }
        },

        async createUser() {
            if (!confirm('Create this user?')) return;
            try {
                const res = await fetch('/api/users', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(this.newUser)
                });

                if (!res.ok) {
                    const txt = await res.text();
                    alert("Failed: " + txt);
                    return;
                }

                this.newUser = { username: '', password: '', email: '', role: 'USER' };
                this.fetchUsers();
                this.fetchAllUsersForDropdown();
            } catch (e) {
                alert(e.message);
            }
        },

        async deleteUser(id) {
            if (!confirm('Delete this user?')) return;
            try {
                const res = await fetch(`/api/users/${id}`, { method: 'DELETE' });
                if (!res.ok) {
                    const json = await res.json().catch(() => ({}));
                    alert(json.message || "Failed to delete");
                    return;
                }
                this.fetchUsers();
                this.fetchAllUsersForDropdown();
            } catch (e) {
                alert(e.message);
            }
        },

        // --- Tasks ---

        async fetchTasks() {
            try {
                const res = await fetch('/api/tasks');
                let data = await res.json();

                // Client-side filtering
                if (this.filters.status !== 'ALL') {
                    data = data.filter(t => t.status === this.filters.status);
                }

                // Sorting
                data.sort((a, b) => {
                    if (this.filters.sortBy === 'priority') {
                        return a.priority - b.priority;
                    } else {
                        // Date newest first
                        return new Date(b.createdDate) - new Date(a.createdDate);
                    }
                });

                this.tasks = data;
            } catch (e) {
                console.error("Fetch tasks failed", e);
            }
        },

        openTaskModal() {
            this.newTask = {
                title: '',
                description: '',
                priority: 3,
                assignedUserId: '' // default logic handled by backend usually, or passing null
            };
            this.showTaskModal = true;
        },

        async createTask() {
            const payload = {
                title: this.newTask.title,
                description: this.newTask.description,
                priority: parseInt(this.newTask.priority),
                assignedUserId: this.newTask.assignedUserId ? parseInt(this.newTask.assignedUserId) : null
            };

            try {
                const res = await fetch('/api/tasks', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!res.ok) throw new Error(await res.text());

                this.showTaskModal = false;
                this.fetchTasks();
            } catch (e) {
                alert("Error: " + e.message);
            }
        },

        // Can delete logic: Admin, or Creator
        canDelete(task) {
            if (this.hasRole('ADMIN')) return true;
            // Assumes task.createUser is populated
            if (task.createUser?.id === this.auth.user?.id) return true;
            return false;
        },

        async deleteTask(id) {
            if (!confirm("Delete task?")) return;
            try {
                const res = await fetch(`/api/tasks/${id}`, { method: 'DELETE' });
                if (res.ok) {
                    this.fetchTasks();
                } else {
                    alert("Failed to delete task");
                }
            } catch (e) {
                alert("Error: " + e.message);
            }
        },

        async updateTaskStatus(id, status) {
            try {
                await fetch(`/api/tasks/${id}/approve`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ status })
                });
                this.fetchTasks();
            } catch (e) {
                alert("Error updating status");
            }
        },

        async exportCsv() {
            try {
                const res = await fetch('/api/tasks/export');
                if (!res.ok) throw new Error('Export failed');
                const blob = await res.blob();
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = 'tasks.csv';
                document.body.appendChild(a);
                a.click();
                window.URL.revokeObjectURL(url);
                document.body.removeChild(a);
            } catch (err) {
                alert('Error exporting CSV: ' + err.message);
            }
        },

        // --- Calendar ---

        changeWeek(days) {
            const d = new Date(this.currentWeekStart);
            d.setDate(d.getDate() + days);
            this.currentWeekStart = d;
        },

        jumpToDate(dateStr) {
            if (!dateStr) return;
            const d = new Date(dateStr);
            const day = d.getDay();
            const diff = d.getDate() - day;
            this.currentWeekStart = new Date(d.setDate(diff));
        },

        get weekRangeText() {
            const start = this.currentWeekStart;
            const end = new Date(start);
            end.setDate(start.getDate() + 6);
            const opts = { month: 'short', day: 'numeric' };
            return `${start.toLocaleDateString(undefined, opts)} - ${end.toLocaleDateString(undefined, opts)}`;
        },

        get calendarDays() {
            const days = [];
            const today = new Date();
            for (let i = 0; i < 7; i++) {
                const d = new Date(this.currentWeekStart);
                d.setDate(d.getDate() + i);

                const dateStr = d.toISOString().split('T')[0];
                const dayTasks = this.tasks.filter(t => {
                    // Task createdDate is ISO timestamp probably, or array?
                    // Original code: new Date(t.createdDate).toDateString()
                    // Assuming ISO string
                    const tDate = new Date(t.createdDate);
                    return tDate.toDateString() === d.toDateString();
                });

                days.push({
                    dayNum: d.getDate(),
                    dateStr: dateStr,
                    isToday: d.toDateString() === today.toDateString(),
                    tasks: dayTasks
                });
            }
            return days;
        },

        // --- Helpers ---

        formatDate(isoStr) {
            if (!isoStr) return '';
            return new Date(isoStr).toLocaleDateString();
        },

        getStatusBadgeClass(status) {
            switch (status) {
                case 'APPROVED': return 'bg-emerald-50 text-emerald-600';
                case 'REJECTED': return 'bg-red-50 text-red-600';
                default: return 'bg-amber-50 text-amber-600';
            }
        }
    }
}
