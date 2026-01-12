// API Configuration and Client
const API_BASE_URL = 'http://localhost:8080';

class ApiClient {
    constructor() {
        this.baseUrl = API_BASE_URL;
    }

    getToken() {
        return localStorage.getItem('adminToken');
    }

    getHeaders() {
        const headers = {
            'Content-Type': 'application/json'
        };
        const token = this.getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        return headers;
    }

    async request(endpoint, options = {}) {
        const url = `${this.baseUrl}${endpoint}`;
        const config = {
            ...options,
            headers: this.getHeaders()
        };

        try {
            const response = await fetch(url, config);
            const data = await response.json();

            if (response.status === 401) {
                // Token expired or invalid
                localStorage.removeItem('adminToken');
                window.location.href = 'login.html';
                return null;
            }

            return data;
        } catch (error) {
            console.error('API Error:', error);
            throw error;
        }
    }

    async get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    }

    async post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body)
        });
    }

    async put(endpoint, body = null) {
        const options = { method: 'PUT' };
        if (body) {
            options.body = JSON.stringify(body);
        }
        return this.request(endpoint, options);
    }

    async delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    }

    // Auth
    async login(username, password) {
        const response = await fetch(`${this.baseUrl}/api/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        return response.json();
    }

    // Admin Dashboard
    async getDashboardStats() {
        return this.get('/api/admin/dashboard');
    }

    async getAdminLogs() {
        return this.get('/api/admin/logs');
    }

    // Posts
    async getAllPosts(status = null) {
        let url = '/api/posts';
        if (status) {
            url += `?status=${status}`;
        }
        return this.get(url);
    }

    async approvePost(postId) {
        return this.put(`/api/posts/${postId}/approve`);
    }

    async rejectPost(postId, reason = '') {
        return this.put(`/api/posts/${postId}/reject?reason=${encodeURIComponent(reason)}`);
    }

    // Users
    async getAllUsers() {
        return this.get('/api/users');
    }

    async verifyKyc(userId) {
        return this.put(`/api/users/${userId}/kyc/verify`);
    }

    async rejectKyc(userId, reason = '') {
        return this.put(`/api/users/${userId}/kyc/reject?reason=${encodeURIComponent(reason)}`);
    }
}

// Global API instance
const api = new ApiClient();
