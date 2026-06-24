window.F3Api = (function () {
    const BASE = window.F3_API_BASE || '/api';

    function buildUrl(url) {
        if (url.startsWith('http')) return url;

        // 🔥 tránh /api/api
        if (url.startsWith('/api')) return url;

        return BASE + url;
    }

    function getToken() {
        const t = localStorage.getItem('token');

        // 🔥 tránh gửi "null" hoặc "undefined"
        if (!t || t === 'null' || t === 'undefined') return null;

        return t;
    }

    function getHeaders() {
        const token = getToken();

        const headers = {
            'Content-Type': 'application/json'
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return headers;
    }

    async function request(method, url, data) {
        const fullUrl = buildUrl(url);

        try {
            const res = await fetch(fullUrl, {
                method,
                headers: getHeaders(),
                credentials: 'include',
                body: data ? JSON.stringify(data) : undefined
            });

            const text = await res.text();

            let json = null;
            try {
                json = text ? JSON.parse(text) : null;
            } catch {
                console.warn('Response không phải JSON:', text);
            }

            // 🔥 HANDLE AUTH LỖI
            if (res.status === 401) {
                console.warn('❌ Chưa đăng nhập');

                localStorage.removeItem('token');
                alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');

                window.location.href = '/login';
                return;
            }

            if (res.status === 403) {
                console.warn('❌ Không có quyền (Access Denied)');
                throw new Error('Bạn không có quyền truy cập');
            }

            if (!res.ok) {
                console.error('API ERROR:', {
                    method,
                    url: fullUrl,
                    status: res.status,
                    body: text
                });

                throw new Error(
                    json?.message ||
                    `HTTP ${res.status} - ${res.statusText}`
                );
            }

            return json;

        } catch (err) {
            console.error('FETCH ERROR:', err);
            throw new Error(err.message || 'Lỗi kết nối server');
        }
    }

    return {
        get:    (url)       => request('GET', url),
        post:   (url, data) => request('POST', url, data),
        patch:  (url, data) => request('PATCH', url, data),
        put:    (url, data) => request('PUT', url, data),
        delete: (url)       => request('DELETE', url),
    };
})();