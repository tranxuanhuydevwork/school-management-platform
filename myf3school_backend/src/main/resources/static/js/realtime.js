/* realtime.js — Real-time notification & live update engine
   Tries SSE first, falls back to polling every 30s
   Dispatches custom events: f3:notification, f3:badge-update
*/
(function () {
    'use strict';

    const BASE_URL  = window.F3_API_BASE || '/api';
    const POLL_MS   = 30_000;   // fallback polling interval
    const userId    = () => window.F3_USER_ID || null;

    let pollTimer   = null;
    let sseSource   = null;
    let isConnected = false;

    /* ── Helpers ────────────────────────────────── */
    function setIndicator(online) {
        isConnected = online;
        const el = document.getElementById('rtIndicator');
        if (!el) return;
        el.classList.toggle('offline', !online);
        const label = el.querySelector('.rt-label');
        if (label) label.textContent = online ? 'Live' : 'Offline';
        el.title = online ? 'Kết nối thời gian thực' : 'Đang thử kết nối lại…';
    }

    function dispatchEvent(name, detail) {
        window.dispatchEvent(new CustomEvent(name, { detail }));
    }

    /* ── Notification list management ──────────── */
    const notifState = {
        items: [],
        unread: 0,
    };

    function renderNotifDropdown() {
        const list  = document.getElementById('notifList');
        const count = document.getElementById('topbarNotifCount');
        const badge = document.getElementById('sidebarNotifBadge');
        if (!list) return;

        if (notifState.items.length === 0) {
            list.innerHTML = '<div class="notif-empty">Không có thông báo mới</div>';
        } else {
            list.innerHTML = notifState.items.slice(0, 15).map(n => {
                const icon = refTypeIcon(n.refType);
                const iconCls = refTypeColor(n.refType);
                return `
          <div class="notif-item ${n.isRead ? '' : 'unread'}"
               data-id="${n.id}" onclick="F3RT.markRead(${n.id})">
            <div class="notif-icon ${iconCls}"><i class="bi ${icon}"></i></div>
            <div class="notif-body">
              <div class="notif-title">${escHtml(n.title)}</div>
              <div class="notif-message">${escHtml(n.message)}</div>
              <div class="notif-time">${relTime(n.createdAt)}</div>
            </div>
          </div>`;
            }).join('');
        }

        const unreadCount = notifState.items.filter(n => !n.isRead).length;
        notifState.unread = unreadCount;

        if (count) {
            count.textContent = unreadCount;
            count.style.display = unreadCount > 0 ? 'flex' : 'none';
        }
        if (badge) {
            badge.textContent = unreadCount;
            badge.style.display = unreadCount > 0 ? '' : 'none';
        }

        dispatchEvent('f3:badge-update', { unread: unreadCount });
    }

    function refTypeIcon(t) {
        const map = {
            SCHEDULE: 'bi-calendar-event',
            SCORE: 'bi-bar-chart',
            ATTENDANCE: 'bi-person-check',
            ENROLLMENT: 'bi-mortarboard',
            APPLICATION: 'bi-file-earmark-text',
        };
        return map[t] || 'bi-bell';
    }
    function refTypeColor(t) {
        const map = {
            SCORE: 'bg-success-soft',
            ATTENDANCE: 'bg-warn-soft',
            APPLICATION: 'bg-accent-soft',
        };
        return map[t] || '';
    }

    function escHtml(s) {
        return String(s || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function relTime(dateStr) {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        const diff = Math.floor((Date.now() - d) / 1000);
        if (diff < 60) return 'Vừa xong';
        if (diff < 3600) return Math.floor(diff / 60) + ' phút trước';
        if (diff < 86400) return Math.floor(diff / 3600) + ' giờ trước';
        return Math.floor(diff / 86400) + ' ngày trước';
    }

    /* ── Fetch notifications ────────────────────── */
    async function fetchNotifications() {
        const uid = userId();
        if (!uid) return;

        try {
            const res  = await fetch(`${BASE_URL}/notifications/users/${uid}?page=0&size=20`);
            if (!res.ok) throw new Error('HTTP ' + res.status);

            const body = await res.json();

            // 🔥 FIX
            const pageData = body?.data ?? body;
            notifState.items = pageData?.content ?? [];

            renderNotifDropdown();
            dispatchEvent('f3:notification', notifState.items);

            setIndicator(true);
        } catch (e) {
            console.warn('[F3RT] fetchNotifications failed:', e.message);
            setIndicator(false);
        }
    }
    /* ── Unread count ───────────────────────────── */
    async function fetchUnreadCount() {
        const uid = userId();
        if (!uid) return;
        try {
            const res  = await fetch(`${BASE_URL}/notifications/users/${uid}/unread-count`);
            if (!res.ok) return;
            const body = await res.json();
            const count = body?.data?.count ?? 0;
            const el = document.getElementById('topbarNotifCount');
            const badge = document.getElementById('sidebarNotifBadge');
            if (el) { el.textContent = count; el.style.display = count > 0 ? 'flex' : 'none'; }
            if (badge) { badge.textContent = count; badge.style.display = count > 0 ? '' : 'none'; }
        } catch (_) {}
    }

    /* ── Mark single read ───────────────────────── */
    async function markRead(id) {
        try {
            const res = await fetch(`${BASE_URL}/notifications/${id}/read`, { method: 'PUT' });
            if (!res.ok) throw new Error();

            const item = notifState.items.find(n => n.id === id);
            if (item) {
                item.isRead = true;
                renderNotifDropdown();
            }
        } catch (_) {}
    }
    /* ── Mark all read ──────────────────────────── */
    async function markAllRead() {
        const uid = userId();
        if (!uid) return;
        try {
            await fetch(`${BASE_URL}/notifications/users/${uid}/read-all`, { method: 'PUT' });
            notifState.items.forEach(n => (n.isRead = true));
            renderNotifDropdown();
            F3Toast.success('Đã đọc tất cả thông báo');
        } catch (_) {}
    }

    /* ── SSE (Server-Sent Events) ───────────────── */
    function initSSE() {
        const uid = userId();
        if (!uid || !window.EventSource) return startPolling();

        const url = `${BASE_URL}/notifications/users/${uid}/sse`;

        try {
            if (sseSource) {
                sseSource.close();
                sseSource = null;
            }

            sseSource = new EventSource(url);

            sseSource.addEventListener('open', () => setIndicator(true));

            sseSource.addEventListener('error', () => {
                sseSource.close();
                sseSource = null;
                setIndicator(false);
                setTimeout(initSSE, 10000);
            });

            sseSource.addEventListener('notification', (e) => {
                try {
                    const data = JSON.parse(e.data);
                    notifState.items.unshift(data);
                    renderNotifDropdown();

                    dispatchEvent('f3:notification', data);
                } catch (_) {}
            });

        } catch (_) {
            startPolling();
        }
    }
    /* ── Fallback Polling ───────────────────────── */
    function startPolling() {
        if (pollTimer) return;
        fetchNotifications();
        pollTimer = setInterval(fetchNotifications, POLL_MS);
    }

    function stopPolling() {
        if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
    }

    /* ── Live Clock ─────────────────────────────── */
    function startClock() {
        const el = document.getElementById('liveClock');
        if (!el) return;
        function tick() {
            const now = new Date();
            el.textContent = now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
        }
        tick();
        setInterval(tick, 1000);
    }

    /* ── DOM Ready ──────────────────────────────── */
    document.addEventListener('DOMContentLoaded', function () {
        startClock();

        // Decide: SSE or polling
        if (window.F3_USE_SSE) {
            initSSE();
        } else {
            startPolling();
        }

        // Notification bell toggle
        const bell = document.getElementById('topbarNotifBtn');
        const dropdown = document.getElementById('notifDropdown');
        if (bell && dropdown) {
            bell.addEventListener('click', (e) => {
                e.stopPropagation();
                dropdown.classList.toggle('open');
                if (dropdown.classList.contains('open')) fetchNotifications();
            });
            document.addEventListener('click', () => dropdown.classList.remove('open'));
            dropdown.addEventListener('click', (e) => e.stopPropagation());
        }

        // Mark all read
        const markAllBtn = document.getElementById('markAllReadBtn');
        if (markAllBtn) markAllBtn.addEventListener('click', markAllRead);
    });

    /* ── Public API ─────────────────────────────── */
    window.F3RT = {
        fetchNotifications,
        fetchUnreadCount,
        markRead,
        markAllRead,
        isConnected: () => isConnected,
    };
})();