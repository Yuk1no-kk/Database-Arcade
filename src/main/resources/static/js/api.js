const API = {
    async get(url, params = {}) {
        const query = new URLSearchParams(params).toString();
        const fullUrl = query ? `${url}?${query}` : url;
        const res = await fetch(fullUrl);
        const json = await res.json();
        if (json.code !== 200) {
            showToast(json.message, 'error');
            throw new Error(json.message);
        }
        return json.data;
    },

    async post(url, data = {}) {
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const json = await res.json();
        if (json.code !== 200) {
            showToast(json.message, 'error');
            throw new Error(json.message);
        }
        showToast(json.message, 'success');
        return json.data;
    },

    async put(url, data = {}) {
        const res = await fetch(url, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const json = await res.json();
        if (json.code !== 200) {
            showToast(json.message, 'error');
            throw new Error(json.message);
        }
        showToast(json.message, 'success');
        return json.data;
    },

    async del(url) {
        const res = await fetch(url, { method: 'DELETE' });
        const json = await res.json();
        if (json.code !== 200) {
            showToast(json.message, 'error');
            throw new Error(json.message);
        }
        showToast(json.message, 'success');
        return json.data;
    }
};

function showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}
