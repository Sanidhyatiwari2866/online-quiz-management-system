/**
 * Online Quiz Management System - API Client
 */
const API = (() => {
    const BASE_URL = '/api';
    const TOKEN_KEY = 'quiz_auth_token';
    const USER_KEY = 'quiz_auth_user';

    const getToken = () => localStorage.getItem(TOKEN_KEY);
    const setToken = (token) => localStorage.setItem(TOKEN_KEY, token);
    const removeToken = () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(USER_KEY);
    };

    const getUser = () => {
        const u = localStorage.getItem(USER_KEY);
        return u ? JSON.parse(u) : null;
    };

    const setUser = (user) => localStorage.setItem(USER_KEY, JSON.stringify(user));

    const request = async (endpoint, options = {}) => {
        const url = `${BASE_URL}${endpoint}`;
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        };

        const token = getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(url, { ...options, headers });
            const data = await response.json().catch(() => null);

            if (!response.ok) {
                const message = (data && data.message) || `Error: ${response.status} ${response.statusText}`;
                if (response.status === 401 && token) {
                    removeToken();
                    window.dispatchEvent(new CustomEvent('auth-expired'));
                }
                throw new Error(message);
            }

            return data ? data.data : null;
        } catch (error) {
            console.error(`API Request failed for [${endpoint}]:`, error);
            throw error;
        }
    };

    return {
        getToken,
        setToken,
        removeToken,
        getUser,
        setUser,

        // Auth
        login: (credentials) => request('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
        register: (userData) => request('/auth/register', { method: 'POST', body: JSON.stringify(userData) }),
        getMe: () => request('/auth/me'),

        // Categories
        getCategories: () => request('/categories'),
        createCategory: (cat) => request('/categories', { method: 'POST', body: JSON.stringify(cat) }),
        updateCategory: (id, cat) => request(`/categories/${id}`, { method: 'PUT', body: JSON.stringify(cat) }),
        deleteCategory: (id) => request(`/categories/${id}`, { method: 'DELETE' }),

        // Quizzes
        getQuizzes: (categoryId = null, keyword = '', includeAll = false) => {
            const params = new URLSearchParams();
            if (categoryId) params.append('categoryId', categoryId);
            if (keyword) params.append('keyword', keyword);
            if (includeAll) params.append('includeAll', 'true');
            const qs = params.toString();
            return request(`/quizzes${qs ? '?' + qs : ''}`);
        },
        getQuiz: (id) => request(`/quizzes/${id}`),
        getPendingQuizzes: () => request('/quizzes/pending'),
        createQuiz: (quiz) => request('/quizzes', { method: 'POST', body: JSON.stringify(quiz) }),
        updateQuiz: (id, quiz) => request(`/quizzes/${id}`, { method: 'PUT', body: JSON.stringify(quiz) }),
        approveQuiz: (id) => request(`/quizzes/${id}/approve`, { method: 'PUT' }),
        rejectQuiz: (id, reason) => request(`/quizzes/${id}/reject`, { method: 'PUT', body: JSON.stringify({ reason }) }),
        deleteQuiz: (id) => request(`/quizzes/${id}`, { method: 'DELETE' }),
        getQuizQuestions: (id) => request(`/quizzes/${id}/questions`),

        // Questions
        addQuestion: (quizId, question) => request(`/quizzes/${quizId}/questions`, { method: 'POST', body: JSON.stringify(question) }),
        updateQuestion: (id, question) => request(`/questions/${id}`, { method: 'PUT', body: JSON.stringify(question) }),
        deleteQuestion: (id) => request(`/questions/${id}`, { method: 'DELETE' }),

        // Attempts
        startQuiz: (quizId) => request(`/attempts/start/${quizId}`, { method: 'POST' }),
        submitQuiz: (submission) => request('/attempts/submit', { method: 'POST', body: JSON.stringify(submission) }),
        getAttempt: (attemptId) => request(`/attempts/${attemptId}`),
        getMyAttempts: () => request('/attempts/my'),
        getAllAttempts: () => request('/attempts/all'),
        getAnalytics: () => request('/attempts/analytics'),

        // Leaderboard
        getLeaderboard: (limit = 10) => request(`/leaderboard?limit=${limit}`),
        getQuizLeaderboard: (quizId, limit = 10) => request(`/leaderboard/quiz/${quizId}?limit=${limit}`),

        // Feedback
        submitFeedback: (feedback) => request('/feedback', { method: 'POST', body: JSON.stringify(feedback) }),
        getQuizFeedback: (quizId) => request(`/feedback/quiz/${quizId}`),
        getMyFeedback: () => request('/feedback/my'),

        // Admin Management
        getUsers: () => request('/admin/users'),
        updateUserRole: (id, role) => request(`/admin/users/${id}/role`, { method: 'PUT', body: JSON.stringify({ role }) }),
        updateUserStatus: (id, enabled) => request(`/admin/users/${id}/status`, { method: 'PUT', body: JSON.stringify({ enabled }) }),
        deleteUser: (id) => request(`/admin/users/${id}`, { method: 'DELETE' }),
        getSettings: () => request('/admin/settings'),
        updateSettings: (settings) => request('/admin/settings', { method: 'PUT', body: JSON.stringify(settings) }),
        getAlerts: () => request('/admin/alerts')
    };
})();
