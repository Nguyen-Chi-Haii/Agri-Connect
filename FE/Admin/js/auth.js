// Authentication Handler
const Auth = {
    TOKEN_KEY: 'adminToken',
    USER_KEY: 'adminUser',

    isLoggedIn() {
        return !!localStorage.getItem(this.TOKEN_KEY);
    },

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getUser() {
        const user = localStorage.getItem(this.USER_KEY);
        return user ? JSON.parse(user) : null;
    },

    setAuth(token, user) {
        localStorage.setItem(this.TOKEN_KEY, token);
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = 'login.html';
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = 'login.html';
            return false;
        }
        return true;
    },

    async login(username, password) {
        try {
            const response = await api.login(username, password);

            if (response.success && response.data) {
                const { accessToken, user } = response.data;

                // Check if user is admin
                if (user.role !== 'ADMIN') {
                    return { success: false, message: 'Bạn không có quyền admin' };
                }

                this.setAuth(accessToken, user);
                return { success: true };
            } else {
                return { success: false, message: response.message || 'Đăng nhập thất bại' };
            }
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, message: 'Lỗi kết nối server' };
        }
    }
};

// Check auth on protected pages
document.addEventListener('DOMContentLoaded', () => {
    // Skip auth check on login page
    if (window.location.pathname.includes('login.html')) {
        return;
    }

    // Require auth on other pages
    Auth.requireAuth();

    // Update user info in sidebar
    const user = Auth.getUser();
    if (user) {
        const userAvatar = document.querySelector('.user-avatar');
        const userName = document.querySelector('.user-details h4');
        const userRole = document.querySelector('.user-details span');

        if (userAvatar) userAvatar.textContent = user.fullName?.charAt(0) || 'A';
        if (userName) userName.textContent = user.fullName || 'Admin';
        if (userRole) userRole.textContent = 'Quản trị viên';
    }
});
