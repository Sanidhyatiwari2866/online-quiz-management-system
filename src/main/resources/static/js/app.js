/**
 * Main Application Orchestrator & View Router
 */
const App = (() => {
    const views = ['quizzes', 'taking-quiz', 'quiz-result', 'my-attempts', 'leaderboard', 'admin'];

    const init = () => {
        Auth.init();
        Student.init();
        Admin.init();
        setupNavigation();

        // Check current path or default to quizzes view
        loadView('quizzes');
    };

    const setupNavigation = () => {
        // Nav items click handlers
        document.getElementById('navQuizzesTab')?.addEventListener('click', (e) => {
            e.preventDefault();
            loadView('quizzes');
        });

        document.getElementById('navLeaderboardTab')?.addEventListener('click', (e) => {
            e.preventDefault();
            loadView('leaderboard');
        });

        document.getElementById('navAttemptsTab')?.addEventListener('click', (e) => {
            e.preventDefault();
            if (!Auth.isLoggedIn()) {
                openAuthModal();
                return;
            }
            loadView('my-attempts');
        });

        document.getElementById('navAdminTab')?.addEventListener('click', (e) => {
            e.preventDefault();
            if (!Auth.isAdminOrTeacher()) {
                showToast('Access restricted to Admins and Quiz Creators.', 'warning');
                return;
            }
            loadView('admin');
        });

        document.getElementById('navLoginBtn')?.addEventListener('click', (e) => {
            e.preventDefault();
            openAuthModal();
        });
    };

    const loadView = (viewName) => {
        // Hide all views
        views.forEach(v => {
            const el = document.getElementById(`view-${v}`);
            if (el) el.classList.add('d-none');
        });

        // Highlight active navbar link
        document.querySelectorAll('.navbar-nav .nav-link').forEach(l => l.classList.remove('active'));
        if (viewName === 'quizzes') document.getElementById('navQuizzesTab')?.classList.add('active');
        if (viewName === 'leaderboard') document.getElementById('navLeaderboardTab')?.classList.add('active');
        if (viewName === 'my-attempts') document.getElementById('navAttemptsTab')?.classList.add('active');
        if (viewName === 'admin') document.getElementById('navAdminTab')?.classList.add('active');

        // Show target view
        const target = document.getElementById(`view-${viewName}`);
        if (target) {
            target.classList.remove('d-none');
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        // Trigger view-specific data loading
        if (viewName === 'quizzes') {
            Student.loadQuizCatalog();
        } else if (viewName === 'leaderboard') {
            Student.loadLeaderboard();
        } else if (viewName === 'my-attempts') {
            Student.loadMyAttempts();
        } else if (viewName === 'admin') {
            Admin.loadAdminData();
        }
    };

    const openAuthModal = () => {
        const modal = new bootstrap.Modal(document.getElementById('authModal'));
        modal.show();
    };

    const showToast = (message, type = 'info') => {
        const toastContainer = document.getElementById('toastContainer');
        if (!toastContainer) return;

        const toastId = 'toast_' + Date.now();
        const bgClass = type === 'success' ? 'bg-success' :
                        type === 'danger' ? 'bg-danger' :
                        type === 'warning' ? 'bg-warning text-dark' : 'bg-primary';

        const toastEl = document.createElement('div');
        toastEl.className = `toast align-items-center text-white ${bgClass} border-0 mb-2`;
        toastEl.id = toastId;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');
        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body fw-semibold">
                    ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;

        toastContainer.appendChild(toastEl);
        const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
        toast.show();

        toastEl.addEventListener('hidden.bs.toast', () => {
            toastEl.remove();
        });
    };

    return {
        init,
        loadView,
        openAuthModal,
        showToast
    };
})();

document.addEventListener('DOMContentLoaded', () => {
    App.init();
});
