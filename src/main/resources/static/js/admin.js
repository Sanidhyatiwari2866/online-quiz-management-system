/**
 * Admin / Teacher Module: Dashboard, Quiz & Question Management, Analytics
 */
const Admin = (() => {
    let currentQuizForQuestion = null;

    const init = () => {
        setupEventListeners();
    };

    const setupEventListeners = () => {
        // Create Quiz form submission
        const formCreateQuiz = document.getElementById('formCreateQuiz');
        if (formCreateQuiz) {
            formCreateQuiz.addEventListener('submit', async (e) => {
                e.preventDefault();
                const quizId = document.getElementById('adminQuizId').value;
                const title = document.getElementById('adminQuizTitle').value.trim();
                const description = document.getElementById('adminQuizDesc').value.trim();
                const categoryId = Number(document.getElementById('adminQuizCategory').value);
                const timeLimitMinutes = Number(document.getElementById('adminQuizTime').value);
                const passingScore = Number(document.getElementById('adminQuizPassScore').value);
                const randomizeQuestions = document.getElementById('adminQuizRandomize').checked;

                try {
                    if (quizId) {
                        await API.updateQuiz(quizId, { title, description, categoryId, timeLimitMinutes, passingScore, randomizeQuestions });
                        App.showToast('Quiz updated successfully!', 'success');
                    } else {
                        await API.createQuiz({ title, description, categoryId, timeLimitMinutes, passingScore, randomizeQuestions });
                        App.showToast('Quiz created successfully!', 'success');
                    }

                    const modal = bootstrap.Modal.getInstance(document.getElementById('modalCreateQuiz'));
                    if (modal) modal.hide();
                    loadAdminData();
                } catch (err) {
                    App.showToast(err.message, 'danger');
                }
            });
        }

        // Create Category form submission
        const formCreateCategory = document.getElementById('formCreateCategory');
        if (formCreateCategory) {
            formCreateCategory.addEventListener('submit', async (e) => {
                e.preventDefault();
                const name = document.getElementById('adminCatName').value.trim();
                const description = document.getElementById('adminCatDesc').value.trim();

                try {
                    await API.createCategory({ name, description });
                    App.showToast('Category created successfully!', 'success');
                    const modal = bootstrap.Modal.getInstance(document.getElementById('modalCreateCategory'));
                    if (modal) modal.hide();
                    formCreateCategory.reset();
                    loadCategoriesList();
                    loadAnalytics();
                } catch (err) {
                    App.showToast(err.message, 'danger');
                }
            });
        }

        // Question Type Change Handler
        const qTypeSelect = document.getElementById('builderQuestionType');
        if (qTypeSelect) {
            qTypeSelect.addEventListener('change', () => {
                handleQuestionTypeChange(qTypeSelect.value);
            });
        }

        // Add Option button in Question Builder
        const btnAddOpt = document.getElementById('btnAddOptionRow');
        if (btnAddOpt) {
            btnAddOpt.addEventListener('click', () => {
                addOptionRow();
            });
        }

        // Save Question form
        const formSaveQuestion = document.getElementById('formSaveQuestion');
        if (formSaveQuestion) {
            formSaveQuestion.addEventListener('submit', async (e) => {
                e.preventDefault();
                await handleSaveQuestion();
            });
        }
    };

    /**
     * Load All Admin Portal Data
     */
    const loadAdminData = async () => {
        if (!Auth.isAdminOrTeacher()) {
            App.showToast('Access denied: Admin or Teacher role required', 'danger');
            App.loadView('quizzes');
            return;
        }

        await loadAnalytics();
        await loadQuizzesTable();
        await loadCategoriesList();
        await loadAllAttemptsTable();
    };

    /**
     * Analytics
     */
    const loadAnalytics = async () => {
        try {
            const data = await API.getAnalytics();
            document.getElementById('kpiTotalQuizzes').textContent = data.totalQuizzes;
            document.getElementById('kpiTotalQuestions').textContent = data.totalQuestions;
            document.getElementById('kpiTotalStudents').textContent = data.totalStudents;
            document.getElementById('kpiTotalAttempts').textContent = data.totalAttempts;
            document.getElementById('kpiPassRate').textContent = `${data.passRatePercentage}%`;
        } catch (err) {
            console.error('Failed to load analytics', err);
        }
    };

    /**
     * Quizzes Table
     */
    const loadQuizzesTable = async () => {
        const tbody = document.getElementById('adminQuizzesTableBody');
        if (!tbody) return;

        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div></td></tr>`;

        try {
            const quizzes = await API.getQuizzes();
            if (!quizzes || quizzes.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No quizzes created yet.</td></tr>`;
                return;
            }

            tbody.innerHTML = quizzes.map(q => `
                <tr>
                    <td class="fw-bold">${q.id}</td>
                    <td class="fw-semibold">${escapeHtml(q.title)}</td>
                    <td><span class="badge bg-light text-dark border">${escapeHtml(q.categoryName)}</span></td>
                    <td>${q.timeLimitMinutes} min</td>
                    <td>${q.questionCount} (${q.totalMarks} marks)</td>
                    <td>${q.passingScore}%</td>
                    <td>
                        <button class="btn btn-sm btn-outline-success me-1" title="Add / Manage Questions" onclick="Admin.openQuestionBuilder(${q.id}, '${escapeHtml(q.title)}')">
                            <i class="fas fa-plus-circle me-1"></i> Questions
                        </button>
                        <button class="btn btn-sm btn-outline-primary me-1" title="Edit Quiz" onclick="Admin.openEditQuizModal(${q.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="btn btn-sm btn-outline-danger" title="Delete Quiz" onclick="Admin.deleteQuiz(${q.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to load: ${err.message}</td></tr>`;
        }
    };

    /**
     * Categories List
     */
    const loadCategoriesList = async () => {
        const container = document.getElementById('adminCategoriesList');
        const selectCat = document.getElementById('adminQuizCategory');
        if (!container) return;

        try {
            const categories = await API.getCategories();

            // Populate table/list
            container.innerHTML = categories.map(c => `
                <div class="list-group-item d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-0 fw-bold">${escapeHtml(c.name)}</h6>
                        <small class="text-muted">${escapeHtml(c.description || 'No description')}</small>
                    </div>
                    <div>
                        <span class="badge bg-primary rounded-pill me-2">${c.quizCount} Quizzes</span>
                        <button class="btn btn-sm btn-outline-danger" onclick="Admin.deleteCategory(${c.id})">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `).join('');

            // Populate category dropdown in Quiz modal
            if (selectCat) {
                selectCat.innerHTML = categories.map(c => `
                    <option value="${c.id}">${escapeHtml(c.name)}</option>
                `).join('');
            }
        } catch (err) {
            console.error('Failed to load categories', err);
        }
    };

    /**
     * All Attempts Table
     */
    const loadAllAttemptsTable = async () => {
        const tbody = document.getElementById('adminAttemptsTableBody');
        if (!tbody) return;

        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary"></div></td></tr>`;

        try {
            const attempts = await API.getAllAttempts();
            if (!attempts || attempts.length === 0) {
                tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No student attempts recorded yet.</td></tr>`;
                return;
            }

            tbody.innerHTML = attempts.map(a => {
                const date = a.completedAt ? new Date(a.completedAt).toLocaleString() : 'In Progress';
                const mins = Math.floor(a.timeTakenSeconds / 60);
                const secs = a.timeTakenSeconds % 60;
                return `
                    <tr>
                        <td class="fw-semibold">${escapeHtml(a.studentName || a.username)}</td>
                        <td>${escapeHtml(a.quizTitle)}</td>
                        <td>${date}</td>
                        <td>${mins}m ${secs}s</td>
                        <td class="fw-bold">${a.score} / ${a.totalMarks} (${a.percentage}%)</td>
                        <td><span class="badge bg-${a.passed ? 'success' : 'danger'}">${a.passed ? 'PASSED' : 'FAILED'}</span></td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="Student.reviewAttempt(${a.attemptId})">
                                <i class="fas fa-eye me-1"></i> Details
                            </button>
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to load: ${err.message}</td></tr>`;
        }
    };

    /**
     * Quiz Modal Actions
     */
    const openCreateQuizModal = () => {
        document.getElementById('formCreateQuiz').reset();
        document.getElementById('adminQuizId').value = '';
        document.getElementById('modalCreateQuizTitle').textContent = 'Create New Quiz';
        const modal = new bootstrap.Modal(document.getElementById('modalCreateQuiz'));
        modal.show();
    };

    const openEditQuizModal = async (quizId) => {
        try {
            const quiz = await API.getQuiz(quizId);
            document.getElementById('adminQuizId').value = quiz.id;
            document.getElementById('adminQuizTitle').value = quiz.title;
            document.getElementById('adminQuizDesc').value = quiz.description || '';
            document.getElementById('adminQuizCategory').value = quiz.categoryId;
            document.getElementById('adminQuizTime').value = quiz.timeLimitMinutes;
            document.getElementById('adminQuizPassScore').value = quiz.passingScore;
            document.getElementById('adminQuizRandomize').checked = !!quiz.randomizeQuestions;

            document.getElementById('modalCreateQuizTitle').textContent = 'Edit Quiz Details';
            const modal = new bootstrap.Modal(document.getElementById('modalCreateQuiz'));
            modal.show();
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    const deleteQuiz = async (id) => {
        if (!confirm('Are you sure you want to delete this quiz? All associated questions and student attempts will also be deleted.')) return;
        try {
            await API.deleteQuiz(id);
            App.showToast('Quiz deleted successfully', 'success');
            loadAdminData();
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    const deleteCategory = async (id) => {
        if (!confirm('Are you sure you want to delete this category?')) return;
        try {
            await API.deleteCategory(id);
            App.showToast('Category deleted successfully', 'success');
            loadCategoriesList();
            loadAnalytics();
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    /**
     * Question Builder
     */
    const openQuestionBuilder = async (quizId, quizTitle) => {
        currentQuizForQuestion = quizId;
        document.getElementById('builderQuizTitle').textContent = quizTitle;
        document.getElementById('formSaveQuestion').reset();

        await loadExistingQuestions(quizId);
        resetQuestionBuilderForm();

        const modal = new bootstrap.Modal(document.getElementById('modalQuestionBuilder'));
        modal.show();
    };

    const loadExistingQuestions = async (quizId) => {
        const container = document.getElementById('builderQuestionsList');
        if (!container) return;

        try {
            const questions = await API.getQuizQuestions(quizId);
            if (!questions || questions.length === 0) {
                container.innerHTML = `<p class="text-muted small">No questions created yet for this quiz.</p>`;
                return;
            }

            container.innerHTML = questions.map((q, idx) => `
                <div class="card mb-2 border shadow-none">
                    <div class="card-body p-3">
                        <div class="d-flex justify-content-between align-items-start">
                            <span class="badge bg-secondary mb-1">Q${idx + 1} (${q.marks} mark${q.marks > 1 ? 's' : ''})</span>
                            <button class="btn btn-sm text-danger p-0 border-0" onclick="Admin.deleteQuestion(${q.id})" title="Delete question">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </div>
                        <p class="mb-1 small fw-semibold text-truncate" title="${escapeHtml(q.questionText)}">${escapeHtml(q.questionText)}</p>
                        <div class="small text-muted">Options: ${q.options.length} | Type: ${q.questionType}</div>
                    </div>
                </div>
            `).join('');
        } catch (err) {
            container.innerHTML = `<p class="text-danger small">Failed to load questions</p>`;
        }
    };

    const resetQuestionBuilderForm = () => {
        document.getElementById('builderQuestionText').value = '';
        document.getElementById('builderQuestionType').value = 'SINGLE_CHOICE';
        document.getElementById('builderQuestionMarks').value = '1';
        document.getElementById('builderQuestionExplanation').value = '';
        handleQuestionTypeChange('SINGLE_CHOICE');
    };

    const handleQuestionTypeChange = (type) => {
        const container = document.getElementById('builderOptionsContainer');
        const btnAdd = document.getElementById('btnAddOptionRow');

        if (type === 'TRUE_FALSE') {
            if (btnAdd) btnAdd.classList.add('d-none');
            container.innerHTML = `
                <div class="input-group mb-2 option-row">
                    <div class="input-group-text">
                        <input class="form-check-input mt-0 opt-correct-check" type="radio" name="optCorrect" value="0" checked>
                    </div>
                    <input type="text" class="form-control opt-text" value="True" readonly>
                </div>
                <div class="input-group mb-2 option-row">
                    <div class="input-group-text">
                        <input class="form-check-input mt-0 opt-correct-check" type="radio" name="optCorrect" value="1">
                    </div>
                    <input type="text" class="form-control opt-text" value="False" readonly>
                </div>
            `;
        } else {
            if (btnAdd) btnAdd.classList.remove('d-none');
            container.innerHTML = '';
            addOptionRow(type === 'MULTIPLE_CHOICE');
            addOptionRow(type === 'MULTIPLE_CHOICE');
            addOptionRow(type === 'MULTIPLE_CHOICE');
            addOptionRow(type === 'MULTIPLE_CHOICE');
        }
    };

    const addOptionRow = (isMulti = false) => {
        const container = document.getElementById('builderOptionsContainer');
        if (!container) return;

        const rowCount = container.querySelectorAll('.option-row').length;
        const inputType = isMulti ? 'checkbox' : 'radio';

        const row = document.createElement('div');
        row.className = 'input-group mb-2 option-row';
        row.innerHTML = `
            <div class="input-group-text" title="Check if this option is correct">
                <input class="form-check-input mt-0 opt-correct-check" type="${inputType}" name="optCorrect" value="${rowCount}" ${rowCount === 0 ? 'checked' : ''}>
            </div>
            <input type="text" class="form-control opt-text" placeholder="Option ${rowCount + 1} text..." required>
            ${rowCount >= 2 ? `
                <button class="btn btn-outline-danger" type="button" onclick="this.closest('.option-row').remove()">
                    <i class="fas fa-times"></i>
                </button>
            ` : ''}
        `;
        container.appendChild(row);
    };

    const handleSaveQuestion = async () => {
        if (!currentQuizForQuestion) return;

        const text = document.getElementById('builderQuestionText').value.trim();
        const type = document.getElementById('builderQuestionType').value;
        const marks = Number(document.getElementById('builderQuestionMarks').value) || 1;
        const explanation = document.getElementById('builderQuestionExplanation').value.trim();

        const optionRows = document.querySelectorAll('#builderOptionsContainer .option-row');
        const options = [];

        optionRows.forEach(row => {
            const optText = row.querySelector('.opt-text').value.trim();
            const isCorrect = row.querySelector('.opt-correct-check').checked;
            if (optText) {
                options.push({ optionText: optText, isCorrect });
            }
        });

        if (options.length < 2) {
            App.showToast('Please provide at least 2 options for the question.', 'warning');
            return;
        }

        const hasCorrect = options.some(o => o.isCorrect);
        if (!hasCorrect) {
            App.showToast('Please select at least one correct option.', 'warning');
            return;
        }

        try {
            await API.addQuestion(currentQuizForQuestion, {
                questionText: text,
                questionType: type,
                marks,
                explanation,
                options
            });

            App.showToast('Question added successfully!', 'success');
            resetQuestionBuilderForm();
            await loadExistingQuestions(currentQuizForQuestion);
            await loadQuizzesTable();
            await loadAnalytics();
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    const deleteQuestion = async (questionId) => {
        if (!confirm('Are you sure you want to delete this question?')) return;
        try {
            await API.deleteQuestion(questionId);
            App.showToast('Question deleted successfully', 'success');
            await loadExistingQuestions(currentQuizForQuestion);
            await loadQuizzesTable();
            await loadAnalytics();
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    const escapeHtml = (str) => {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    };

    return {
        init,
        loadAdminData,
        openCreateQuizModal,
        openEditQuizModal,
        deleteQuiz,
        deleteCategory,
        openQuestionBuilder,
        deleteQuestion
    };
})();
