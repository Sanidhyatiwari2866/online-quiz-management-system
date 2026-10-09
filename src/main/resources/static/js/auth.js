/**
 * Authentication Module
 */
const Auth = (() => {
    const init = () => {
        setupEventListeners();
        updateUI();
    };

    const setupEventListeners = () => {
        // Login form submit
        const loginForm = document.getElementById('loginForm');
        if (loginForm) {
            loginForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const username = document.getElementById('loginUsername').value.trim();
                const password = document.getElementById('loginPassword').value;
                const errDiv = document.getElementById('loginError');
                errDiv.classList.add('d-none');

                try {
                    const res = await API.login({ username, password });
                    API.setToken(res.token);
                    API.setUser({
                        id: res.id,
                        username: res.username,
                        email: res.email,
                        fullName: res.fullName,
                        role: res.role
                    });

                    // Hide modal
                    const modalEl = document.getElementById('authModal');
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();

                    updateUI();
                    App.showToast(`Welcome back, ${res.fullName || res.username}!`, 'success');
                    App.loadView('quizzes');
                } catch (err) {
                    errDiv.textContent = err.message;
                    errDiv.classList.remove('d-none');
                }
            });
        }

        // Register form submit
        const registerForm = document.getElementById('registerForm');
        if (registerForm) {
            registerForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                const username = document.getElementById('regUsername').value.trim();
                const fullName = document.getElementById('regFullName').value.trim();
                const email = document.getElementById('regEmail').value.trim();
                const password = document.getElementById('regPassword').value;
                const role = document.getElementById('regRole').value;
                const errDiv = document.getElementById('regError');
                errDiv.classList.add('d-none');

                try {
                    const res = await API.register({ username, fullName, email, password, role });
                    API.setToken(res.token);
                    API.setUser({
                        id: res.id,
                        username: res.username,
                        email: res.email,
                        fullName: res.fullName,
                        role: res.role
                    });

                    const modalEl = document.getElementById('authModal');
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();

                    updateUI();
                    App.showToast('Account created successfully! Welcome to QuizMaster.', 'success');
                    App.loadView('quizzes');
                } catch (err) {
                    errDiv.textContent = err.message;
                    errDiv.classList.remove('d-none');
                }
            });
        }

        // Quick login buttons
        const fillAdminBtn = document.getElementById('btnQuickAdmin');
        if (fillAdminBtn) {
            fillAdminBtn.addEventListener('click', () => {
                document.getElementById('loginUsername').value = 'admin';
                document.getElementById('loginPassword').value = 'admin123';
            });
        }

        const fillStudentBtn = document.getElementById('btnQuickStudent');
        if (fillStudentBtn) {
            fillStudentBtn.addEventListener('click', () => {
                document.getElementById('loginUsername').value = 'student';
                document.getElementById('loginPassword').value = 'student123';
            });
        }

        // Logout
        const logoutBtn = document.getElementById('navLogout');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', (e) => {
                e.preventDefault();
                API.removeToken();
                updateUI();
                App.showToast('You have been logged out.', 'info');
                App.loadView('quizzes');
            });
        }

        // Listen for session expiry
        window.addEventListener('auth-expired', () => {
            updateUI();
            App.showToast('Session expired. Please log in again.', 'warning');
            App.openAuthModal();
        });
    };

    const updateUI = () => {
        const user = API.getUser();
        const navAuth = document.getElementById('navAuth');
        const navUser = document.getElementById('navUser');
        const navAdminTab = document.getElementById('navAdminTab');
        const navAttemptsTab = document.getElementById('navAttemptsTab');
        const userNameSpan = document.getElementById('currentUserName');
        const userRoleBadge = document.getElementById('currentUserRole');

        if (user && API.getToken()) {
            if (navAuth) navAuth.classList.add('d-none');
            if (navUser) navUser.classList.remove('d-none');
            if (userNameSpan) userNameSpan.textContent = user.fullName || user.username;

            if (userRoleBadge) {
                userRoleBadge.textContent = user.role.replace('ROLE_', '');
                userRoleBadge.className = 'badge ms-2 ' +
                    (user.role === 'ROLE_ADMIN' ? 'bg-danger' :
                     user.role === 'ROLE_TEACHER' ? 'bg-warning text-dark' : 'bg-primary');
            }

            if (navAttemptsTab) navAttemptsTab.classList.remove('d-none');

            // Show admin tab only if Admin or Teacher
            if (navAdminTab) {
                if (user.role === 'ROLE_ADMIN' || user.role === 'ROLE_TEACHER') {
                    navAdminTab.classList.remove('d-none');
                } else {
                    navAdminTab.classList.add('d-none');
                }
            }
        } else {
            if (navAuth) navAuth.classList.remove('d-none');
            if (navUser) navUser.classList.add('d-none');
            if (navAdminTab) navAdminTab.classList.add('d-none');
            if (navAttemptsTab) navAttemptsTab.classList.add('d-none');
        }
    };

    return {
        init,
        updateUI,
        getUser: API.getUser,
        isLoggedIn: () => !!API.getToken() && !!API.getUser(),
        isAdminOrTeacher: () => {
            const u = API.getUser();
            return u && (u.role === 'ROLE_ADMIN' || u.role === 'ROLE_TEACHER');
        }
    };
})();
