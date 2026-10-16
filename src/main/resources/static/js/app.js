/**
 * FitKit Nutrition Journal — Production Client Logic
 * Authoritative Backend Persistence & Spring Security Authentication:
 * - /api/auth: Registration, login, logout, current user check, and CSRF
 * - /api/meals: Persistent meal logs, nutrient snapshots, add/update/delete, 7-day history, localStorage migration
 * - /api/targets: Persisted user-specific calorie & macronutrient targets
 * - /api/foods & /api/recipes: Verified food catalog & recipe yield engine
 */

(function () {
    'use strict';

    // State Store
    const state = {
        currentUser: null,
        currentDate: new Date(),
        diaryDate: new Date(),
        activeView: 'overview',
        targets: {},
        dailyLog: null,
        todayLog: null,
        diaryLog: null,
        modalTargetDate: null,
        weeklyHistory: [],
        foods: [],
        filteredFoods: [],
        foodGroups: [],
        libSearch: '',
        libStateFilter: 'ALL',
        libGroupFilter: 'ALL',
        libQualityFilter: 'ALL',
        libSort: 'RELEVANCE',
        recipes: [],
        selectedRecipe: null,
        customRecipes: [],
        recipeMode: 'curated',
        modalSelectedFood: null,
        modalTargetMeal: 'Breakfast',
        editingEntry: null
    };

    // DOM Elements Cache
    const DOM = {
        // App Layout Sections
        viewAuth: document.getElementById('view-auth'),
        viewNutritionOnboarding: document.getElementById('view-nutrition-onboarding'),
        mainAppLayout: document.getElementById('main-app-layout'),
        nutritionOnboardingForm: document.getElementById('nutrition-onboarding-form'),
        onboardingAlert: document.getElementById('onboarding-alert'),
        onboardingAge: document.getElementById('onboarding-age'),
        onboardingSex: document.getElementById('onboarding-sex'),
        onboardingHeight: document.getElementById('onboarding-height'),
        onboardingWeight: document.getElementById('onboarding-weight'),
        onboardingActivity: document.getElementById('onboarding-activity'),
        onboardingGoal: document.getElementById('onboarding-goal'),
        btnOnboardingLogout: document.getElementById('btn-onboarding-logout'),

        // Auth Controls
        tabBtnLogin: document.getElementById('tab-btn-login'),
        tabBtnRegister: document.getElementById('tab-btn-register'),
        formLogin: document.getElementById('form-login'),
        formRegister: document.getElementById('form-register'),
        loginEmail: document.getElementById('login-email'),
        loginPassword: document.getElementById('login-password'),
        btnDemoFill: document.getElementById('btn-demo-fill'),
        registerName: document.getElementById('register-name'),
        registerEmail: document.getElementById('register-email'),
        registerPassword: document.getElementById('register-password'),
        authAlertBox: document.getElementById('auth-alert-box'),
        btnSidebarLogout: document.getElementById('btn-sidebar-logout'),
        btnTopbarLogout: document.getElementById('btn-topbar-logout'),
        sidebarUserAvatar: document.getElementById('sidebar-user-avatar'),
        sidebarUserName: document.getElementById('sidebar-user-name'),
        sidebarUserEmail: document.getElementById('sidebar-user-email'),

        // Navigation
        navItems: document.querySelectorAll('.nav-item'),
        views: document.querySelectorAll('.view-section'),
        mobileNavToggle: document.getElementById('mobile-nav-btn'),
        sidebar: document.querySelector('.app-sidebar'),

        // Date Controls
        btnPrevDay: document.getElementById('btn-prev-day'),
        btnNextDay: document.getElementById('btn-next-day'),
        btnJumpToday: document.getElementById('btn-jump-today'),
        currentDateText: document.getElementById('current-date-text'),

        // Overview & Summary
        calRemainingVal: document.getElementById('cal-remaining-val'),
        calStatusText: document.getElementById('cal-status-text'),
        calConsumedVal: document.getElementById('cal-consumed-val'),
        calTargetVal: document.getElementById('cal-target-val'),
        calProgressFill: document.getElementById('cal-progress-fill'),
        calPctText: document.getElementById('cal-pct-text'),
        calConsumedRatio: document.getElementById('cal-consumed-ratio'),

        macroProteinConsumed: document.getElementById('macro-protein-consumed'),
        macroProteinTarget: document.getElementById('macro-protein-target'),
        macroBarProtein: document.getElementById('macro-bar-protein'),

        macroCarbsConsumed: document.getElementById('macro-carbs-consumed'),
        macroCarbsTarget: document.getElementById('macro-carbs-target'),
        macroBarCarbs: document.getElementById('macro-bar-carbs'),

        macroFatConsumed: document.getElementById('macro-fat-consumed'),
        macroFatTarget: document.getElementById('macro-fat-target'),
        macroBarFat: document.getElementById('macro-bar-fat'),

        macroFiberConsumed: document.getElementById('macro-fiber-consumed'),
        macroFiberTarget: document.getElementById('macro-fiber-target'),
        macroBarFiber: document.getElementById('macro-bar-fiber'),

        sidebarUserTarget: document.getElementById('sidebar-user-target'),

        // Overview & Today's Summary
        overviewDateStr: document.getElementById('overview-date-str'),
        todayMealsSummaryGrid: document.getElementById('today-meals-summary-grid'),
        btnOverviewOpenDiary: document.getElementById('btn-overview-open-diary'),

        // Diary Date & Meals
        diaryPrevDay: document.getElementById('diary-prev-day'),
        diaryNextDay: document.getElementById('diary-next-day'),
        diaryJumpTodayBtn: document.getElementById('diary-jump-today-btn'),
        diaryDateInput: document.getElementById('diary-date-input'),
        diaryCurrentDateText: document.getElementById('diary-current-date-text'),
        diaryDateRelativeTag: document.getElementById('diary-date-relative-tag'),
        diaryDaySummary: document.getElementById('diary-day-summary'),
        diarySummaryCals: document.getElementById('diary-summary-cals'),
        diarySummaryMacros: document.getElementById('diary-summary-macros'),
        diaryEmptyDay: document.getElementById('diary-empty-day'),
        diaryEmptyTitle: document.getElementById('diary-empty-title'),
        diaryEmptyDesc: document.getElementById('diary-empty-desc'),
        diaryMealsContainer: document.getElementById('diary-meals-container'),
        btnDiaryAddFood: document.getElementById('btn-diary-add-food'),

        // Food Library
        libSearchInput: document.getElementById('lib-search-input'),
        libFilterBtns: document.querySelectorAll('.filter-controls .filter-btn'),
        libGroupSelect: document.getElementById('lib-group-select'),
        libQualitySelect: document.getElementById('lib-quality-select'),
        libSortSelect: document.getElementById('lib-sort-select'),
        btnClearLibFilters: document.getElementById('btn-clear-lib-filters'),
        libTableBody: document.getElementById('library-table-body'),

        // Recipe Lab
        recipeTabsContainer: document.getElementById('recipe-tabs-container'),
        activeRecipeTitle: document.getElementById('active-recipe-title'),
        activeRecipeMethod: document.getElementById('active-recipe-method'),
        activeRecipeNotes: document.getElementById('active-recipe-notes'),
        labSliderYield: document.getElementById('lab-slider-yield'),
        labYieldVal: document.getElementById('lab-yield-val'),
        labSliderServings: document.getElementById('lab-slider-servings'),
        labServingsVal: document.getElementById('lab-servings-val'),
        recipeIngredientsTableBody: document.getElementById('recipe-ingredients-table-body'),
        resRawWeight: document.getElementById('res-raw-weight'),
        resCookedWeight: document.getElementById('res-cooked-weight'),
        resServingWeight: document.getElementById('res-serving-weight'),
        resPer100Kcal: document.getElementById('res-per100-kcal'),
        resPer100Macros: document.getElementById('res-per100-macros'),
        resServingKcal: document.getElementById('res-serving-kcal'),
        resServingMacros: document.getElementById('res-serving-macros'),
        btnLogCalculatedRecipe: document.getElementById('btn-log-calculated-recipe'),
        tabBtnCurated: document.getElementById('tab-btn-curated'),
        tabBtnCustom: document.getElementById('tab-btn-custom'),
        customRecipesBadge: document.getElementById('custom-recipes-badge'),
        btnOpenCreateRecipe: document.getElementById('btn-open-create-recipe'),
        sectionCuratedRecipes: document.getElementById('section-curated-recipes'),
        sectionCustomRecipes: document.getElementById('section-custom-recipes'),
        customRecipesListContainer: document.getElementById('custom-recipes-list-container'),

        // Custom Recipe Formulation Modal
        modalCustomRecipe: document.getElementById('modal-custom-recipe'),
        modalCustomRecipeTitle: document.getElementById('modal-custom-recipe-title'),
        btnCloseCustomRecipe: document.getElementById('btn-close-custom-recipe'),
        btnCancelCustomRecipe: document.getElementById('btn-cancel-custom-recipe'),
        customRecipeEditId: document.getElementById('custom-recipe-edit-id'),
        customRecipeTitle: document.getElementById('custom-recipe-title'),
        customRecipeCuisine: document.getElementById('custom-recipe-cuisine'),
        customRecipeDesc: document.getElementById('custom-recipe-desc'),
        customRecipeMethod: document.getElementById('custom-recipe-method'),
        customIngredientsTbody: document.getElementById('custom-ingredients-tbody'),
        btnAddIngredientRow: document.getElementById('btn-add-ingredient-row'),
        customRecipeCookedWeight: document.getElementById('custom-recipe-cooked-weight'),
        customRecipeYieldFactor: document.getElementById('custom-recipe-yield-factor'),
        customRecipeServings: document.getElementById('custom-recipe-servings'),
        btnCalcCustomPreview: document.getElementById('btn-calc-custom-preview'),
        btnSaveCustomRecipe: document.getElementById('btn-save-custom-recipe'),
        previewBatchKcal: document.getElementById('preview-batch-kcal'),
        previewBatchMacros: document.getElementById('preview-batch-macros'),
        previewBatchWeight: document.getElementById('preview-batch-weight'),
        previewPer100Kcal: document.getElementById('preview-per100-kcal'),
        previewPer100Macros: document.getElementById('preview-per100-macros'),
        previewServingKcal: document.getElementById('preview-serving-kcal'),
        previewServingMacros: document.getElementById('preview-serving-macros'),
        previewServingWeight: document.getElementById('preview-serving-weight'),
        previewIncompleteWarning: document.getElementById('preview-incomplete-warning'),

        // Progress
        trendBarsContainer: document.getElementById('trend-bars-container'),
        trendAvgCal: document.getElementById('trend-avg-cal'),
        trendLoggedDays: document.getElementById('trend-logged-days'),
        progressTargetRef: document.getElementById('progress-target-ref'),
        macroPctProtein: document.getElementById('macro-pct-protein'),
        macroPctCarbs: document.getElementById('macro-pct-carbs'),
        macroPctFat: document.getElementById('macro-pct-fat'),

        // Settings Form
        targetsForm: document.getElementById('targets-form'),
        settingCalTarget: document.getElementById('setting-cal-target'),
        settingProteinTarget: document.getElementById('setting-protein-target'),
        settingCarbsTarget: document.getElementById('setting-carbs-target'),
        settingFatTarget: document.getElementById('setting-fat-target'),
        settingFiberTarget: document.getElementById('setting-fiber-target'),
        btnResetTargets: document.getElementById('btn-reset-targets'),

        // Add Food Modal
        modalLogFood: document.getElementById('modal-log-food'),
        btnOpenLogModal: document.getElementById('btn-open-log-modal'),
        btnCloseLogModal: document.getElementById('btn-close-log-modal'),
        btnCancelLog: document.getElementById('btn-cancel-log'),
        modalMealSelect: document.getElementById('modal-meal-select'),
        modalFoodSearch: document.getElementById('modal-food-search'),
        modalSearchResults: document.getElementById('modal-search-results'),
        modalSelectedDetails: document.getElementById('modal-selected-food-details'),
        modalSelName: document.getElementById('modal-sel-name'),
        modalSelState: document.getElementById('modal-sel-state'),
        modalPortionSelect: document.getElementById('modal-portion-select'),
        modalQtyInput: document.getElementById('modal-qty-input'),
        modalPreviewKcal: document.getElementById('modal-preview-kcal'),
        modalPreviewP: document.getElementById('modal-preview-p'),
        modalPreviewC: document.getElementById('modal-preview-c'),
        modalPreviewF: document.getElementById('modal-preview-f'),
        modalPreviewFib: document.getElementById('modal-preview-fib'),
        btnConfirmLog: document.getElementById('btn-confirm-log'),

        // Edit Entry Modal
        modalEditEntry: document.getElementById('modal-edit-entry'),
        btnCloseEditModal: document.getElementById('btn-close-edit-modal'),
        btnCancelEdit: document.getElementById('btn-cancel-edit'),
        btnConfirmEdit: document.getElementById('btn-confirm-edit'),
        editEntryId: document.getElementById('edit-entry-id'),
        modalEditName: document.getElementById('modal-edit-name'),
        modalEditMeal: document.getElementById('modal-edit-meal'),
        modalEditPortionDesc: document.getElementById('modal-edit-portion-desc'),
        modalEditQtyInput: document.getElementById('modal-edit-qty-input'),
        modalEditPreviewKcal: document.getElementById('modal-edit-preview-kcal'),
        modalEditPreviewP: document.getElementById('modal-edit-preview-p'),
        modalEditPreviewC: document.getElementById('modal-edit-preview-c'),
        modalEditPreviewF: document.getElementById('modal-edit-preview-f'),
        modalEditPreviewFib: document.getElementById('modal-edit-preview-fib'),

        // Food Details Modal
        modalFoodInfo: document.getElementById('modal-food-info'),
        btnCloseInfoModal: document.getElementById('btn-close-info-modal'),
        btnDismissInfo: document.getElementById('btn-dismiss-info'),
        btnLogFromInfo: document.getElementById('btn-log-from-info'),
        infoModalCode: document.getElementById('info-modal-code'),
        infoModalTitle: document.getElementById('info-modal-title'),
        infoModalState: document.getElementById('info-modal-state'),
        infoModalGroup: document.getElementById('info-modal-group'),
        infoModalQuality: document.getElementById('info-modal-quality'),
        infoModalKcal: document.getElementById('info-modal-kcal'),
        infoModalKj: document.getElementById('info-modal-kj'),
        infoModalP: document.getElementById('info-modal-p'),
        infoModalC: document.getElementById('info-modal-c'),
        infoModalF: document.getElementById('info-modal-f'),
        infoModalFib: document.getElementById('info-modal-fib'),
        infoModalWater: document.getElementById('info-modal-water'),
        infoModalAsh: document.getElementById('info-modal-ash'),
        infoModalPortions: document.getElementById('info-modal-portions'),
        infoModalCitation: document.getElementById('info-modal-citation'),

        // Toast
        toastNotice: document.getElementById('toast-notice')
    };

    // Helper: Toast Notice
    function showToast(message) {
        if (!DOM.toastNotice) return;
        DOM.toastNotice.textContent = message;
        DOM.toastNotice.classList.add('show');
        setTimeout(() => DOM.toastNotice.classList.remove('show'), 2600);
    }

    // Helper: Cookie extraction (CSRF XSRF-TOKEN)
    function getCookie(name) {
        const matches = document.cookie.match(new RegExp(
            '(?:^|; )' + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + '=([^;]*)'
        ));
        return matches ? decodeURIComponent(matches[1]) : null;
    }

    // Helper: Authenticated Fetch with CSRF and same-origin credentials
    async function apiFetch(url, options = {}) {
        options.credentials = 'same-origin';
        options.headers = options.headers || {};

        const method = (options.method || 'GET').toUpperCase();
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
            let csrfToken = getCookie('XSRF-TOKEN');
            if (!csrfToken) {
                try {
                    const csrfRes = await fetch('/api/auth/csrf');
                    if (csrfRes.ok) {
                        const csrfData = await csrfRes.json();
                        csrfToken = csrfData.token || getCookie('XSRF-TOKEN');
                    }
                } catch (e) {
                    console.warn('Failed to retrieve CSRF token:', e);
                }
            }
            if (csrfToken) {
                options.headers['X-XSRF-TOKEN'] = csrfToken;
            }
        }

        const res = await fetch(url, options);

        if (res.status === 401 && !url.includes('/api/auth/login') && !url.includes('/api/auth/register') && !url.includes('/api/auth/me')) {
            state.currentUser = null;
            showAuthView();
            showToast('Session expired. Please sign in.');
        }

        return res;
    }

    // Helper: Format Date Key (YYYY-MM-DD)
    function formatDateKey(date) {
        if (!date) return '';
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    // Helper: Parse YYYY-MM-DD into Date object
    function parseDateInput(str) {
        if (!str) return null;
        const parts = str.split('-');
        if (parts.length !== 3) return null;
        const year = parseInt(parts[0], 10);
        const month = parseInt(parts[1], 10) - 1;
        const day = parseInt(parts[2], 10);
        if (isNaN(year) || isNaN(month) || isNaN(day)) return null;
        return new Date(year, month, day);
    }

    // Helper: Format Readable Date Header
    function formatReadableDate(date) {
        if (!date) return '';
        const today = new Date();
        const isToday = formatDateKey(date) === formatDateKey(today);

        const options = { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' };
        let str = date.toLocaleDateString('en-US', options);

        if (DOM.currentDateText) {
            if (isToday) {
                DOM.currentDateText.classList.add('is-today');
            } else {
                DOM.currentDateText.classList.remove('is-today');
            }
        }

        if (isToday) {
            return `Today, ${date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`;
        } else {
            return str;
        }
    }

    // =========================================================================
    // 1. Authentication View & Lifecycle Management
    // =========================================================================

    function showAuthView() {
        if (DOM.viewAuth) DOM.viewAuth.style.display = 'flex';
        if (DOM.viewNutritionOnboarding) DOM.viewNutritionOnboarding.style.display = 'none';
        if (DOM.mainAppLayout) DOM.mainAppLayout.style.display = 'none';
        if (DOM.loginPassword) DOM.loginPassword.value = '';
        if (DOM.registerPassword) DOM.registerPassword.value = '';
        hideAuthAlert();
    }

    function showMainAppView() {
        if (DOM.viewAuth) DOM.viewAuth.style.display = 'none';
        if (DOM.viewNutritionOnboarding) DOM.viewNutritionOnboarding.style.display = 'none';
        if (DOM.mainAppLayout) DOM.mainAppLayout.style.display = 'flex';

        if (state.currentUser) {
            const name = state.currentUser.fullName || state.currentUser.email || 'User';
            if (DOM.sidebarUserName) DOM.sidebarUserName.textContent = name;
            if (DOM.sidebarUserEmail) DOM.sidebarUserEmail.textContent = state.currentUser.email;
            if (DOM.sidebarUserAvatar) DOM.sidebarUserAvatar.textContent = name.charAt(0).toUpperCase();
        }
    }

    function showNutritionOnboarding() {
        if (DOM.viewAuth) DOM.viewAuth.style.display = 'none';
        if (DOM.viewNutritionOnboarding) DOM.viewNutritionOnboarding.style.display = 'flex';
        if (DOM.mainAppLayout) DOM.mainAppLayout.style.display = 'none';
        if (DOM.onboardingAlert) DOM.onboardingAlert.style.display = 'none';
    }

    async function routeAuthenticatedUser() {
        try {
            const res = await apiFetch('/api/nutrition-profile');
            if (!res.ok) throw new Error('Unable to load nutrition setup.');
            const profile = await res.json();
            if (!profile.complete) {
                showNutritionOnboarding();
                return false;
            }

            state.targets = {
                dailyCalories: profile.dailyCalories,
                proteinG: profile.proteinG,
                carbsG: profile.carbsG,
                fatG: profile.fatG,
                fiberG: profile.fiberG
            };
            showMainAppView();
            updateTargetsUI();
            await checkAndMigrateLocalStorage();
            await Promise.all([
                loadTargets(),
                loadTodayLog(),
                loadDiaryLog(formatDateKey(state.diaryDate)),
                loadCustomRecipes()
            ]);
            return true;
        } catch (err) {
            console.error('Nutrition profile check failed:', err);
            showNutritionOnboarding();
            if (DOM.onboardingAlert) {
                DOM.onboardingAlert.textContent = 'Unable to load your nutrition setup. Please try again.';
                DOM.onboardingAlert.style.display = 'block';
            }
            return false;
        }
    }

    function showAuthAlert(message) {
        if (DOM.authAlertBox) {
            DOM.authAlertBox.textContent = message;
            DOM.authAlertBox.style.display = 'block';
        }
    }

    function hideAuthAlert() {
        if (DOM.authAlertBox) {
            DOM.authAlertBox.style.display = 'none';
        }
    }

    async function checkAuthSession() {
        try {
            // Pre-seed CSRF cookie
            await fetch('/api/auth/csrf');

            const res = await fetch('/api/auth/me', { credentials: 'same-origin' });
            if (res.ok) {
                state.currentUser = await res.json();
                await routeAuthenticatedUser();
                return true;
            }
        } catch (e) {
            console.warn('Session check failed:', e);
        }

        state.currentUser = null;
        showAuthView();
        return false;
    }

    async function handleLogin(e) {
        e.preventDefault();
        hideAuthAlert();

        const email = DOM.loginEmail.value.trim();
        const password = DOM.loginPassword.value;

        if (!email || !password) {
            showAuthAlert('Please enter both email and password.');
            return;
        }

        try {
            const res = await apiFetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email, password })
            });

            if (res.ok) {
                state.currentUser = await res.json();
                const ready = await routeAuthenticatedUser();
                if (ready) showToast(`Welcome back, ${state.currentUser.fullName || 'Athlete'}!`);
            } else {
                showAuthAlert('Invalid email or password. Please try again.');
            }
        } catch (err) {
            console.error('Login error:', err);
            showAuthAlert('Unable to connect to authentication service.');
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        hideAuthAlert();

        const fullName = DOM.registerName.value.trim();
        const email = DOM.registerEmail.value.trim();
        const password = DOM.registerPassword.value;

        if (!fullName || !email || !password) {
            showAuthAlert('All fields are required.');
            return;
        }

        if (password.length < 8) {
            showAuthAlert('Password must be at least 8 characters long.');
            return;
        }

        try {
            const res = await apiFetch('/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ fullName, email, password })
            });

            if (res.ok) {
                showToast('Account created! Signing you in...');
                // Automatically log in
                DOM.loginEmail.value = email;
                DOM.loginPassword.value = password;
                await handleLogin(e);
            } else {
                const errData = await res.json().catch(() => ({}));
                showAuthAlert(errData.message || 'Registration failed. Email may already be in use.');
            }
        } catch (err) {
            console.error('Registration error:', err);
            showAuthAlert('Unable to complete registration.');
        }
    }

    async function handleNutritionOnboarding(e) {
        e.preventDefault();
        if (DOM.onboardingAlert) DOM.onboardingAlert.style.display = 'none';
        const profile = {
            age: Number(DOM.onboardingAge.value),
            sexForEstimate: DOM.onboardingSex.value,
            heightCm: Number(DOM.onboardingHeight.value),
            weightKg: Number(DOM.onboardingWeight.value),
            activityLevel: DOM.onboardingActivity.value,
            goal: DOM.onboardingGoal.value
        };
        try {
            const res = await apiFetch('/api/nutrition-profile', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(profile)
            });
            if (!res.ok) {
                const responseText = await res.text();
                let error = {};
                try { error = responseText ? JSON.parse(responseText) : {}; } catch (parseError) {}
                let serverMessage = error.message || error.detail;
                if (!serverMessage && error.error && error.error !== 'Not Found' && error.error !== 'Internal Server Error') {
                    serverMessage = error.error;
                }
                const fallback = res.status === 404
                    ? 'The nutrition onboarding service is not available (404 Not Found). Please verify the server build is up to date.'
                    : res.status === 403
                        ? 'Your security token expired. Sign out, sign in again, and resubmit your answers.'
                        : res.status >= 500
                            ? 'FitKit could not save your setup. Please try again or check the server logs.'
                            : `FitKit rejected the questionnaire (HTTP ${res.status}). Check the entered values and try again.`;
                throw new Error(serverMessage || fallback);
            }
            const saved = await res.json();
            state.targets = {
                dailyCalories: saved.dailyCalories,
                proteinG: saved.proteinG,
                carbsG: saved.carbsG,
                fatG: saved.fatG,
                fiberG: saved.fiberG
            };
            showMainAppView();
            updateTargetsUI();
            await checkAndMigrateLocalStorage();
            await Promise.all([
                loadTargets(),
                loadTodayLog(),
                loadDiaryLog(formatDateKey(state.diaryDate)),
                loadCustomRecipes()
            ]);
            showToast('Your estimated nutrition targets are ready. You can adjust them in Settings.');
        } catch (err) {
            if (DOM.onboardingAlert) {
                DOM.onboardingAlert.textContent = err.message || 'Unable to save your answers.';
                DOM.onboardingAlert.style.display = 'block';
            }
        }
    }

    async function handleLogout() {
        try {
            await apiFetch('/api/auth/logout', { method: 'POST' });
        } catch (e) {}

        state.currentUser = null;
        state.targets = {};
        showAuthView();
        showToast('Signed out of FitKit.');
    }

    // =========================================================================
    // 2. User Nutrition Targets (Authoritative Authenticated Persistence)
    // =========================================================================

    async function loadTargets() {
        try {
            const res = await apiFetch('/api/targets');
            if (res.ok) {
                const data = await res.json();
                state.targets = data;
            }
        } catch (err) {
            console.warn('Unable to fetch targets from backend:', err);
        }
        updateTargetsUI();
    }

    async function saveTargets(newTargets) {
        try {
            const res = await apiFetch('/api/targets', {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(newTargets)
            });
            if (res.ok) {
                const updated = await res.json();
                state.targets = updated;
                updateTargetsUI();
                await Promise.all([
                    loadTodayLog(),
                    loadDiaryLog(formatDateKey(state.diaryDate))
                ]);
                showToast('Daily targets saved.');
            } else {
                throw new Error('Server returned ' + res.status);
            }
        } catch (err) {
            console.error('Failed to save targets:', err);
            showToast('Failed to save targets to server.');
        }
    }

    function updateTargetsUI() {
        const t = state.targets || {};
        const cal = t.dailyCalories;
        const p = t.proteinG;
        const c = t.carbsG;
        const f = t.fatG;
        const fib = t.fiberG;

        if (DOM.calTargetVal) DOM.calTargetVal.textContent = cal == null ? '--' : Number(cal).toLocaleString();
        if (DOM.sidebarUserTarget) DOM.sidebarUserTarget.textContent = cal == null ? 'Complete nutrition setup' : `Target: ${Number(cal).toLocaleString()} kcal`;
        if (DOM.macroProteinTarget) DOM.macroProteinTarget.textContent = p == null ? '--' : Number(p).toFixed(0);
        if (DOM.macroCarbsTarget) DOM.macroCarbsTarget.textContent = c == null ? '--' : Number(c).toFixed(0);
        if (DOM.macroFatTarget) DOM.macroFatTarget.textContent = f == null ? '--' : Number(f).toFixed(0);
        if (DOM.macroFiberTarget) DOM.macroFiberTarget.textContent = fib == null ? '--' : Number(fib).toFixed(0);
        if (DOM.progressTargetRef) DOM.progressTargetRef.textContent = cal == null ? '--' : Number(cal).toLocaleString();

        const totalCal = state.todayLog ? Number(state.todayLog.totalCalories || 0) : 0;
        if (DOM.calConsumedRatio) {
            DOM.calConsumedRatio.textContent = cal == null
                ? (state.todayLog ? `${Math.round(totalCal).toLocaleString()} kcal logged` : '--')
                : `${Math.round(totalCal).toLocaleString()} / ${Number(cal).toLocaleString()} kcal`;
        }
        if (DOM.calRemainingVal) {
            if (cal == null) {
                DOM.calRemainingVal.textContent = '--';
                DOM.calRemainingVal.style.color = 'var(--text-primary)';
                if (DOM.calStatusText) DOM.calStatusText.textContent = 'Daily target not set';
                if (DOM.calProgressFill) {
                    DOM.calProgressFill.style.width = '0%';
                    DOM.calProgressFill.className = 'progress-fill';
                }
                if (DOM.calPctText) DOM.calPctText.textContent = 'Set up a personal target in Settings';
            } else {
                const remaining = Number(cal) - totalCal;
                DOM.calRemainingVal.textContent = Math.round(Math.abs(remaining)).toLocaleString();
                DOM.calRemainingVal.style.color = remaining >= 0 ? 'var(--text-primary)' : 'var(--state-error)';
                if (DOM.calStatusText) DOM.calStatusText.textContent = remaining >= 0 ? 'Calories remaining' : 'Calories over target';
                const pct = Math.min(Math.round((totalCal / Number(cal)) * 100), 100);
                if (DOM.calPctText) DOM.calPctText.textContent = `${pct}% of daily target`;
                if (DOM.calProgressFill) {
                    DOM.calProgressFill.style.width = `${pct}%`;
                    DOM.calProgressFill.className = remaining >= 0 ? 'progress-fill' : 'progress-fill over-target';
                }
            }
        }

        updateMacroBar('protein', state.todayLog ? (state.todayLog.totalProteinG || 0) : 0, p);
        updateMacroBar('carbs', state.todayLog ? (state.todayLog.totalCarbsG || 0) : 0, c);
        updateMacroBar('fat', state.todayLog ? (state.todayLog.totalFatG || 0) : 0, f);
        updateMacroBar('fiber', state.todayLog ? (state.todayLog.totalFiberG || 0) : 0, fib);

        // Populate Settings Form
        if (DOM.settingCalTarget) DOM.settingCalTarget.value = cal ?? '';
        if (DOM.settingProteinTarget) DOM.settingProteinTarget.value = p ?? '';
        if (DOM.settingCarbsTarget) DOM.settingCarbsTarget.value = c ?? '';
        if (DOM.settingFatTarget) DOM.settingFatTarget.value = f ?? '';
        if (DOM.settingFiberTarget) DOM.settingFiberTarget.value = fib ?? '';
    }

    // =========================================================================
    // 3. Persistent Meal Diary & Daily Overview (Authoritative Authenticated)
    // =========================================================================

    async function loadDailyLog(dateStr) {
        try {
            const res = await apiFetch(`/api/meals?date=${dateStr}`);
            if (!res.ok) throw new Error('Failed to load meal log: ' + res.status);
            state.dailyLog = await res.json();
            renderDailyOverview();
            loadWeeklyHistory();
        } catch (err) {
            console.error('Error loading daily log:', err);
        }
    }

    function renderDailyOverview() {
        const dateStr = formatDateKey(state.currentDate);
        DOM.currentDateText.textContent = formatReadableDate(state.currentDate);

        if (!state.dailyLog) return;
        const data = state.dailyLog;

        const meals = ['Breakfast', 'Lunch', 'Dinner', 'Snacks'];

        meals.forEach(mealName => {
            const section = (data.meals && data.meals[mealName]) ? data.meals[mealName] : { entries: [], subtotalCalories: 0 };
            const items = section.entries || [];
            const subtotal = section.subtotalCalories || 0;

            const rowsHtml = items.map(item => {
                const stateTag = formatStateLabel(item.foodState);
                const calVal = Math.round(Number(item.energyKcal || 0));
                const pVal = Number(item.proteinG || 0).toFixed(1);
                const cVal = Number(item.carbohydratesG || 0).toFixed(1);
                const fVal = Number(item.fatG || 0).toFixed(1);

                return `
                    <li class="food-row">
                        <div class="food-info">
                            <div class="food-title-line">
                                <span class="food-name">${escapeHtml(item.foodName)}</span>
                                <span class="food-tag">${stateTag}</span>
                            </div>
                            <div class="food-portion-desc">${escapeHtml(item.portionDescription)}</div>
                        </div>
                        <div class="food-nutrients-summary">
                            <span class="food-calories">${calVal} kcal</span>
                            <span class="food-macros-text">P: ${pVal}g • C: ${cVal}g • F: ${fVal}g</span>
                        </div>
                        <div class="food-actions">
                            <button class="food-row-btn btn-edit" onclick="window.FitKitApp.openEditEntryModal(${item.id})" title="Edit quantity" aria-label="Edit quantity">
                                <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                    <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                </svg>
                            </button>
                            <button class="food-row-btn btn-remove" onclick="window.FitKitApp.deleteDiaryEntry(${item.id})" title="Delete entry" aria-label="Delete entry">
                                <svg viewBox="0 0 24 24" width="15" height="15" fill="none" stroke="currentColor" stroke-width="2">
                                    <line x1="18" y1="6" x2="6" y2="18"></line>
                                    <line x1="6" y1="6" x2="18" y2="18"></line>
                                </svg>
                            </button>
                        </div>
                    </li>
                `;
            }).join('');

            const emptyHtml = `
                <div class="meal-empty">
                    <span>No food logged yet for ${mealName.toLowerCase()}.</span>
                    <button class="btn btn-secondary btn-sm" onclick="window.FitKitApp.openLogModalForMeal('${mealName}')">+ Add food</button>
                </div>
            `;

            const entriesElem = document.getElementById(`entries-${mealName}`);
            if (entriesElem) {
                entriesElem.innerHTML = items.length > 0 ? rowsHtml : emptyHtml;
            }

            const subtotalElem = document.getElementById(`subtotal-${mealName}`);
            if (subtotalElem) {
                subtotalElem.textContent = `${Math.round(Number(subtotal))} kcal`;
            }
        });

        if (DOM.diaryFullContainer && DOM.mealsContainer) {
            DOM.diaryFullContainer.innerHTML = DOM.mealsContainer.innerHTML;
        }

        const totalCal = Number(data.totalCalories || 0);
        const targetValue = data.targetCalories ?? state.targets.dailyCalories;
        const targetCal = targetValue == null ? null : Number(targetValue);
        const remainingCal = targetCal == null ? null : targetCal - totalCal;
        const calPct = targetCal == null || targetCal <= 0 ? null : Math.min(Math.round((totalCal / targetCal) * 100), 100);

        DOM.calConsumedVal.textContent = Math.round(totalCal).toLocaleString();
        DOM.calConsumedRatio.textContent = targetCal == null ? `${Math.round(totalCal).toLocaleString()} kcal logged` : `${Math.round(totalCal).toLocaleString()} / ${targetCal.toLocaleString()} kcal`;
        DOM.calPctText.textContent = calPct == null ? 'Set up a personal target in Settings' : `${calPct}% of daily target`;

        if (remainingCal == null) {
            DOM.calRemainingVal.textContent = '--';
            DOM.calRemainingVal.style.color = 'var(--text-primary)';
            DOM.calStatusText.textContent = 'Daily target not set';
            DOM.calProgressFill.style.width = '0%';
            DOM.calProgressFill.className = 'progress-fill';
        } else if (remainingCal >= 0) {
            DOM.calRemainingVal.textContent = Math.round(remainingCal).toLocaleString();
            DOM.calRemainingVal.style.color = 'var(--text-primary)';
            DOM.calStatusText.textContent = 'Calories remaining';
            DOM.calProgressFill.style.width = `${(totalCal / targetCal) * 100}%`;
            DOM.calProgressFill.className = 'progress-fill';
        } else {
            DOM.calRemainingVal.textContent = Math.abs(Math.round(remainingCal)).toLocaleString();
            DOM.calRemainingVal.style.color = 'var(--state-error)';
            DOM.calStatusText.textContent = 'Calories over target';
            DOM.calProgressFill.style.width = '100%';
            DOM.calProgressFill.className = 'progress-fill over-target';
        }

        updateMacroBar('protein', data.totalProteinG || 0, data.targetProteinG ?? state.targets.proteinG);
        updateMacroBar('carbs', data.totalCarbsG || 0, data.targetCarbsG ?? state.targets.carbsG);
        updateMacroBar('fat', data.totalFatG || 0, data.targetFatG ?? state.targets.fatG);
        updateMacroBar('fiber', data.totalFiberG || 0, data.targetFiberG ?? state.targets.fiberG);
    }

    function updateMacroBar(name, consumed, target) {
        const consumedElem = document.getElementById(`macro-${name}-consumed`);
        const barElem = document.getElementById(`macro-bar-${name}`);
        if (consumedElem) consumedElem.textContent = Number(consumed).toFixed(1);
        const targetElem = document.getElementById(`macro-${name}-target`);
        if (targetElem) targetElem.textContent = target == null ? '--' : Number(target).toFixed(0);

        if (barElem) {
            const pct = target != null && target > 0 ? Math.min(Math.round((consumed / target) * 100), 100) : 0;
            barElem.style.width = `${pct}%`;
        }
    }

    // =========================================================================
    // 4. Weekly Trends (Authoritative Server History)
    // =========================================================================

    async function loadWeeklyHistory() {
        try {
            const todayStr = formatDateKey(new Date());
            const res = await apiFetch(`/api/meals/history?endDate=${todayStr}&days=7`);
            if (!res.ok) throw new Error('History fetch error: ' + res.status);
            state.weeklyHistory = await res.json();
            renderWeeklyTrends();
        } catch (err) {
            console.error('Failed to load history:', err);
        }
    }

    function renderWeeklyTrends() {
        const days = state.weeklyHistory || [];
        if (days.length === 0) return;

        let totalWeekCal = 0;
        let loggedDays = 0;
        let totalP = 0;
        let totalC = 0;
        let totalF = 0;

        days.forEach(d => {
            const cal = Number(d.totalCalories || 0);
            if (d.hasEntries && cal > 0) {
                loggedDays++;
                totalWeekCal += cal;
                totalP += Number(d.totalProteinG || 0);
                totalC += Number(d.totalCarbsG || 0);
                totalF += Number(d.totalFatG || 0);
            }
        });

        const targetCal = state.targets ? state.targets.dailyCalories : null;
        const baseline = targetCal != null ? Number(targetCal) : 2000;
        const maxCal = Math.max(baseline * 1.25, ...days.map(d => Number(d.totalCalories || 0)), 1500);

        DOM.trendBarsContainer.innerHTML = days.map(day => {
            const cal = Number(day.totalCalories || 0);
            const barHeightPx = cal > 0 ? Math.min(Math.round((cal / maxCal) * 130), 130) : 4;
            const isOver = targetCal != null && cal > targetCal;

            return `
                <div class="trend-col">
                    <span class="trend-cal-val">${day.hasEntries ? cal : '-'}</span>
                    <div class="trend-bar-wrapper">
                        <div class="trend-bar ${isOver ? 'over' : ''}" style="height: ${barHeightPx}px;"></div>
                    </div>
                    <span class="trend-day-label">${escapeHtml(day.dayLabel)}</span>
                </div>
            `;
        }).join('');

        const avg = loggedDays > 0 ? Math.round(totalWeekCal / loggedDays) : 0;
        DOM.trendAvgCal.textContent = `${avg.toLocaleString()} kcal`;
        DOM.trendLoggedDays.textContent = `${loggedDays} of 7 days`;

        const sumEnergy = (totalP * 4) + (totalC * 4) + (totalF * 9);
        if (sumEnergy > 0) {
            DOM.macroPctProtein.textContent = `${Math.round(((totalP * 4) / sumEnergy) * 100)}%`;
            DOM.macroPctCarbs.textContent = `${Math.round(((totalC * 4) / sumEnergy) * 100)}%`;
            DOM.macroPctFat.textContent = `${Math.round(((totalF * 9) / sumEnergy) * 100)}%`;
        } else {
            DOM.macroPctProtein.textContent = '20%';
            DOM.macroPctCarbs.textContent = '55%';
            DOM.macroPctFat.textContent = '25%';
        }
    }

    // =========================================================================
    // 5. Safe Browser LocalStorage Migration (Authenticated Owner Scoped)
    // =========================================================================

    async function checkAndMigrateLocalStorage() {
        if (!state.currentUser) return;
        const isMigrated = localStorage.getItem(`fitkit_migrated_${state.currentUser.email}`);
        if (isMigrated === 'true') return;

        const migrationPayload = [];

        try {
            for (let i = 0; i < localStorage.length; i++) {
                const key = localStorage.key(i);
                if (key && key.startsWith('fitkit_diary_')) {
                    const dateStr = key.replace('fitkit_diary_', '');
                    const raw = localStorage.getItem(key);
                    if (raw) {
                        try {
                            const parsed = JSON.parse(raw);
                            migrationPayload.push({
                                date: dateStr,
                                meals: parsed
                            });
                        } catch (e) {}
                    }
                }
            }

            if (migrationPayload.length > 0) {
                const res = await apiFetch('/api/meals/migrate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(migrationPayload)
                });

                if (res.ok) {
                    const resJson = await res.json();
                    localStorage.setItem(`fitkit_migrated_${state.currentUser.email}`, 'true');
                    if (resJson.migratedEntriesCount > 0) {
                        showToast(`Safely migrated ${resJson.migratedEntriesCount} diary entries to your account.`);
                    }
                }
            } else {
                localStorage.setItem(`fitkit_migrated_${state.currentUser.email}`, 'true');
            }
        } catch (err) {
            console.warn('LocalStorage migration encountered a non-fatal error:', err);
        }
    }

    // =========================================================================
    // 6. Food Catalog (Expanded Indian Library)
    // =========================================================================

    async function loadFoods() {
        try {
            const res = await fetch('/api/foods');
            if (!res.ok) throw new Error('Failed to load foods: ' + res.status);
            state.foods = await res.json();
            state.filteredFoods = [...state.foods];

            // populate food groups dropdown
            try {
                const groupsRes = await fetch('/api/foods/groups');
                if (groupsRes.ok) {
                    const groups = await groupsRes.json();
                    state.foodGroups = groups;
                    populateGroupDropdown(groups);
                } else {
                    const groups = [...new Set(state.foods.map(f => f.foodGroup).filter(Boolean))].sort();
                    state.foodGroups = groups;
                    populateGroupDropdown(groups);
                }
            } catch (ge) {
                const groups = [...new Set(state.foods.map(f => f.foodGroup).filter(Boolean))].sort();
                state.foodGroups = groups;
                populateGroupDropdown(groups);
            }

            renderLibraryTable(state.filteredFoods);
        } catch (err) {
            console.error(err);
            DOM.libTableBody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 24px; color: var(--state-error);">Unable to load food catalog from backend.</td></tr>`;
        }
    }

    function populateGroupDropdown(groups) {
        DOM.libGroupSelect.innerHTML = '<option value="ALL">All Categories</option>';
        groups.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g;
            opt.textContent = g;
            DOM.libGroupSelect.appendChild(opt);
        });
    }

    function renderLibraryTable(foods) {
        if (!foods || foods.length === 0) {
            DOM.libTableBody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 24px; color: var(--text-muted);">No matching foods found.</td></tr>`;
            return;
        }

        DOM.libTableBody.innerHTML = foods.map(food => {
            const kcal = food.energyKcal != null ? Number(food.energyKcal).toFixed(0) : '--';
            const kj = food.energyKj != null ? Number(food.energyKj).toFixed(0) : '--';
            const p = food.proteinG != null ? Number(food.proteinG).toFixed(1) : '--';
            const c = food.availableCarbohydratesG != null ? Number(food.availableCarbohydratesG).toFixed(1) : '--';
            const f = food.totalFatG != null ? Number(food.totalFatG).toFixed(1) : '--';
            const stateLabel = formatStateLabel(food.foodState);

            let qualityBadge = '';
            if (food.qualityStatus === 'SOURCE_VERIFIED') {
                qualityBadge = '<span class="quality-badge quality-verified">Source Verified</span>';
            } else if (food.qualityStatus === 'CORRECTED_ANOMALY') {
                qualityBadge = '<span class="quality-badge quality-corrected">Audit Corrected</span>';
            } else if (food.qualityStatus === 'CALCULATED_ESTIMATE') {
                qualityBadge = '<span class="quality-badge quality-estimate">Calculated</span>';
            } else if (food.qualityStatus === 'UNVERIFIED') {
                qualityBadge = '<span class="quality-badge quality-unverified">Unverified</span>';
            }

            let aliasHtml = '';
            if (food.matchedAlias) {
                aliasHtml = `<span class="alias-tag">Matched: "${escapeHtml(food.matchedAlias)}"</span>`;
            } else if (food.aliases && food.aliases.length > 0) {
                aliasHtml = `<span class="alias-hint">Aliases: ${escapeHtml(food.aliases.slice(0, 3).join(', '))}${food.aliases.length > 3 ? '…' : ''}</span>`;
            }

            return `
                <tr>
                    <td>
                        <strong class="item-main-name" style="cursor:pointer;" onclick="window.FitKitApp.openFoodInfo(${food.id})">${escapeHtml(food.name)}</strong>
                        ${food.scientificName ? `<span class="item-scientific">${escapeHtml(food.scientificName)}</span>` : ''}
                        ${aliasHtml}
                        <div style="display: flex; gap: 6px; align-items: center; margin-top: 4px; flex-wrap: wrap;">
                            <span class="source-tag">${escapeHtml(food.sourceName || 'Catalog')}${food.foodCode ? ` • ${escapeHtml(food.foodCode)}` : ''}</span>
                            ${qualityBadge}
                        </div>
                    </td>
                    <td><span class="food-tag">${stateLabel}</span></td>
                    <td style="color: var(--text-secondary);">${escapeHtml(food.foodGroup || 'General')}</td>
                    <td><strong>${kcal} kcal</strong> <small class="text-secondary" style="display:block;">${kj} kJ</small></td>
                    <td>${p}g</td>
                    <td>${c}g</td>
                    <td>${f}g</td>
                    <td><span style="font-size: 12px; color: var(--text-secondary);">${food.portionCount || 1} portion(s)</span></td>
                    <td>
                        <button class="btn btn-secondary btn-sm" onclick="window.FitKitApp.openLogModalWithFood(${food.id})">
                            + Log
                        </button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    async function applyLibraryFilters() {
        try {
            const params = new URLSearchParams();
            if (state.libSearch && state.libSearch.trim()) {
                params.append('query', state.libSearch.trim());
            }
            if (state.libStateFilter && state.libStateFilter !== 'ALL') {
                params.append('state', state.libStateFilter);
            }
            if (state.libGroupFilter && state.libGroupFilter !== 'ALL') {
                params.append('group', state.libGroupFilter);
            }
            if (state.libQualityFilter && state.libQualityFilter !== 'ALL') {
                params.append('quality', state.libQualityFilter);
            }
            if (state.libSort) {
                params.append('sort', state.libSort);
            }

            const url = '/api/foods' + (params.toString() ? '?' + params.toString() : '');
            const res = await fetch(url);
            if (!res.ok) throw new Error('Search failed: ' + res.status);
            const foods = await res.json();
            state.filteredFoods = foods;
            renderLibraryTable(foods);
        } catch (err) {
            console.error('Error applying library filters:', err);
            DOM.libTableBody.innerHTML = `<tr><td colspan="9" style="text-align:center; padding: 24px; color: var(--state-error);">Unable to retrieve search results.</td></tr>`;
        }
    }

    // =========================================================================
    // 7. Recipe Lab Integration
    // =========================================================================

    function formatCookingMethod(method) {
        if (!method) return 'Home Cooking';
        const methodMap = {
            'ROASTING_TAWA': 'Tawa Roasting',
            'PRESSURE_COOKING': 'Pressure Cooking',
            'SAUTEING_STIRFRY': 'Sautéing / Tadka',
            'BOILING_SIMMERING': 'Boiling & Simmering',
            'STEAMING': 'Steaming',
            'BAKING': 'Baking',
            'DEEP_FRYING': 'Deep Frying',
            'RAW_BLENDING': 'Raw / Blended'
        };
        return methodMap[method] || method.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    }

    async function loadRecipes() {
        try {
            const res = await fetch('/api/recipes');
            if (!res.ok) throw new Error('Failed to load recipes');
            state.recipes = await res.json();

            if (state.recipes.length > 0) {
                renderRecipeTabs(state.recipes);
                selectRecipe(state.recipes[0]);
            }
        } catch (err) {
            console.error('Failed to load recipes:', err);
        }
    }

    function renderRecipeTabs(recipes) {
        DOM.recipeTabsContainer.innerHTML = recipes.map((r, idx) => `
            <button class="recipe-tab-btn ${idx === 0 ? 'active' : ''}" data-id="${r.id}">
                ${escapeHtml(r.title)}
            </button>
        `).join('');

        DOM.recipeTabsContainer.querySelectorAll('.recipe-tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                DOM.recipeTabsContainer.querySelectorAll('.recipe-tab-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                const id = Number(btn.getAttribute('data-id'));
                const rec = state.recipes.find(r => r.id === id);
                if (rec) selectRecipe(rec);
            });
        });
    }

    function selectRecipe(recipe) {
        state.selectedRecipe = recipe;
        DOM.activeRecipeTitle.textContent = recipe.title;
        DOM.activeRecipeMethod.textContent = formatCookingMethod(recipe.cookingMethod);
        DOM.activeRecipeNotes.textContent = recipe.preparationNotes || 'Traditional home recipe.';

        const yieldVal = Number(recipe.cookingYieldFactor || 1.0);
        const servingsVal = Number(recipe.servingsYield || 1.0);

        DOM.labSliderYield.value = yieldVal;
        DOM.labYieldVal.textContent = yieldVal.toFixed(2) + 'x';

        DOM.labSliderServings.value = servingsVal;
        DOM.labServingsVal.textContent = servingsVal + ' servings';

        DOM.recipeIngredientsTableBody.innerHTML = (recipe.ingredients || []).map(ing => {
            const cal = ing.ingredientCalories != null ? Number(ing.ingredientCalories).toFixed(0) : '--';
            const prot = ing.ingredientProtein != null ? Number(ing.ingredientProtein).toFixed(1) : '--';
            const ret = ing.retentionFactorOverride != null ? (Number(ing.retentionFactorOverride) * 100).toFixed(0) + '%' : '100%';
            return `
                <tr>
                    <td><strong>${escapeHtml(ing.foodName || 'Item')}</strong></td>
                    <td>${Number(ing.rawQuantityG).toFixed(1)}g</td>
                    <td>${ret}</td>
                    <td>${cal} kcal</td>
                    <td>${prot}g</td>
                </tr>
            `;
        }).join('');

        recalculateLabRecipe();
    }

    async function recalculateLabRecipe() {
        if (!state.selectedRecipe) return;

        const yieldFactor = parseFloat(DOM.labSliderYield.value);
        const servings = parseFloat(DOM.labSliderServings.value);

        try {
            const url = `/api/recipes/${state.selectedRecipe.id}/calculate?yieldFactor=${yieldFactor}&servings=${servings}`;
            const res = await fetch(url, { method: 'POST' });
            if (!res.ok) throw new Error('Recalculation error');
            const calc = await res.json();

            DOM.resRawWeight.textContent = Number(calc.totalRawWeightG).toFixed(1) + ' g';
            DOM.resCookedWeight.textContent = Number(calc.cookedWeightG).toFixed(1) + ' g';
            DOM.resServingWeight.textContent = Number(calc.servingWeightG).toFixed(1) + ' g';

            DOM.resPer100Kcal.textContent = `${Number(calc.energyKcalPer100g).toFixed(0)} kcal`;
            DOM.resPer100Macros.textContent = `P: ${Number(calc.proteinGPer100g).toFixed(1)}g | C: ${Number(calc.availableCarbsGPer100g || calc.carbohydratesGPer100g || 0).toFixed(1)}g | F: ${Number(calc.fatGPer100g).toFixed(1)}g`;

            DOM.resServingKcal.textContent = `${Number(calc.energyKcalPerServing).toFixed(0)} kcal`;
            DOM.resServingMacros.textContent = `P: ${Number(calc.proteinGPerServing).toFixed(1)}g | C: ${Number(calc.availableCarbsGPerServing || calc.carbohydratesGPerServing || 0).toFixed(1)}g | F: ${Number(calc.fatGPerServing).toFixed(1)}g`;

            DOM.btnLogCalculatedRecipe.onclick = async () => {
                if (!state.currentUser) {
                    showToast('Please sign in to log recipes.');
                    showAuthView();
                    return;
                }

                const dateStr = formatDateKey(state.currentDate);
                const req = {
                    mealType: 'Lunch',
                    recipeId: state.selectedRecipe.id,
                    portionDescription: `1 serving (${Number(calc.servingWeightG).toFixed(0)}g)`,
                    portionGramWeight: Number(calc.servingWeightG),
                    quantityMultiplier: 1.0,
                    logDate: dateStr
                };

                try {
                    const postRes = await apiFetch('/api/meals/entries', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(req)
                    });
                    if (postRes.ok) {
                        state.dailyLog = await postRes.json();
                        renderDailyOverview();
                        loadWeeklyHistory();
                        showToast(`Logged ${state.selectedRecipe.title} to Lunch`);
                        switchView('overview');
                    } else {
                        throw new Error('Server returned ' + postRes.status);
                    }
                } catch (e) {
                    console.error('Failed to log recipe:', e);
                    showToast('Failed to log recipe.');
                }
            };
        } catch (err) {
            console.error('Calculation error:', err);
        }
    }

    // =========================================================================
    // 7b. User-Owned Custom Recipe Management (Phase 6)
    // =========================================================================

    function switchRecipeMode(mode) {
        state.recipeMode = mode;
        if (mode === 'custom') {
            DOM.tabBtnCurated.classList.remove('active');
            DOM.tabBtnCustom.classList.add('active');
            DOM.sectionCuratedRecipes.style.display = 'none';
            DOM.sectionCustomRecipes.style.display = 'block';
            loadCustomRecipes();
        } else {
            DOM.tabBtnCustom.classList.remove('active');
            DOM.tabBtnCurated.classList.add('active');
            DOM.sectionCustomRecipes.style.display = 'none';
            DOM.sectionCuratedRecipes.style.display = 'block';
        }
    }

    async function loadCustomRecipes() {
        if (!state.currentUser) {
            renderCustomRecipes([]);
            if (DOM.customRecipesBadge) DOM.customRecipesBadge.textContent = '0';
            return;
        }

        try {
            const res = await apiFetch('/api/custom-recipes');
            if (res.ok) {
                state.customRecipes = await res.json();
                if (DOM.customRecipesBadge) {
                    DOM.customRecipesBadge.textContent = String(state.customRecipes.length);
                }
                renderCustomRecipes(state.customRecipes);
            }
        } catch (err) {
            console.error('Failed to load custom recipes:', err);
        }
    }

    function renderCustomRecipes(recipes) {
        if (!DOM.customRecipesListContainer) return;

        if (!recipes || recipes.length === 0) {
            DOM.customRecipesListContainer.innerHTML = `
                <div class="empty-custom-recipes">
                    <h4>No Saved Recipes Yet</h4>
                    <p style="max-width: 480px; margin: 0 auto 18px; font-size: 13.5px;">
                        Easily calculate calories and macros for home-cooked meals by adding raw ingredients and finished weight.
                    </p>
                    <button class="btn btn-primary" onclick="window.FitKitApp.openCreateRecipeModal()">
                        + Create Your First Recipe
                    </button>
                </div>
            `;
            return;
        }

        DOM.customRecipesListContainer.innerHTML = recipes.map(r => {
            const calc = r.calculationResult || {};
            const batchKcal = calc.totalBatchCalories != null ? Math.round(Number(calc.totalBatchCalories)) : '--';
            const servingKcal = calc.energyKcalPerServing != null ? Math.round(Number(calc.energyKcalPerServing)) : '--';
            const servingP = calc.proteinGPerServing != null ? Number(calc.proteinGPerServing).toFixed(1) : '--';
            const servingC = (calc.availableCarbsGPerServing || calc.carbohydratesGPerServing) != null ? Number(calc.availableCarbsGPerServing || calc.carbohydratesGPerServing).toFixed(1) : '--';
            const servingF = calc.fatGPerServing != null ? Number(calc.fatGPerServing).toFixed(1) : '--';
            const densityKcal = calc.energyKcalPer100g != null ? Math.round(Number(calc.energyKcalPer100g)) : '--';
            const servingWeight = r.servingWeightG != null ? Math.round(Number(r.servingWeightG)) : (calc.servingWeightG != null ? Math.round(Number(calc.servingWeightG)) : '--');
            const cookedWeight = r.cookedWeightG != null ? Math.round(Number(r.cookedWeightG)) : (calc.cookedWeightG != null ? Math.round(Number(calc.cookedWeightG)) : '--');
            const yieldVal = r.cookingYieldFactor != null ? Number(r.cookingYieldFactor).toFixed(2) : '1.00';
            const servingsCount = r.servingsYield != null ? Number(r.servingsYield).toFixed(1) : '1.0';

            const ingsSummary = (r.ingredients || []).map(i =>
                `${escapeHtml(i.foodName)} (${Number(i.rawQuantityG).toFixed(0)}g)`
            ).join(', ');

            return `
                <div class="custom-recipe-card">
                    <div class="custom-recipe-header">
                        <div>
                            <div class="custom-recipe-title">${escapeHtml(r.title)}</div>
                            <div class="custom-recipe-desc">${escapeHtml(r.description || 'Homemade recipe')}</div>
                            <div style="margin-top: 6px; display: flex; gap: 6px; flex-wrap: wrap;">
                                <span class="food-tag">${escapeHtml(r.cuisineRegion || 'Home Recipe')}</span>
                                <span class="food-tag">${escapeHtml(formatCookingMethod(r.cookingMethod))}</span>
                                <span class="food-tag" style="background:#EDE4D5;">Weight Change: ${yieldVal}x</span>
                            </div>
                        </div>
                        <div style="text-align: right;">
                            <span style="font-size: 11px; text-transform: uppercase; color: var(--text-muted); display: block;">Per Serving</span>
                            <strong style="font-size: 24px; font-family: var(--font-serif); color: var(--accent-primary);">${servingKcal} kcal</strong>
                            <small class="text-secondary" style="display:block;">${servingWeight}g serving</small>
                        </div>
                    </div>

                    <div class="custom-recipe-metrics">
                        <div>
                            <small>Full Dish</small>
                            <strong>${batchKcal} kcal</strong>
                            <small style="color: var(--text-muted);">${cookedWeight}g total</small>
                        </div>
                        <div>
                            <small>Servings</small>
                            <strong>${servingsCount}</strong>
                            <small style="color: var(--text-muted);">portions</small>
                        </div>
                        <div>
                            <small>Per Serving</small>
                            <strong>P:${servingP}g • C:${servingC}g</strong>
                            <small style="color: var(--text-muted);">Fat: ${servingF}g</small>
                        </div>
                        <div>
                            <small>Per 100g</small>
                            <strong>${densityKcal} kcal</strong>
                            <small style="color: var(--text-muted);">standard</small>
                        </div>
                    </div>

                    <div style="font-size: 12.5px; color: var(--text-secondary); margin-bottom: 6px;">
                        <strong>Ingredients (${(r.ingredients || []).length}):</strong> ${ingsSummary || 'None'}
                    </div>

                    <div class="custom-recipe-actions">
                        <button class="btn btn-secondary btn-sm" onclick="window.FitKitApp.editCustomRecipe(${r.id})">
                            Edit Recipe
                        </button>
                        <button class="btn btn-danger btn-sm" onclick="window.FitKitApp.deleteCustomRecipe(${r.id})">
                            Delete
                        </button>
                        <button class="btn btn-primary btn-sm" onclick="window.FitKitApp.logCustomRecipe(${r.id})">
                            + Log to Diary
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    }

    function openCreateRecipeModal() {
        if (!state.currentUser) {
            showToast('Please sign in to create custom recipes.');
            showAuthView();
            return;
        }

        DOM.modalCustomRecipeTitle.textContent = 'Create Custom Recipe';
        DOM.customRecipeEditId.value = '';
        DOM.customRecipeTitle.value = '';
        DOM.customRecipeDesc.value = '';
        DOM.customRecipeCuisine.value = 'Home Recipe';
        DOM.customRecipeMethod.value = 'PRESSURE_COOKING';
        DOM.customRecipeCookedWeight.value = '';
        DOM.customRecipeYieldFactor.value = '1.0';
        DOM.customRecipeServings.value = '2.0';

        DOM.customIngredientsTbody.innerHTML = '';
        // Add 2 starter ingredient rows
        addIngredientRow(null, 100, 1.0);
        addIngredientRow(null, 50, 1.0);

        resetPreviewBox();
        DOM.modalCustomRecipe.classList.add('active');
    }

    async function openEditRecipeModal(recipeId) {
        if (!state.currentUser) return;

        try {
            const res = await apiFetch(`/api/custom-recipes/${recipeId}`);
            if (!res.ok) throw new Error('Recipe not found');
            const r = await res.json();

            DOM.modalCustomRecipeTitle.textContent = 'Edit Custom Recipe';
            DOM.customRecipeEditId.value = r.id;
            DOM.customRecipeTitle.value = r.title || '';
            DOM.customRecipeDesc.value = r.description || '';
            DOM.customRecipeCuisine.value = r.cuisineRegion || '';
            DOM.customRecipeMethod.value = r.cookingMethod || 'PRESSURE_COOKING';
            DOM.customRecipeCookedWeight.value = r.cookedWeightG ? Math.round(Number(r.cookedWeightG)) : '';
            DOM.customRecipeYieldFactor.value = r.cookingYieldFactor ? Number(r.cookingYieldFactor).toFixed(2) : '1.0';
            DOM.customRecipeServings.value = r.servingsYield ? Number(r.servingsYield).toFixed(1) : '2.0';

            DOM.customIngredientsTbody.innerHTML = '';
            if (r.ingredients && r.ingredients.length > 0) {
                r.ingredients.forEach(ing => {
                    addIngredientRow(ing.foodId, Number(ing.rawQuantityG), Number(ing.retentionFactorOverride || 1.0));
                });
            } else {
                addIngredientRow(null, 100, 1.0);
            }

            DOM.modalCustomRecipe.classList.add('active');
            calculateCustomRecipePreview();
        } catch (err) {
            console.error(err);
            showToast('Unable to open recipe for editing.');
        }
    }

    function closeCustomRecipeModal() {
        DOM.modalCustomRecipe.classList.remove('active');
    }

    function addIngredientRow(foodId = null, rawQty = 100, retention = 1.0) {
        const tr = document.createElement('tr');
        const foodsOptions = state.foods.map(f =>
            `<option value="${f.id}" ${foodId === f.id ? 'selected' : ''}>${escapeHtml(f.name)} (${formatStateLabel(f.foodState)})</option>`
        ).join('');

        tr.innerHTML = `
            <td>
                <select class="form-select custom-ing-food" style="font-size: 12px; padding: 5px 8px; width: 100%;">
                    <option value="">-- Select ingredient --</option>
                    ${foodsOptions}
                </select>
            </td>
            <td>
                <div style="display: flex; align-items: center; gap: 4px;">
                    <input type="number" class="form-input custom-ing-qty" min="1" step="1" value="${rawQty}" style="font-size: 12px; padding: 5px 8px; width: 75px;">
                    <span style="font-size: 11px; color: var(--text-muted);">g</span>
                </div>
            </td>
            <td>
                <input type="number" class="form-input custom-ing-ret" min="0.1" max="1.0" step="0.05" value="${retention}" style="font-size: 12px; padding: 5px 8px; width: 65px;">
            </td>
            <td style="text-align: center;">
                <button type="button" class="btn btn-subtle btn-sm btn-remove-ing-row" style="color: var(--state-error); font-size: 15px; padding: 2px 6px;" title="Remove ingredient">&times;</button>
            </td>
        `;

        tr.querySelector('.btn-remove-ing-row').addEventListener('click', () => {
            tr.remove();
            calculateCustomRecipePreview();
        });

        tr.querySelector('.custom-ing-food').addEventListener('change', calculateCustomRecipePreview);
        tr.querySelector('.custom-ing-qty').addEventListener('input', calculateCustomRecipePreview);
        tr.querySelector('.custom-ing-ret').addEventListener('input', calculateCustomRecipePreview);

        DOM.customIngredientsTbody.appendChild(tr);
    }

    function gatherCustomRecipePayload() {
        const ingredients = [];
        const rows = DOM.customIngredientsTbody.querySelectorAll('tr');

        rows.forEach(tr => {
            const foodSelect = tr.querySelector('.custom-ing-food');
            const qtyInput = tr.querySelector('.custom-ing-qty');
            const retInput = tr.querySelector('.custom-ing-ret');

            const foodId = foodSelect && foodSelect.value ? parseInt(foodSelect.value, 10) : null;
            const rawG = qtyInput && qtyInput.value ? parseFloat(qtyInput.value) : null;
            const ret = retInput && retInput.value ? parseFloat(retInput.value) : 1.0;

            if (foodId && rawG && rawG > 0) {
                ingredients.push({
                    foodId: foodId,
                    rawQuantityG: rawG,
                    unitMeasure: 'g',
                    retentionFactorOverride: ret
                });
            }
        });

        const cookedW = DOM.customRecipeCookedWeight.value ? parseFloat(DOM.customRecipeCookedWeight.value) : null;
        const yieldF = DOM.customRecipeYieldFactor.value ? parseFloat(DOM.customRecipeYieldFactor.value) : 1.0;
        const servings = DOM.customRecipeServings.value ? parseFloat(DOM.customRecipeServings.value) : 1.0;

        return {
            title: DOM.customRecipeTitle.value.trim(),
            description: DOM.customRecipeDesc.value.trim(),
            cuisineRegion: DOM.customRecipeCuisine.value.trim(),
            cookingMethod: DOM.customRecipeMethod.value,
            cookedWeightG: cookedW,
            cookingYieldFactor: yieldF,
            servingsYield: servings,
            ingredients: ingredients
        };
    }

    async function calculateCustomRecipePreview() {
        const payload = gatherCustomRecipePayload();
        if (!payload.ingredients || payload.ingredients.length === 0) {
            resetPreviewBox();
            return;
        }

        try {
            const res = await apiFetch('/api/custom-recipes/preview', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const calc = await res.json();
                DOM.previewBatchKcal.textContent = `${Math.round(Number(calc.totalBatchCalories || 0))} kcal`;
                DOM.previewBatchMacros.textContent = `P: ${Number(calc.totalBatchProtein || 0).toFixed(1)}g | C: ${Number(calc.totalBatchCarbs || 0).toFixed(1)}g | F: ${Number(calc.totalBatchFat || 0).toFixed(1)}g`;
                DOM.previewBatchWeight.textContent = `Weight: ${Number(calc.cookedWeightG || 0).toFixed(0)}g`;

                DOM.previewPer100Kcal.textContent = `${Math.round(Number(calc.energyKcalPer100g || 0))} kcal`;
                DOM.previewPer100Macros.textContent = `P: ${Number(calc.proteinGPer100g || 0).toFixed(1)}g | C: ${Number(calc.availableCarbsGPer100g || 0).toFixed(1)}g | F: ${Number(calc.fatGPer100g || 0).toFixed(1)}g`;

                DOM.previewServingKcal.textContent = `${Math.round(Number(calc.energyKcalPerServing || 0))} kcal`;
                DOM.previewServingMacros.textContent = `P: ${Number(calc.proteinGPerServing || 0).toFixed(1)}g | C: ${Number(calc.availableCarbsGPerServing || 0).toFixed(1)}g | F: ${Number(calc.fatGPerServing || 0).toFixed(1)}g`;
                DOM.previewServingWeight.textContent = `Portion: ${Number(calc.servingWeightG || 0).toFixed(0)}g`;

                if (calc.hasIncompleteNutrients) {
                    DOM.previewIncompleteWarning.style.display = 'block';
                } else {
                    DOM.previewIncompleteWarning.style.display = 'none';
                }
            }
        } catch (err) {
            console.error('Preview error:', err);
        }
    }

    function resetPreviewBox() {
        DOM.previewBatchKcal.textContent = '-- kcal';
        DOM.previewBatchMacros.textContent = 'P: -- | C: -- | F: --';
        DOM.previewBatchWeight.textContent = 'Weight: --g';
        DOM.previewPer100Kcal.textContent = '-- kcal';
        DOM.previewPer100Macros.textContent = 'P: -- | C: -- | F: --';
        DOM.previewServingKcal.textContent = '-- kcal';
        DOM.previewServingMacros.textContent = 'P: -- | C: -- | F: --';
        DOM.previewServingWeight.textContent = 'Portion: --g';
        DOM.previewIncompleteWarning.style.display = 'none';
    }

    async function saveCustomRecipe() {
        const payload = gatherCustomRecipePayload();

        if (!payload.title) {
            showToast('Please enter a recipe title.');
            DOM.customRecipeTitle.focus();
            return;
        }

        if (!payload.ingredients || payload.ingredients.length === 0) {
            showToast('Please add at least one valid ingredient with quantity.');
            return;
        }

        const editId = DOM.customRecipeEditId.value;
        const isEdit = Boolean(editId);
        const endpoint = isEdit ? `/api/custom-recipes/${editId}` : '/api/custom-recipes';
        const method = isEdit ? 'PUT' : 'POST';

        try {
            DOM.btnSaveCustomRecipe.disabled = true;
            const res = await apiFetch(endpoint, {
                method: method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const saved = await res.json();
                showToast(`Recipe "${saved.title}" saved successfully!`);
                closeCustomRecipeModal();
                await loadCustomRecipes();
                switchRecipeMode('custom');
            } else {
                const errData = await res.json().catch(() => ({}));
                showToast(errData.error || 'Failed to save recipe.');
            }
        } catch (err) {
            console.error('Save recipe error:', err);
            showToast('An error occurred saving your recipe.');
        } finally {
            DOM.btnSaveCustomRecipe.disabled = false;
        }
    }

    async function deleteCustomRecipe(recipeId) {
        if (!confirm('Are you sure you want to delete this recipe? Previous diary entries will not be affected.')) {
            return;
        }

        try {
            const res = await apiFetch(`/api/custom-recipes/${recipeId}`, {
                method: 'DELETE'
            });

            if (res.ok) {
                showToast('Recipe deleted.');
                await loadCustomRecipes();
            } else {
                showToast('Unable to delete recipe.');
            }
        } catch (err) {
            console.error('Delete error:', err);
            showToast('Failed to delete recipe.');
        }
    }

    function logCustomRecipe(recipeId) {
        const r = state.customRecipes.find(item => item.id === recipeId);
        if (!r) return;

        const calc = r.calculationResult || {};
        const portionWeight = Math.round(Number(r.servingWeightG || calc.servingWeightG || 100));
        const servingKcal = Number(calc.energyKcalPerServing != null ? calc.energyKcalPerServing : (r.energyKcalPerServing || 0));
        const servingP = Number(calc.proteinGPerServing != null ? calc.proteinGPerServing : (r.proteinGPerServing || 0));
        const servingC = Number((calc.availableCarbsGPerServing || calc.carbohydratesGPerServing) != null ? (calc.availableCarbsGPerServing || calc.carbohydratesGPerServing) : (r.carbohydratesGPerServing || 0));
        const servingF = Number(calc.fatGPerServing != null ? calc.fatGPerServing : (r.fatGPerServing || 0));
        const servingFib = Number(calc.dietaryFiberGPerServing != null ? calc.dietaryFiberGPerServing : (r.dietaryFiberGPerServing || 0));

        const customFoodObj = {
            isRecipe: true,
            recipeId: r.id,
            id: r.id,
            name: r.title,
            foodState: 'PREPARED_RECIPE',
            portions: [
                {
                    portionDescription: `1 serving (${portionWeight}g)`,
                    gramWeight: portionWeight
                }
            ],
            energyKcal: (servingKcal / portionWeight) * 100,
            proteinG: (servingP / portionWeight) * 100,
            availableCarbohydratesG: (servingC / portionWeight) * 100,
            totalFatG: (servingF / portionWeight) * 100,
            dietaryFiberG: (servingFib / portionWeight) * 100
        };

        openLogModal('Lunch', customFoodObj);
    }

    // =========================================================================
    // 8. Modal: Add Food to Diary (Server Persistence)
    // =========================================================================

    function openLogModal(defaultMeal = 'Breakfast', preselectedFood = null) {
        if (!state.currentUser) {
            showToast('Please sign in to log meals.');
            showAuthView();
            return;
        }

        state.modalTargetMeal = defaultMeal;
        DOM.modalMealSelect.value = defaultMeal;
        DOM.modalFoodSearch.value = '';
        DOM.modalSearchResults.style.display = 'none';

        if (preselectedFood) {
            selectFoodForLogging(preselectedFood);
        } else {
            DOM.modalSelectedDetails.style.display = 'none';
            DOM.btnConfirmLog.disabled = true;
        }

        DOM.modalLogFood.classList.add('active');
        DOM.modalFoodSearch.focus();
    }

    function closeLogModal() {
        DOM.modalLogFood.classList.remove('active');
        state.modalSelectedFood = null;
    }

    async function selectFoodForLogging(food) {
        state.modalSelectedFood = food;
        DOM.modalSelName.textContent = food.name;
        DOM.modalSelState.textContent = formatStateLabel(food.foodState);
        DOM.modalSearchResults.style.display = 'none';
        DOM.modalSelectedDetails.style.display = 'block';
        DOM.btnConfirmLog.disabled = false;

        let portions = food.portions || [];
        if (!portions || portions.length === 0) {
            try {
                const res = await fetch(`/api/foods/${food.id}`);
                if (res.ok) {
                    const detail = await res.json();
                    portions = detail.portions || [];
                    state.modalSelectedFood.portions = portions;
                }
            } catch (e) {}
        }

        DOM.modalPortionSelect.innerHTML = '';
        if (portions && portions.length > 0) {
            portions.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.gramWeight;
                opt.textContent = `${p.portionDescription} (${p.gramWeight}g)`;
                DOM.modalPortionSelect.appendChild(opt);
            });
        }

        const opt100 = document.createElement('option');
        opt100.value = '100';
        opt100.textContent = 'Standard 100 grams';
        DOM.modalPortionSelect.appendChild(opt100);

        DOM.modalQtyInput.value = '1.0';
        updateLogModalPreview();
    }

    function updateLogModalPreview() {
        if (!state.modalSelectedFood) return;

        const portionWeight = parseFloat(DOM.modalPortionSelect.value) || 100;
        const qty = parseFloat(DOM.modalQtyInput.value) || 1.0;
        const totalGrams = portionWeight * qty;
        const ratio = totalGrams / 100;

        const f = state.modalSelectedFood;
        const kcal = Math.round((f.energyKcal || 0) * ratio);
        const p = ((f.proteinG || 0) * ratio).toFixed(1);
        const c = ((f.availableCarbohydratesG || 0) * ratio).toFixed(1);
        const fat = ((f.totalFatG || 0) * ratio).toFixed(1);
        const fib = ((f.dietaryFiberG || 0) * ratio).toFixed(1);

        DOM.modalPreviewKcal.textContent = `${kcal} kcal (${Math.round(totalGrams)}g)`;
        DOM.modalPreviewP.textContent = `${p}g`;
        DOM.modalPreviewC.textContent = `${c}g`;
        DOM.modalPreviewF.textContent = `${fat}g`;
        DOM.modalPreviewFib.textContent = `${fib}g`;
    }

    async function confirmAddFoodToDiary() {
        if (!state.modalSelectedFood || !state.currentUser) return;

        const meal = DOM.modalMealSelect.value;
        const portionWeight = parseFloat(DOM.modalPortionSelect.value) || 100;
        const portionText = DOM.modalPortionSelect.options[DOM.modalPortionSelect.selectedIndex].text;
        const qty = parseFloat(DOM.modalQtyInput.value) || 1.0;
        const dateStr = formatDateKey(state.currentDate);

        const payload = {
            mealType: meal,
            portionDescription: `${portionText}`,
            portionGramWeight: portionWeight,
            quantityMultiplier: qty,
            logDate: dateStr
        };

        if (state.modalSelectedFood.isRecipe) {
            payload.recipeId = state.modalSelectedFood.recipeId || state.modalSelectedFood.id;
        } else {
            payload.foodId = state.modalSelectedFood.id;
        }

        try {
            DOM.btnConfirmLog.disabled = true;
            const res = await apiFetch('/api/meals/entries', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                state.dailyLog = await res.json();
                renderDailyOverview();
                loadWeeklyHistory();
                closeLogModal();
                showToast(`Added to ${meal}.`);
            } else {
                throw new Error('Server returned ' + res.status);
            }
        } catch (err) {
            console.error('Failed to log food:', err);
            showToast('Unable to add food to diary.');
        } finally {
            DOM.btnConfirmLog.disabled = false;
        }
    }

    // =========================================================================
    // 9. Modal: Edit Entry (Portion & Quantity Multiplier)
    // =========================================================================

    function openEditEntryModal(entryId) {
        if (!state.dailyLog || !state.dailyLog.meals) return;

        let foundEntry = null;
        let foundMeal = null;

        for (const [mName, sec] of Object.entries(state.dailyLog.meals)) {
            const e = (sec.entries || []).find(it => it.id === entryId);
            if (e) {
                foundEntry = e;
                foundMeal = mName;
                break;
            }
        }

        if (!foundEntry) return;

        state.editingEntry = foundEntry;
        DOM.editEntryId.value = foundEntry.id;
        DOM.modalEditName.textContent = foundEntry.foodName;
        DOM.modalEditMeal.textContent = foundMeal;
        DOM.modalEditPortionDesc.value = foundEntry.portionDescription;
        DOM.modalEditQtyInput.value = foundEntry.quantityMultiplier || 1.0;

        updateEditModalPreview();
        DOM.modalEditEntry.classList.add('active');
    }

    function closeEditModal() {
        DOM.modalEditEntry.classList.remove('active');
        state.editingEntry = null;
    }

    function updateEditModalPreview() {
        if (!state.editingEntry) return;
        const e = state.editingEntry;
        const currentQty = parseFloat(DOM.modalEditQtyInput.value) || 1.0;
        const prevQty = parseFloat(e.quantityMultiplier) || 1.0;
        const scale = currentQty / prevQty;

        const kcal = Math.round(Number(e.energyKcal || 0) * scale);
        const p = (Number(e.proteinG || 0) * scale).toFixed(1);
        const c = (Number(e.carbohydratesG || 0) * scale).toFixed(1);
        const f = (Number(e.fatG || 0) * scale).toFixed(1);
        const fib = (Number(e.fiberG || 0) * scale).toFixed(1);

        DOM.modalEditPreviewKcal.textContent = `${kcal} kcal`;
        DOM.modalEditPreviewP.textContent = `${p}g`;
        DOM.modalEditPreviewC.textContent = `${c}g`;
        DOM.modalEditPreviewF.textContent = `${f}g`;
        DOM.modalEditPreviewFib.textContent = `${fib}g`;
    }

    async function confirmEditEntry() {
        if (!state.editingEntry || !state.currentUser) return;

        const entryId = state.editingEntry.id;
        const newQty = parseFloat(DOM.modalEditQtyInput.value) || 1.0;
        const newPortionDesc = DOM.modalEditPortionDesc.value.trim() || state.editingEntry.portionDescription;

        const payload = {
            portionDescription: newPortionDesc,
            portionGramWeight: state.editingEntry.portionGramWeight,
            quantityMultiplier: newQty
        };

        try {
            DOM.btnConfirmEdit.disabled = true;
            const res = await apiFetch(`/api/meals/entries/${entryId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                state.dailyLog = await res.json();
                renderDailyOverview();
                loadWeeklyHistory();
                closeEditModal();
                showToast('Entry updated.');
            } else {
                throw new Error('Server returned ' + res.status);
            }
        } catch (err) {
            console.error('Failed to update entry:', err);
            showToast('Unable to update entry.');
        } finally {
            DOM.btnConfirmEdit.disabled = false;
        }
    }

    // =========================================================================
    // 10. Delete Diary Entry (Server Persistence)
    // =========================================================================

    async function deleteDiaryEntry(entryId) {
        if (!state.currentUser) return;

        try {
            const res = await apiFetch(`/api/meals/entries/${entryId}`, {
                method: 'DELETE'
            });

            if (res.ok) {
                state.dailyLog = await res.json();
                renderDailyOverview();
                loadWeeklyHistory();
                showToast('Entry removed.');
            } else {
                throw new Error('Server returned ' + res.status);
            }
        } catch (err) {
            console.error('Failed to delete entry:', err);
            showToast('Unable to remove entry.');
        }
    }

    // =========================================================================
    // 11. Modal: Food Information & Citation
    // =========================================================================

    async function openFoodInfo(id) {
        try {
            const res = await fetch(`/api/foods/${id}`);
            if (!res.ok) throw new Error('Failed to fetch food details');
            const food = await res.json();

            DOM.infoModalCode.textContent = food.foodCode || `ID-${food.id}`;
            DOM.infoModalTitle.textContent = food.name;
            DOM.infoModalState.textContent = formatStateLabel(food.foodState);
            DOM.infoModalGroup.textContent = food.foodGroup || 'General';
            DOM.infoModalQuality.textContent = food.qualityStatus || 'SOURCE_VERIFIED';

            DOM.infoModalKcal.textContent = food.energyKcal != null ? Number(food.energyKcal).toFixed(0) : '--';
            DOM.infoModalKj.textContent = food.energyKj != null ? Number(food.energyKj).toFixed(0) : '--';
            DOM.infoModalP.textContent = food.proteinG != null ? Number(food.proteinG).toFixed(1) : '--';
            DOM.infoModalC.textContent = food.availableCarbohydratesG != null ? Number(food.availableCarbohydratesG).toFixed(1) : '--';
            DOM.infoModalF.textContent = food.totalFatG != null ? Number(food.totalFatG).toFixed(1) : '--';
            DOM.infoModalFib.textContent = food.dietaryFiberG != null ? Number(food.dietaryFiberG).toFixed(1) : '--';

            const nut = food.allNutrients && food.allNutrients.length > 0 ? food.allNutrients[0] : null;
            DOM.infoModalWater.textContent = nut && nut.moistureWaterG != null ? Number(nut.moistureWaterG).toFixed(1) : '--';
            DOM.infoModalAsh.textContent = nut && nut.ashG != null ? Number(nut.ashG).toFixed(1) : '--';

            if (food.portions && food.portions.length > 0) {
                DOM.infoModalPortions.innerHTML = food.portions.map(p => {
                    const cals = food.energyKcal ? Math.round((Number(p.gramWeight) / 100) * Number(food.energyKcal)) : '--';
                    return `
                        <div style="display:flex; justify-content:space-between; padding: 6px 0; border-bottom: 1px solid var(--border-subtle);">
                            <span>${escapeHtml(p.portionDescription)} (${p.gramWeight}g)</span>
                            <strong>${cals} kcal</strong>
                        </div>
                    `;
                }).join('');
            } else {
                DOM.infoModalPortions.innerHTML = '<span class="text-secondary">Standard 100g measures only.</span>';
            }

            DOM.infoModalCitation.textContent = `${food.sourceName || 'Catalog Reference'} • ${food.sourceCitation || 'Documented standard.'} (License: ${food.sourceLicense || 'Standard'})`;

            DOM.btnLogFromInfo.onclick = () => {
                DOM.modalFoodInfo.classList.remove('active');
                openLogModal('Breakfast', food);
            };

            DOM.modalFoodInfo.classList.add('active');
        } catch (err) {
            console.error(err);
            showToast('Unable to load food details.');
        }
    }

    // Helper: Format Food State
    function formatStateLabel(state) {
        if (!state) return 'GENERAL';
        switch (state) {
            case 'RAW_INGREDIENT': return 'Raw Ingredient';
            case 'COOKED_SINGLE': return 'Cooked Dish';
            case 'PREPARED_RECIPE': return 'Composite Recipe';
            case 'PACKAGED_COMMERCIAL': return 'Packaged Product';
            default: return state.replace(/_/g, ' ');
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    // =========================================================================
    // 12. Navigation & Event Listeners
    // =========================================================================

    function initNavigation() {
        DOM.navItems.forEach(item => {
            item.addEventListener('click', e => {
                e.preventDefault();
                const viewName = item.getAttribute('data-view');
                switchView(viewName);
            });
        });

        if (DOM.mobileNavToggle) {
            DOM.mobileNavToggle.addEventListener('click', () => {
                DOM.sidebar.style.display = DOM.sidebar.style.display === 'flex' ? 'none' : 'flex';
            });
        }
    }

    function switchView(viewName) {
        state.activeView = viewName;

        DOM.navItems.forEach(item => {
            item.classList.toggle('active', item.getAttribute('data-view') === viewName);
        });

        DOM.views.forEach(section => {
            section.classList.toggle('active', section.id === `view-${viewName}`);
        });

        if (window.innerWidth <= 768 && DOM.sidebar) {
            DOM.sidebar.style.display = 'none';
        }

        window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    function initDateControls() {
        DOM.btnPrevDay.addEventListener('click', () => {
            state.currentDate.setDate(state.currentDate.getDate() - 1);
            loadDailyLog(formatDateKey(state.currentDate));
        });

        DOM.btnNextDay.addEventListener('click', () => {
            state.currentDate.setDate(state.currentDate.getDate() + 1);
            loadDailyLog(formatDateKey(state.currentDate));
        });

        DOM.btnJumpToday.addEventListener('click', () => {
            state.currentDate = new Date();
            loadDailyLog(formatDateKey(state.currentDate));
        });
    }

    function setupEventListeners() {
        initNavigation();
        initDateControls();

        // Auth Tabs
        if (DOM.tabBtnLogin) {
            DOM.tabBtnLogin.addEventListener('click', () => {
                DOM.tabBtnLogin.classList.add('active');
                DOM.tabBtnRegister.classList.remove('active');
                DOM.formLogin.style.display = 'block';
                DOM.formRegister.style.display = 'none';
                hideAuthAlert();
            });
        }

        if (DOM.tabBtnRegister) {
            DOM.tabBtnRegister.addEventListener('click', () => {
                DOM.tabBtnRegister.classList.add('active');
                DOM.tabBtnLogin.classList.remove('active');
                DOM.formRegister.style.display = 'block';
                DOM.formLogin.style.display = 'none';
                hideAuthAlert();
            });
        }

        // Demo login filler
        if (DOM.btnDemoFill) {
            DOM.btnDemoFill.addEventListener('click', () => {
                DOM.loginEmail.value = 'demo@fitkit.local';
                DOM.loginPassword.value = 'FitKit2026!';
            });
        }

        // Auth Form Submissions
        if (DOM.formLogin) DOM.formLogin.addEventListener('submit', handleLogin);
        if (DOM.formRegister) DOM.formRegister.addEventListener('submit', handleRegister);
        if (DOM.nutritionOnboardingForm) DOM.nutritionOnboardingForm.addEventListener('submit', handleNutritionOnboarding);
        if (DOM.btnOnboardingLogout) DOM.btnOnboardingLogout.addEventListener('click', handleLogout);
        if (DOM.btnSidebarLogout) DOM.btnSidebarLogout.addEventListener('click', handleLogout);
        if (DOM.btnTopbarLogout) DOM.btnTopbarLogout.addEventListener('click', handleLogout);

        // Add food modal
        DOM.btnOpenLogModal.addEventListener('click', () => openLogModal());
        DOM.btnCloseLogModal.addEventListener('click', closeLogModal);
        DOM.btnCancelLog.addEventListener('click', closeLogModal);
        DOM.modalLogFood.addEventListener('click', e => {
            if (e.target === DOM.modalLogFood) closeLogModal();
        });

        // Edit entry modal
        DOM.btnCloseEditModal.addEventListener('click', closeEditModal);
        DOM.btnCancelEdit.addEventListener('click', closeEditModal);
        DOM.modalEditEntry.addEventListener('click', e => {
            if (e.target === DOM.modalEditEntry) closeEditModal();
        });
        DOM.modalEditQtyInput.addEventListener('input', updateEditModalPreview);
        DOM.btnConfirmEdit.addEventListener('click', confirmEditEntry);

        // Add food search
        let modalSearchTimer;
        DOM.modalFoodSearch.addEventListener('input', e => {
            clearTimeout(modalSearchTimer);
            modalSearchTimer = setTimeout(async () => {
                const q = e.target.value.trim();
                if (!q) {
                    DOM.modalSearchResults.style.display = 'none';
                    return;
                }

                let foodMatches = [];
                try {
                    const res = await fetch(`/api/foods?query=${encodeURIComponent(q)}&sort=RELEVANCE`);
                    if (res.ok) {
                        const allFoods = await res.json();
                        foodMatches = allFoods.slice(0, 6);
                    }
                } catch (err) {
                    console.error('Modal search query error:', err);
                }

                const qLower = q.toLowerCase();
                const recipeMatches = (state.customRecipes || []).filter(r =>
                    (r.title && r.title.toLowerCase().includes(qLower)) ||
                    (r.recipeCode && r.recipeCode.toLowerCase().includes(qLower))
                ).slice(0, 4);

                let html = '';
                if (recipeMatches.length > 0) {
                    html += recipeMatches.map(r => `
                        <div class="food-row" style="cursor: pointer; padding: 8px 12px; background: #FAF7F0;" onclick="window.FitKitApp.pickModalRecipe(${r.id})">
                            <div>
                                <strong style="font-size: 13.5px; color: var(--accent-primary);">${escapeHtml(r.title)}</strong>
                                <small class="text-secondary" style="display:block;">Custom Recipe • ${escapeHtml(r.cuisineRegion || 'Home')}</small>
                            </div>
                            <strong style="color: var(--accent-primary); font-size: 13px;">${Math.round(r.energyKcalPerServing || 0)} kcal/srv</strong>
                        </div>
                    `).join('');
                }

                if (foodMatches.length > 0) {
                    html += foodMatches.map(f => `
                        <div class="food-row" style="cursor: pointer; padding: 8px 12px;" onclick="window.FitKitApp.pickModalFood(${f.id})">
                            <div>
                                <strong style="font-size: 13.5px; color: var(--text-primary);">${escapeHtml(f.name)}</strong>
                                ${f.matchedAlias ? `<span class="alias-tag" style="margin-bottom: 2px;">Matched: ${escapeHtml(f.matchedAlias)}</span>` : ''}
                                <small class="text-secondary" style="display:block;">${formatStateLabel(f.foodState)} • ${f.foodGroup || 'General'}</small>
                            </div>
                            <strong style="color: var(--accent-primary); font-size: 13px;">${Math.round(f.energyKcal || 0)} kcal</strong>
                        </div>
                    `).join('');
                }

                if (html) {
                    DOM.modalSearchResults.innerHTML = html;
                    DOM.modalSearchResults.style.display = 'block';
                } else {
                    DOM.modalSearchResults.innerHTML = `<div style="padding: 12px; color: var(--text-muted); font-size: 13px;">No food or recipe found matching "${escapeHtml(q)}"</div>`;
                    DOM.modalSearchResults.style.display = 'block';
                }
            }, 180);
        });

        DOM.modalPortionSelect.addEventListener('change', updateLogModalPreview);
        DOM.modalQtyInput.addEventListener('input', updateLogModalPreview);
        DOM.btnConfirmLog.addEventListener('click', confirmAddFoodToDiary);

        // Custom Recipe controls & modal
        if (DOM.tabBtnCurated) DOM.tabBtnCurated.addEventListener('click', () => switchRecipeMode('curated'));
        if (DOM.tabBtnCustom) DOM.tabBtnCustom.addEventListener('click', () => switchRecipeMode('custom'));
        if (DOM.btnOpenCreateRecipe) DOM.btnOpenCreateRecipe.addEventListener('click', openCreateRecipeModal);
        if (DOM.btnCloseCustomRecipe) DOM.btnCloseCustomRecipe.addEventListener('click', closeCustomRecipeModal);
        if (DOM.btnCancelCustomRecipe) DOM.btnCancelCustomRecipe.addEventListener('click', closeCustomRecipeModal);
        if (DOM.modalCustomRecipe) {
            DOM.modalCustomRecipe.addEventListener('click', e => {
                if (e.target === DOM.modalCustomRecipe) closeCustomRecipeModal();
            });
        }
        if (DOM.btnAddIngredientRow) DOM.btnAddIngredientRow.addEventListener('click', () => addIngredientRow(null, 100, 1.0));
        if (DOM.btnCalcCustomPreview) DOM.btnCalcCustomPreview.addEventListener('click', calculateCustomRecipePreview);
        if (DOM.btnSaveCustomRecipe) DOM.btnSaveCustomRecipe.addEventListener('click', saveCustomRecipe);
        if (DOM.customRecipeCookedWeight) DOM.customRecipeCookedWeight.addEventListener('input', calculateCustomRecipePreview);
        if (DOM.customRecipeYieldFactor) DOM.customRecipeYieldFactor.addEventListener('input', calculateCustomRecipePreview);
        if (DOM.customRecipeServings) DOM.customRecipeServings.addEventListener('input', calculateCustomRecipePreview);

        // Info modal
        DOM.btnCloseInfoModal.addEventListener('click', () => DOM.modalFoodInfo.classList.remove('active'));
        DOM.btnDismissInfo.addEventListener('click', () => DOM.modalFoodInfo.classList.remove('active'));
        DOM.modalFoodInfo.addEventListener('click', e => {
            if (e.target === DOM.modalFoodInfo) DOM.modalFoodInfo.classList.remove('active');
        });

        // Quick add from meal card headers
        document.querySelectorAll('.btn-quick-add').forEach(btn => {
            btn.addEventListener('click', () => {
                const meal = btn.getAttribute('data-meal');
                openLogModal(meal);
            });
        });

        // Library search & filters
        let libSearchTimer;
        DOM.libSearchInput.addEventListener('input', e => {
            clearTimeout(libSearchTimer);
            libSearchTimer = setTimeout(() => {
                state.libSearch = e.target.value.trim();
                applyLibraryFilters();
            }, 180);
        });

        DOM.libFilterBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                DOM.libFilterBtns.forEach(b => b.classList.remove('active'));
                btn.classList.add('active');
                state.libStateFilter = btn.getAttribute('data-state');
                applyLibraryFilters();
            });
        });

        DOM.libGroupSelect.addEventListener('change', e => {
            state.libGroupFilter = e.target.value;
            applyLibraryFilters();
        });

        if (DOM.libQualitySelect) {
            DOM.libQualitySelect.addEventListener('change', e => {
                state.libQualityFilter = e.target.value;
                applyLibraryFilters();
            });
        }

        if (DOM.libSortSelect) {
            DOM.libSortSelect.addEventListener('change', e => {
                state.libSort = e.target.value;
                applyLibraryFilters();
            });
        }

        if (DOM.btnClearLibFilters) {
            DOM.btnClearLibFilters.addEventListener('click', () => {
                state.libSearch = '';
                state.libStateFilter = 'ALL';
                state.libGroupFilter = 'ALL';
                state.libQualityFilter = 'ALL';
                state.libSort = 'RELEVANCE';

                DOM.libSearchInput.value = '';
                DOM.libFilterBtns.forEach(b => {
                    if (b.getAttribute('data-state') === 'ALL') b.classList.add('active');
                    else b.classList.remove('active');
                });
                DOM.libGroupSelect.value = 'ALL';
                if (DOM.libQualitySelect) DOM.libQualitySelect.value = 'ALL';
                if (DOM.libSortSelect) DOM.libSortSelect.value = 'RELEVANCE';

                applyLibraryFilters();
            });
        }

        // Recipe Lab sliders
        DOM.labSliderYield.addEventListener('input', e => {
            DOM.labYieldVal.textContent = Number(e.target.value).toFixed(2) + 'x';
            recalculateLabRecipe();
        });

        DOM.labSliderServings.addEventListener('input', e => {
            DOM.labServingsVal.textContent = e.target.value + ' servings';
            recalculateLabRecipe();
        });

        // Settings Form submission
        DOM.targetsForm.addEventListener('submit', e => {
            e.preventDefault();
            const newTargets = {
                dailyCalories: parseInt(DOM.settingCalTarget.value, 10),
                proteinG: parseFloat(DOM.settingProteinTarget.value),
                carbsG: parseFloat(DOM.settingCarbsTarget.value),
                fatG: parseFloat(DOM.settingFatTarget.value),
                fiberG: parseFloat(DOM.settingFiberTarget.value)
            };
            saveTargets(newTargets);
        });

        DOM.btnResetTargets.addEventListener('click', () => {
            apiFetch('/api/nutrition-profile').then(res => res.json()).then(profile => {
                return saveTargets({
                    dailyCalories: profile.dailyCalories,
                    proteinG: profile.proteinG,
                    carbsG: profile.carbsG,
                    fatG: profile.fatG,
                    fiberG: profile.fiberG
                });
            }).catch(err => {
                console.error('Unable to restore calculated targets:', err);
                showToast('Unable to restore your calculated targets.');
            });
        });
    }

    // Public window helper for onclick bindings
    window.FitKitApp = {
        openLogModal: (meal) => openLogModal(meal || 'Breakfast'),
        openLogModalForMeal: (meal) => openLogModal(meal),
        openLogModalWithFood: (foodId) => {
            const food = state.foods.find(f => f.id === foodId);
            if (food) openLogModal('Breakfast', food);
        },
        openFoodInfo: (foodId) => openFoodInfo(foodId),
        pickModalFood: (foodId) => {
            const food = state.foods.find(f => f.id === foodId);
            if (food) selectFoodForLogging(food);
        },
        pickModalRecipe: (recipeId) => {
            const r = (state.customRecipes || []).find(item => item.id === recipeId);
            if (r) {
                const calc = r.calculationResult || {};
                const portionWeight = Math.round(Number(r.servingWeightG || calc.servingWeightG || 100));
                const servingKcal = Number(calc.energyKcalPerServing != null ? calc.energyKcalPerServing : (r.energyKcalPerServing || 0));
                const servingP = Number(calc.proteinGPerServing != null ? calc.proteinGPerServing : (r.proteinGPerServing || 0));
                const servingC = Number((calc.availableCarbsGPerServing || calc.carbohydratesGPerServing) != null ? (calc.availableCarbsGPerServing || calc.carbohydratesGPerServing) : (r.carbohydratesGPerServing || 0));
                const servingF = Number(calc.fatGPerServing != null ? calc.fatGPerServing : (r.fatGPerServing || 0));
                const servingFib = Number(calc.dietaryFiberGPerServing != null ? calc.dietaryFiberGPerServing : (r.dietaryFiberGPerServing || 0));

                selectFoodForLogging({
                    isRecipe: true,
                    recipeId: r.id,
                    id: r.id,
                    name: r.title,
                    foodState: 'PREPARED_RECIPE',
                    portions: [
                        { portionDescription: `1 serving (${portionWeight}g)`, gramWeight: portionWeight }
                    ],
                    energyKcal: (servingKcal / portionWeight) * 100,
                    proteinG: (servingP / portionWeight) * 100,
                    availableCarbohydratesG: (servingC / portionWeight) * 100,
                    totalFatG: (servingF / portionWeight) * 100,
                    dietaryFiberG: (servingFib / portionWeight) * 100
                });
            }
        },
        openCreateRecipeModal: () => openCreateRecipeModal(),
        editCustomRecipe: (id) => openEditRecipeModal(id),
        deleteCustomRecipe: (id) => deleteCustomRecipe(id),
        logCustomRecipe: (id) => logCustomRecipe(id),
        openEditEntryModal: (entryId) => openEditEntryModal(entryId),
        deleteDiaryEntry: (entryId) => deleteDiaryEntry(entryId)
    };

    // App Initialization
    async function init() {
        setupEventListeners();

        // 1. Preload public catalogs (independent of auth state)
        await Promise.all([
            loadFoods(),
            loadRecipes()
        ]);

        // 2. Check user session
        const isAuthenticated = await checkAuthSession();

        // Authenticated users are routed to onboarding or the journal by checkAuthSession.
    }

    document.addEventListener('DOMContentLoaded', init);
})();
