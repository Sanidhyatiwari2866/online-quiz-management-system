/**
 * Student Module: Quiz Catalog, Taking Timed Quizzes, Scoring & Review
 */
const Student = (() => {
    let currentQuizData = null;
    let currentQuestionIndex = 0;
    let userAnswers = {}; // { questionId: [selectedOptionIds] }
    let flaggedQuestions = new Set();
    let timerInterval = null;
    let remainingSeconds = 0;

    const init = () => {
        setupEventListeners();
    };

    const setupEventListeners = () => {
        // Search and category filters
        const searchInput = document.getElementById('quizSearchInput');
        if (searchInput) {
            let debounceTimer;
            searchInput.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                debounceTimer = setTimeout(() => {
                    loadQuizCatalog(getSelectedCategoryId(), searchInput.value.trim());
                }, 300);
            });
        }

        // Quiz navigation buttons
        const btnPrev = document.getElementById('btnPrevQuestion');
        if (btnPrev) {
            btnPrev.addEventListener('click', () => {
                if (currentQuestionIndex > 0) {
                    renderQuestion(currentQuestionIndex - 1);
                }
            });
        }

        const btnNext = document.getElementById('btnNextQuestion');
        if (btnNext) {
            btnNext.addEventListener('click', () => {
                if (currentQuizData && currentQuestionIndex < currentQuizData.questions.length - 1) {
                    renderQuestion(currentQuestionIndex + 1);
                }
            });
        }

        const btnFlag = document.getElementById('btnFlagQuestion');
        if (btnFlag) {
            btnFlag.addEventListener('click', () => {
                if (!currentQuizData) return;
                const q = currentQuizData.questions[currentQuestionIndex];
                if (flaggedQuestions.has(q.id)) {
                    flaggedQuestions.delete(q.id);
                    btnFlag.innerHTML = '<i class="far fa-flag me-1"></i> Flag for Review';
                    btnFlag.classList.remove('btn-warning');
                    btnFlag.classList.add('btn-outline-warning');
                } else {
                    flaggedQuestions.add(q.id);
                    btnFlag.innerHTML = '<i class="fas fa-flag me-1"></i> Flagged';
                    btnFlag.classList.remove('btn-outline-warning');
                    btnFlag.classList.add('btn-warning');
                }
                updatePalette();
            });
        }

        // Quiz Submission
        const btnSubmitQuiz = document.getElementById('btnSubmitQuiz');
        if (btnSubmitQuiz) {
            btnSubmitQuiz.addEventListener('click', () => {
                confirmAndSubmitQuiz();
            });
        }

        const btnConfirmSubmit = document.getElementById('btnConfirmSubmit');
        if (btnConfirmSubmit) {
            btnConfirmSubmit.addEventListener('click', () => {
                const modalEl = document.getElementById('submitConfirmModal');
                const modal = bootstrap.Modal.getInstance(modalEl);
                if (modal) modal.hide();
                executeSubmitQuiz();
            });
        }
    };

    const getSelectedCategoryId = () => {
        const activeBtn = document.querySelector('.category-pill.active');
        return activeBtn ? activeBtn.dataset.categoryId : null;
    };

    /**
     * Load Categories and Quizzes
     */
    const loadQuizCatalog = async (categoryId = null, keyword = '') => {
        const container = document.getElementById('quizListContainer');
        if (!container) return;

        container.innerHTML = `
            <div class="col-12 text-center py-5">
                <div class="spinner-border text-primary" role="status"></div>
                <p class="mt-2 text-muted">Loading available quizzes...</p>
            </div>
        `;

        try {
            // Load categories first if container is empty
            const catPills = document.getElementById('categoryPills');
            if (catPills && catPills.children.length <= 1) {
                const categories = await API.getCategories();
                renderCategoryPills(categories);
            }

            const quizzes = await API.getQuizzes(categoryId, keyword);
            if (!quizzes || quizzes.length === 0) {
                container.innerHTML = `
                    <div class="col-12 text-center py-5">
                        <i class="fas fa-folder-open fa-3x text-muted mb-3"></i>
                        <h5 class="text-secondary">No quizzes found</h5>
                        <p class="text-muted">Try adjusting your category filter or search keywords.</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = quizzes.map(q => `
                <div class="col-md-6 col-lg-4 mb-4">
                    <div class="quiz-card p-4">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle">${q.categoryName || 'General'}</span>
                            <span class="badge bg-light text-secondary border"><i class="far fa-clock me-1"></i>${q.timeLimitMinutes} mins</span>
                        </div>
                        <h5 class="card-title fw-bold text-dark mb-2">${escapeHtml(q.title)}</h5>
                        <p class="card-text text-muted small flex-grow-1 mb-3">${escapeHtml(q.description || 'No description provided.')}</p>
                        <div class="border-top pt-3 mt-auto">
                            <div class="d-flex justify-content-between text-muted small mb-3">
                                <span><i class="fas fa-list-ol me-1 text-primary"></i>${q.questionCount} Questions</span>
                                <span><i class="fas fa-check-circle me-1 text-success"></i>Pass: ${q.passingScore}%</span>
                            </div>
                            <button class="btn btn-primary w-100 fw-semibold" onclick="Student.startQuiz(${q.id})">
                                <i class="fas fa-play-circle me-1"></i> Start Quiz
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
        } catch (err) {
            container.innerHTML = `
                <div class="col-12 alert alert-danger">
                    <i class="fas fa-exclamation-triangle me-2"></i> Failed to load quizzes: ${err.message}
                </div>
            `;
        }
    };

    const renderCategoryPills = (categories) => {
        const container = document.getElementById('categoryPills');
        if (!container) return;

        const allPill = `
            <button class="btn btn-sm btn-outline-primary category-pill active me-2 mb-2" data-category-id="" onclick="Student.selectCategory(this, '')">
                All Categories
            </button>
        `;

        const otherPills = categories.map(c => `
            <button class="btn btn-sm btn-outline-primary category-pill me-2 mb-2" data-category-id="${c.id}" onclick="Student.selectCategory(this, ${c.id})">
                ${escapeHtml(c.name)} <span class="badge bg-light text-primary ms-1">${c.quizCount}</span>
            </button>
        `).join('');

        container.innerHTML = allPill + otherPills;
    };

    const selectCategory = (button, categoryId) => {
        document.querySelectorAll('.category-pill').forEach(b => b.classList.remove('active'));
        button.classList.add('active');
        const keyword = document.getElementById('quizSearchInput')?.value.trim() || '';
        loadQuizCatalog(categoryId || null, keyword);
    };

    /**
     * Start a Quiz
     */
    const startQuiz = async (quizId) => {
        if (!Auth.isLoggedIn()) {
            App.showToast('Please log in or register before taking a quiz.', 'warning');
            App.openAuthModal();
            return;
        }

        try {
            App.showToast('Starting quiz session...', 'info');
            const data = await API.startQuiz(quizId);
            currentQuizData = data;
            currentQuestionIndex = 0;
            userAnswers = {};
            flaggedQuestions.clear();

            // Set countdown timer
            remainingSeconds = data.timeLimitMinutes * 60;
            startTimer();

            // Render view
            App.loadView('taking-quiz');
            document.getElementById('activeQuizTitle').textContent = data.title;
            document.getElementById('activeQuizTotal').textContent = `${data.questions.length} Questions`;
            document.getElementById('activeQuizPass').textContent = `Pass: ${data.passingScore}%`;

            renderPalette();
            renderQuestion(0);
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    /**
     * Timer Logic
     */
    const startTimer = () => {
        if (timerInterval) clearInterval(timerInterval);

        updateTimerDisplay();
        timerInterval = setInterval(() => {
            remainingSeconds--;
            updateTimerDisplay();

            if (remainingSeconds <= 0) {
                clearInterval(timerInterval);
                App.showToast("Time has expired! Submitting your answers automatically...", 'warning');
                executeSubmitQuiz();
            }
        }, 1000);
    };

    const updateTimerDisplay = () => {
        const timerEl = document.getElementById('quizTimer');
        const timerContainer = document.getElementById('timerContainer');
        if (!timerEl) return;

        const m = Math.floor(remainingSeconds / 60);
        const s = remainingSeconds % 60;
        timerEl.textContent = `${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;

        if (remainingSeconds <= 60 && timerContainer) {
            timerContainer.classList.add('timer-warning');
        } else if (timerContainer) {
            timerContainer.classList.remove('timer-warning');
        }
    };

    /**
     * Render Active Question
     */
    const renderQuestion = (index) => {
        if (!currentQuizData || !currentQuizData.questions[index]) return;
        currentQuestionIndex = index;
        const q = currentQuizData.questions[index];
        const total = currentQuizData.questions.length;

        document.getElementById('qCurrentNum').textContent = index + 1;
        document.getElementById('qTotalNum').textContent = total;
        document.getElementById('qMarks').textContent = `${q.marks} Mark${q.marks > 1 ? 's' : ''}`;
        document.getElementById('qText').textContent = q.questionText;

        const typeBadge = document.getElementById('qTypeBadge');
        if (typeBadge) {
            if (q.questionType === 'MULTIPLE_CHOICE') {
                typeBadge.textContent = 'Multiple Choice (Select all that apply)';
                typeBadge.className = 'badge bg-warning text-dark';
            } else if (q.questionType === 'TRUE_FALSE') {
                typeBadge.textContent = 'True / False';
                typeBadge.className = 'badge bg-info text-dark';
            } else {
                typeBadge.textContent = 'Single Choice';
                typeBadge.className = 'badge bg-secondary';
            }
        }

        // Render Options
        const optionsContainer = document.getElementById('qOptionsContainer');
        const selected = userAnswers[q.id] || [];
        const isMulti = q.questionType === 'MULTIPLE_CHOICE';

        optionsContainer.innerHTML = q.options.map(opt => {
            const isChecked = selected.includes(opt.id);
            const inputType = isMulti ? 'checkbox' : 'radio';
            return `
                <div class="option-item ${isChecked ? 'selected' : ''}" onclick="Student.toggleOption(${q.id}, ${opt.id}, ${isMulti})">
                    <div class="form-check">
                        <input class="form-check-input" type="${inputType}" name="q_opt_${q.id}" id="opt_${opt.id}"
                               value="${opt.id}" ${isChecked ? 'checked' : ''} onclick="event.stopPropagation(); Student.toggleOption(${q.id}, ${opt.id}, ${isMulti})">
                        <label class="form-check-label w-100 ms-2" for="opt_${opt.id}" style="cursor:pointer;">
                            ${escapeHtml(opt.optionText)}
                        </label>
                    </div>
                </div>
            `;
        }).join('');

        // Navigation state
        const btnPrev = document.getElementById('btnPrevQuestion');
        const btnNext = document.getElementById('btnNextQuestion');
        if (btnPrev) btnPrev.disabled = index === 0;
        if (btnNext) btnNext.disabled = index === total - 1;

        // Flag button state
        const btnFlag = document.getElementById('btnFlagQuestion');
        if (btnFlag) {
            if (flaggedQuestions.has(q.id)) {
                btnFlag.innerHTML = '<i class="fas fa-flag me-1"></i> Flagged';
                btnFlag.classList.remove('btn-outline-warning');
                btnFlag.classList.add('btn-warning');
            } else {
                btnFlag.innerHTML = '<i class="far fa-flag me-1"></i> Flag for Review';
                btnFlag.classList.remove('btn-warning');
                btnFlag.classList.add('btn-outline-warning');
            }
        }

        updatePalette();
    };

    const toggleOption = (questionId, optionId, isMulti) => {
        if (!userAnswers[questionId]) {
            userAnswers[questionId] = [];
        }

        if (isMulti) {
            const idx = userAnswers[questionId].indexOf(optionId);
            if (idx > -1) {
                userAnswers[questionId].splice(idx, 1);
            } else {
                userAnswers[questionId].push(optionId);
            }
        } else {
            userAnswers[questionId] = [optionId];
        }

        renderQuestion(currentQuestionIndex);
        updatePalette();
    };

    /**
     * Question Palette
     */
    const renderPalette = () => {
        const container = document.getElementById('questionPalette');
        if (!container || !currentQuizData) return;

        container.innerHTML = currentQuizData.questions.map((q, idx) => `
            <button class="palette-btn" id="pal_btn_${idx}" onclick="Student.jumpToQuestion(${idx})">
                ${idx + 1}
            </button>
        `).join('');

        updatePalette();
    };

    const updatePalette = () => {
        if (!currentQuizData) return;

        currentQuizData.questions.forEach((q, idx) => {
            const btn = document.getElementById(`pal_btn_${idx}`);
            if (!btn) return;

            btn.className = 'palette-btn';

            const isAnswered = userAnswers[q.id] && userAnswers[q.id].length > 0;
            const isFlagged = flaggedQuestions.has(q.id);

            if (idx === currentQuestionIndex) btn.classList.add('active');
            if (isAnswered) btn.classList.add('answered');
            if (isFlagged) btn.classList.add('flagged');
        });
    };

    const jumpToQuestion = (index) => {
        renderQuestion(index);
    };

    /**
     * Submission
     */
    const confirmAndSubmitQuiz = () => {
        if (!currentQuizData) return;

        const total = currentQuizData.questions.length;
        const answeredCount = Object.values(userAnswers).filter(arr => arr && arr.length > 0).length;
        const unansweredCount = total - answeredCount;

        document.getElementById('modalAnsweredCount').textContent = answeredCount;
        document.getElementById('modalUnansweredCount').textContent = unansweredCount;

        const alertUnanswered = document.getElementById('modalUnansweredAlert');
        if (alertUnanswered) {
            if (unansweredCount > 0) {
                alertUnanswered.classList.remove('d-none');
            } else {
                alertUnanswered.classList.add('d-none');
            }
        }

        const modal = new bootstrap.Modal(document.getElementById('submitConfirmModal'));
        modal.show();
    };

    const executeSubmitQuiz = async () => {
        if (timerInterval) clearInterval(timerInterval);

        const answersList = Object.keys(userAnswers).map(qId => ({
            questionId: Number(qId),
            selectedOptionIds: userAnswers[qId]
        }));

        try {
            App.showToast('Evaluating your quiz answers...', 'info');
            const result = await API.submitQuiz({
                attemptId: currentQuizData.attemptId,
                answers: answersList
            });

            renderQuizResult(result);
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    /**
     * Render Quiz Result Breakdown
     */
    const renderQuizResult = (result) => {
        App.loadView('quiz-result');

        document.getElementById('resQuizTitle').textContent = result.quizTitle;
        document.getElementById('resScoreNum').textContent = `${result.score} / ${result.totalMarks}`;
        document.getElementById('resScorePercent').textContent = `${result.percentage}%`;
        document.getElementById('resCorrectCount').textContent = `${result.correctAnswers} / ${result.totalQuestions}`;

        const mins = Math.floor(result.timeTakenSeconds / 60);
        const secs = result.timeTakenSeconds % 60;
        document.getElementById('resTimeTaken').textContent = `${mins}m ${secs}s`;

        // Pass/Fail Banner & Ring
        const banner = document.getElementById('resPassFailBanner');
        const scoreRing = document.getElementById('resScoreRing');
        const certBtn = document.getElementById('btnViewCertificate');

        if (result.passed) {
            banner.className = 'alert alert-success d-flex align-items-center mb-4';
            banner.innerHTML = `<i class="fas fa-check-circle fa-2x me-3"></i><div><h5 class="alert-heading mb-1">Congratulations! You Passed!</h5><p class="mb-0">You scored ${result.percentage}%, exceeding the required passing score of ${result.passingScore}%.</p></div>`;
            if (scoreRing) {
                scoreRing.className = 'score-circle score-pass';
            }
            if (certBtn) certBtn.classList.remove('d-none');
        } else {
            banner.className = 'alert alert-danger d-flex align-items-center mb-4';
            banner.innerHTML = `<i class="fas fa-times-circle fa-2x me-3"></i><div><h5 class="alert-heading mb-1">Keep Practicing! You Did Not Pass.</h5><p class="mb-0">You scored ${result.percentage}%. The minimum passing threshold is ${result.passingScore}%.</p></div>`;
            if (scoreRing) {
                scoreRing.className = 'score-circle score-fail';
            }
            if (certBtn) certBtn.classList.add('d-none');
        }

        // Store result for certificate generation
        window.lastQuizResult = result;

        // Render Question Breakdown
        const breakdownContainer = document.getElementById('resBreakdownContainer');
        if (breakdownContainer && result.questionResults) {
            breakdownContainer.innerHTML = result.questionResults.map((qr, idx) => {
                const isCorrect = qr.isCorrect;
                return `
                    <div class="card mb-3 border-${isCorrect ? 'success' : 'danger'}">
                        <div class="card-header bg-${isCorrect ? 'success-subtle' : 'danger-subtle'} d-flex justify-content-between align-items-center">
                            <span class="fw-bold">Question ${idx + 1} (${qr.marksAwarded} / ${qr.marks} Marks)</span>
                            <span class="badge bg-${isCorrect ? 'success' : 'danger'}">${isCorrect ? 'Correct' : 'Incorrect'}</span>
                        </div>
                        <div class="card-body">
                            <h6 class="card-title fw-semibold mb-3">${escapeHtml(qr.questionText)}</h6>
                            <div class="list-group mb-3">
                                ${qr.options.map(opt => {
                                    const isUserPicked = qr.selectedOptionIds.includes(opt.id);
                                    const isRealCorrect = qr.correctOptionIds.includes(opt.id);
                                    let itemClass = '';
                                    let badgeHtml = '';

                                    if (isRealCorrect) {
                                        itemClass = 'list-group-item-success fw-semibold';
                                        badgeHtml += '<span class="badge bg-success ms-auto"><i class="fas fa-check me-1"></i> Correct Answer</span>';
                                    }
                                    if (isUserPicked && !isRealCorrect) {
                                        itemClass = 'list-group-item-danger';
                                        badgeHtml += '<span class="badge bg-danger ms-auto"><i class="fas fa-times me-1"></i> Your Pick</span>';
                                    } else if (isUserPicked && isRealCorrect) {
                                        badgeHtml += '<span class="badge bg-primary ms-1"><i class="fas fa-user-check me-1"></i> Your Pick</span>';
                                    }

                                    return `
                                        <div class="list-group-item d-flex align-items-center ${itemClass}">
                                            <span>${escapeHtml(opt.optionText)}</span>
                                            ${badgeHtml}
                                        </div>
                                    `;
                                }).join('')}
                            </div>
                            ${qr.explanation ? `
                                <div class="p-3 bg-light rounded border border-light-subtle small">
                                    <strong><i class="fas fa-info-circle text-primary me-1"></i> Explanation:</strong> ${escapeHtml(qr.explanation)}
                                </div>
                            ` : ''}
                        </div>
                    </div>
                `;
            }).join('');
        }
    };

    /**
     * My Quiz Attempt History
     */
    const loadMyAttempts = async () => {
        const container = document.getElementById('myAttemptsTableBody');
        if (!container) return;

        container.innerHTML = `
            <tr><td colspan="7" class="text-center py-4"><div class="spinner-border text-primary spinner-border-sm me-2"></div> Loading attempts...</td></tr>
        `;

        try {
            const attempts = await API.getMyAttempts();
            if (!attempts || attempts.length === 0) {
                container.innerHTML = `
                    <tr><td colspan="7" class="text-center py-5 text-muted"><i class="fas fa-history fa-2x mb-2 d-block"></i> No quiz attempts found yet. Take a quiz to view your history!</td></tr>
                `;
                return;
            }

            container.innerHTML = attempts.map(a => {
                const date = a.completedAt ? new Date(a.completedAt).toLocaleString() : 'In Progress';
                const mins = Math.floor(a.timeTakenSeconds / 60);
                const secs = a.timeTakenSeconds % 60;
                return `
                    <tr>
                        <td class="fw-semibold">${escapeHtml(a.quizTitle)}</td>
                        <td><span class="badge bg-light text-dark border">${escapeHtml(a.categoryName)}</span></td>
                        <td>${date}</td>
                        <td>${mins}m ${secs}s</td>
                        <td class="fw-bold">${a.score} / ${a.totalMarks} (${a.percentage}%)</td>
                        <td>
                            <span class="badge bg-${a.passed ? 'success' : 'danger'}">
                                ${a.passed ? 'PASSED' : 'FAILED'}
                            </span>
                        </td>
                        <td>
                            <button class="btn btn-sm btn-outline-primary" onclick="Student.reviewAttempt(${a.attemptId})">
                                <i class="fas fa-eye me-1"></i> Review
                            </button>
                        </td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            container.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to load history: ${err.message}</td></tr>`;
        }
    };

    const reviewAttempt = async (attemptId) => {
        try {
            App.showToast('Fetching quiz attempt breakdown...', 'info');
            const result = await API.getAttempt(attemptId);
            renderQuizResult(result);
        } catch (err) {
            App.showToast(err.message, 'danger');
        }
    };

    /**
     * Load Global Leaderboard (or per-quiz if quizId provided)
     */
    const loadLeaderboard = async (scopeValue) => {
        const container = document.getElementById('leaderboardTableBody');
        if (!container) return;

        container.innerHTML = `
            <tr><td colspan="7" class="text-center py-4">
                <div class="spinner-border text-warning spinner-border-sm me-2"></div> Loading leaderboard...
            </td></tr>
        `;

        try {
            let entries;
            const isGlobal = !scopeValue || scopeValue === 'global';
            if (isGlobal) {
                entries = await API.getLeaderboard(15);
            } else {
                entries = await API.getQuizLeaderboard(scopeValue, 15);
            }

            // Render top-3 podium
            renderPodium(entries);

            if (!entries || entries.length === 0) {
                container.innerHTML = `
                    <tr><td colspan="7" class="text-center py-5 text-muted">
                        <i class="fas fa-trophy fa-2x mb-2 d-block text-warning opacity-50"></i>
                        No quiz attempts yet. Be the first to take a quiz!
                    </td></tr>
                `;
                return;
            }

            container.innerHTML = entries.map((e, idx) => {
                const medalClass = idx === 0 ? 'text-warning' : idx === 1 ? 'text-secondary' : idx === 2 ? 'text-danger' : 'text-muted';
                const medalIcon = idx < 3 ? `<i class="fas fa-medal ${medalClass} me-1"></i>` : '';
                const completedDate = e.completedAt ? new Date(e.completedAt).toLocaleDateString() : 'N/A';
                return `
                    <tr class="${idx < 3 ? 'fw-semibold' : ''}">
                        <td>${medalIcon}<span class="badge bg-${idx === 0 ? 'warning text-dark' : idx === 1 ? 'secondary' : idx === 2 ? 'danger' : 'light text-dark'}">#${e.rank}</span></td>
                        <td>${escapeHtml(e.fullName || e.username)}</td>
                        <td>${escapeHtml(e.quizTitle || 'Overall')}</td>
                        <td class="fw-bold text-success">${e.score} / ${e.totalMarks}</td>
                        <td>
                            <div class="d-flex align-items-center gap-2">
                                <div class="progress flex-grow-1" style="height:8px;">
                                    <div class="progress-bar bg-${e.percentage >= 80 ? 'success' : e.percentage >= 50 ? 'warning' : 'danger'}"
                                         style="width:${e.percentage}%"></div>
                                </div>
                                <small>${e.percentage}%</small>
                            </div>
                        </td>
                        <td class="text-muted small">${e.timeTakenSeconds ? Math.floor(e.timeTakenSeconds/60) + 'm ' + (e.timeTakenSeconds%60) + 's' : 'N/A'}</td>
                        <td class="text-muted small">${completedDate}</td>
                    </tr>
                `;
            }).join('');
        } catch (err) {
            container.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Failed to load leaderboard: ${err.message}</td></tr>`;
        }
    };

    const renderPodium = (entries) => {
        const podium = document.getElementById('leaderboardPodium');
        if (!podium || !entries || entries.length === 0) {
            if (podium) podium.innerHTML = '';
            return;
        }
        const top3 = entries.slice(0, 3);
        const medals = ['🥇', '🥈', '🥉'];
        const colors = ['warning', 'secondary', 'danger'];
        podium.innerHTML = top3.map((e, idx) => `
            <div class="col-md-4">
                <div class="card border-${colors[idx]} border-2 text-center p-3 shadow-sm">
                    <div class="display-5 mb-1">${medals[idx]}</div>
                    <h6 class="fw-bold mb-1">${escapeHtml(e.fullName || e.username)}</h6>
                    <small class="text-muted d-block mb-2">${escapeHtml(e.quizTitle || '')}</small>
                    <span class="badge bg-${colors[idx]} fs-6">${e.score}/${e.totalMarks} &bull; ${e.percentage}%</span>
                </div>
            </div>
        `).join('');
    };

    /**
     * Show Certificate Modal
     */
    const showCertificate = () => {
        const res = window.lastQuizResult;
        if (!res || !res.passed) return;

        document.getElementById('certStudentName').textContent = res.studentName || 'Student';
        document.getElementById('certQuizTitle').textContent = res.quizTitle;
        document.getElementById('certScore').textContent = `${res.percentage}% (${res.score}/${res.totalMarks})`;
        document.getElementById('certDate').textContent = new Date(res.completedAt || new Date()).toLocaleDateString('en-US', { year: 'numeric', month: 'long', day: 'numeric' });

        const modal = new bootstrap.Modal(document.getElementById('certificateModal'));
        modal.show();
    };

    const printCertificate = () => {
        window.print();
    };

    const escapeHtml = (str) => {
        if (!str) return '';
        const div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    };

    return {
        init,
        loadQuizCatalog,
        loadLeaderboard,
        selectCategory,
        startQuiz,
        toggleOption,
        jumpToQuestion,
        loadMyAttempts,
        reviewAttempt,
        showCertificate,
        printCertificate
    };
})();
