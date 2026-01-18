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
            priority: 'P2',
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
            // Align currentWeekStart to Sunday (UTC)
            const now = new Date();
            const utc = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate()));
            const day = utc.getUTCDay(); // Sunday is 0
            utc.setUTCDate(utc.getUTCDate() - day);
            this.currentWeekStart = utc;

            this.checkAuth();
        },

        // --- Auth ---

        async checkAuth() {
            this.loading = true;
            try {
                const res = await fetch('/api/auth/me');
                if (res.ok) {
                    const data = await res.json();
                    if (data.authenticated && data.username !== 'anonymousUser') {
                        this.auth.user = data;
                        this.auth.token = true;
                        this.fetchTasks();
                        if (this.hasRole('ADMIN') || this.hasRole('MANAGER')) {
                            this.fetchUsers();
                            this.fetchAllUsersForDropdown();
                        }
                    } else {
                        this.auth.user = null;
                        this.auth.token = false;
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
                        return a.priority.localeCompare(b.priority);
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
                priority: 'P2',
                assignedUserId: '' // default logic handled by backend usually, or passing null
            };
            this.showTaskModal = true;
        },

        async createTask() {
            const payload = {
                title: this.newTask.title,
                description: this.newTask.description,
                priority: this.newTask.priority,
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
            const utc = new Date(Date.UTC(d.getUTCFullYear(), d.getUTCMonth(), d.getUTCDate()));
            const day = utc.getUTCDay();
            utc.setUTCDate(utc.getUTCDate() - day);
            this.currentWeekStart = utc;
        },

        get weekRangeText() {
            const start = this.currentWeekStart;
            const end = new Date(start);
            end.setUTCDate(start.getUTCDate() + 6);

            const format = (d) => d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', timeZone: 'UTC' });
            return `${format(start)} - ${format(end)} (UTC)`;
        },

        get calendarDays() {
            const days = [];
            const now = new Date();
            const todayUTCStr = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate())).toISOString().split('T')[0];

            for (let i = 0; i < 7; i++) {
                const d = new Date(this.currentWeekStart);
                d.setUTCDate(d.getUTCDate() + i);

                const dateStr = d.toISOString().split('T')[0];
                const dayTasks = this.tasks.filter(t => {
                    if (!t.createdDate) return false;
                    const tDateStr = new Date(t.createdDate).toISOString().split('T')[0];
                    return tDateStr === dateStr;
                });

                days.push({
                    dayNum: d.getUTCDate(),
                    dateStr: dateStr,
                    isToday: dateStr === todayUTCStr,
                    tasks: dayTasks
                });
            }
            return days;
        },

        // --- Helpers ---

        formatDate(isoStr) {
            if (!isoStr) return '';
            const d = new Date(isoStr);
            return d.toISOString().replace('T', ' ').substring(0, 16) + ' UTC';
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
