/* ═══════════════════════════════════════════
   theme.js — MyF3School Admin
   Dark / Light mode toggle + live clock + modal + toast + confirm
═══════════════════════════════════════════ */
(function () {
    'use strict';

    // ── Apply saved theme immediately (before paint) ──────────────
    const saved = localStorage.getItem('f3_theme') || 'dark';
    document.documentElement.setAttribute('data-theme', saved);

    document.addEventListener('DOMContentLoaded', function () {

        // ── Theme toggle ────────────────────────────────────────────
        const btn = document.getElementById('themeToggle');
        if (btn) {
            const icon = () => document.documentElement.getAttribute('data-theme') === 'dark'
                ? '<i class="bi bi-moon-stars"></i>'
                : '<i class="bi bi-sun"></i>';
            btn.innerHTML = icon();
            btn.addEventListener('click', function () {
                const next = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
                document.documentElement.setAttribute('data-theme', next);
                localStorage.setItem('f3_theme', next);
                btn.innerHTML = icon();
            });
        }

        // ── Live clock ───────────────────────────────────────────────
        function updateClock() {
            const el = document.getElementById('liveClock');
            if (el) el.textContent = new Date().toLocaleTimeString('vi-VN', {
                hour: '2-digit', minute: '2-digit', second: '2-digit'
            });
        }
        updateClock();
        setInterval(updateClock, 1000);

    });

    // ── Modal helpers (global) ────────────────────────────────────
    window.F3Modal = {
        open(id) {
            const m = document.getElementById(id);
            if (m) { m.classList.add('open'); document.body.style.overflow = 'hidden'; }
        },
        close(id) {
            const m = document.getElementById(id);
            if (m) { m.classList.remove('open'); document.body.style.overflow = ''; }
        }
    };

    document.addEventListener('click', function (e) {
        const open = e.target.closest('[data-modal-open]');
        if (open) F3Modal.open(open.dataset.modalOpen);
        const close = e.target.closest('[data-modal-close]');
        if (close) F3Modal.close(close.dataset.modalClose);
        if (e.target.classList.contains('modal-overlay')) F3Modal.close(e.target.id);
    });

    // ── Toast (global) ────────────────────────────────────────────
    window.F3Toast = {
        _show(type, title, desc, icon) {
            let tc = document.getElementById('toastContainer');
            if (!tc) { tc = document.createElement('div'); tc.id = 'toastContainer'; tc.className = 'toast-container'; document.body.appendChild(tc); }
            const el = document.createElement('div');
            el.className = `toast ${type}`;
            el.innerHTML = `<i class="bi ${icon} toast-icon"></i>
        <div class="toast-body">
          <div class="toast-title">${_esc(title)}</div>
          ${desc ? `<div class="toast-desc">${_esc(desc)}</div>` : ''}
        </div>`;
            tc.appendChild(el);
            setTimeout(() => {
                el.style.opacity = '0';
                el.style.transform = 'translateX(20px)';
                el.style.transition = 'all .3s';
                setTimeout(() => el.remove(), 300);
            }, 3500);
        },
        success: (t, d) => F3Toast._show('success', t, d, 'bi-check-circle-fill'),
        danger:  (t, d) => F3Toast._show('danger',  t, d, 'bi-exclamation-circle-fill'),
        warn:    (t, d) => F3Toast._show('warn',     t, d, 'bi-exclamation-triangle-fill'),
        info:    (t, d) => F3Toast._show('info',     t, d, 'bi-info-circle-fill'),
    };

    // ── Confirm dialog (global) ──────────────────────────────────
    window.F3Confirm = function (msg, cb, opts) {
        opts = opts || {};
        let dlg = document.getElementById('confirmDialog');
        if (!dlg) {
            dlg = document.createElement('div');
            dlg.id = 'confirmDialog';
            dlg.className = 'confirm-dialog';
            dlg.innerHTML = `<div class="confirm-box">
        <h3 id="_confirmTitle">Xác nhận</h3>
        <p id="_confirmMsg"></p>
        <div class="confirm-actions">
          <button class="btn btn-secondary" id="_confirmCancel">Hủy</button>
          <button class="btn btn-danger"    id="_confirmOk">Xác nhận</button>
        </div>
      </div>`;
            document.body.appendChild(dlg);
            document.getElementById('_confirmCancel').addEventListener('click', () => dlg.classList.remove('open'));
        }
        document.getElementById('_confirmMsg').textContent   = msg;
        document.getElementById('_confirmTitle').textContent = opts.title || 'Xác nhận';
        const okBtn = document.getElementById('_confirmOk');
        okBtn.textContent = opts.okLabel || 'Xác nhận';
        okBtn.className   = `btn ${opts.okClass || 'btn-danger'}`;
        const handler = function () { dlg.classList.remove('open'); okBtn.removeEventListener('click', handler); if (cb) cb(); };
        okBtn.addEventListener('click', handler);
        dlg.classList.add('open');
    };

    // ── Loading state helper (global) ────────────────────────────
    window.F3Loading = function (btn, state) {
        if (!btn) return;
        if (state) { btn._f3txt = btn.innerHTML; btn.innerHTML = '<span class="spinner"></span>'; btn.disabled = true; }
        else        { btn.innerHTML = btn._f3txt || btn.innerHTML; btn.disabled = false; }
    };

    // ── HTML escape helper (global) ──────────────────────────────
    function _esc(s) {
        return String(s || '').replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }
    window.escHtml = _esc;

    // ── Date/time formatters (global) ────────────────────────────
    window.fmtDate = function (d) {
        if (!d) return '—';
        return new Date(d).toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
    };
    window.fmtDatetime = function (d) {
        if (!d) return '—';
        return new Date(d).toLocaleString('vi-VN');
    };
    window.relTime = function (d) {
        if (!d) return '';
        const s = Math.floor((Date.now() - new Date(d)) / 1000);
        if (s < 60)    return 'Vừa xong';
        if (s < 3600)  return Math.floor(s / 60) + ' phút trước';
        if (s < 86400) return Math.floor(s / 3600) + ' giờ trước';
        return Math.floor(s / 86400) + ' ngày trước';
    };

})();