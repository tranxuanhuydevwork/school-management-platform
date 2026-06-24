/* theme.js — Dark/Light theme toggle + persistence */
(function () {
    'use strict';

    const STORAGE_KEY = 'f3school-theme';
    const DARK = 'dark', LIGHT = 'light';

    function getTheme() {
        return localStorage.getItem(STORAGE_KEY) || DARK;
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        const btn = document.getElementById('themeToggle');
        if (!btn) return;
        const icon = btn.querySelector('i');
        if (icon) {
            icon.className = theme === DARK ? 'bi bi-sun' : 'bi bi-moon-stars';
        }
        btn.setAttribute('title', theme === DARK ? 'Chuyển sang Light' : 'Chuyển sang Dark');
    }

    function toggle() {
        const current = getTheme();
        const next = current === DARK ? LIGHT : DARK;
        localStorage.setItem(STORAGE_KEY, next);
        applyTheme(next);
    }

    // Apply on DOM ready
    document.addEventListener('DOMContentLoaded', function () {
        applyTheme(getTheme());
        const btn = document.getElementById('themeToggle');
        if (btn) btn.addEventListener('click', toggle);
    });
})();