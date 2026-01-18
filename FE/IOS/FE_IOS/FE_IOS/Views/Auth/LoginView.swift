import SwiftUI

struct LoginView: View {
    @State private var username = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage = ""
    @State private var showError = false
    
    @State private var usernameError: String?
    @State private var passwordError: String?
    
    var body: some View {
        NavigationView {
            ZStack {
                // Background gradient
                LinearGradient(
                    colors: [Color(hex: "#E8F5E9"), Color.white],
                    startPoint: .top,
                    endPoint: .bottom
                )
                .ignoresSafeArea()
                
                VStack(spacing: 24) {
                    Spacer()
                    
                    // Logo
                    VStack(spacing: 8) {
                        Image(systemName: "leaf.circle.fill")
                            .font(.system(size: 80))
                            .foregroundColor(Color(hex: "#2E7D32"))
                        
                        Text("Agri-Connect")
                            .font(.title)
                            .fontWeight(.bold)
                            .foregroundColor(Color(hex: "#2E7D32"))
                    }
                    
                    // Title
                    Text("Đăng nhập")
                        .font(.title2)
                        .fontWeight(.semibold)
                    
                    // Form
                    VStack(spacing: 16) {
                        // Username
                        ValidatedFormField(
                            title: "Tên đăng nhập",
                            placeholder: "Nhập tên đăng nhập",
                            text: $username,
                            error: $usernameError
                        )
                        
                        // Password
                        ValidatedFormField(
                            title: "Mật khẩu",
                            placeholder: "Nhập mật khẩu",
                            text: $password,
                            error: $passwordError,
                            isSecure: true
                        )
                    }
                    .padding(.horizontal, 24)
                    
                    // Login Button
                    Button(action: login) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Đăng nhập")
                                .fontWeight(.semibold)
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 50)
                    .background(Color(hex: "#2E7D32"))
                    .foregroundColor(.white)
                    .cornerRadius(12)
                    .padding(.horizontal, 24)
                    .disabled(isLoading)
                    
                    // Register Link
                    HStack {
                        Text("Chưa có tài khoản?")
                            .foregroundColor(.gray)
                        NavigationLink(destination: RoleSelectionView()) {
                            Text("Đăng ký")
                                .foregroundColor(Color(hex: "#2E7D32"))
                                .fontWeight(.semibold)
                        }
                    }

                    
                    Spacer()
                }
            }
            .navigationBarHidden(true)
            .alert(isPresented: $showError) {
                Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .onAppear {
            username = ""
            password = ""
            usernameError = nil
            passwordError = nil
            errorMessage = ""
            showError = false
        }
    }
    
    private func login() {
        var isValid = true
        
        // Reset errors
        usernameError = nil
        passwordError = nil
        
        if username.isEmpty {
            usernameError = "Vui lòng nhập tên đăng nhập"
            isValid = false
        }
        
        if password.isEmpty {
            passwordError = "Vui lòng nhập mật khẩu"
            isValid = false
        }
        
        guard isValid else { return }
        
        isLoading = true
        
        let request = LoginRequest(username: username, password: password)
        
        APIClient.shared.request(
            endpoint: APIConfig.Auth.login,
            method: .post,
            body: request
        ) { (result: Result<ApiResponse<JwtResponse>, Error>) in
            isLoading = false
            
            switch result {
            case .success(let response):
                if response.success, let jwt = response.data {
                    // Save tokens. FE_IOSApp will observe this and switch Root View automatically.
                    TokenManager.shared.saveTokens(access: jwt.accessToken, refresh: jwt.refreshToken)
                    TokenManager.shared.saveUserInfo(id: jwt.userId, name: jwt.fullName, role: jwt.role)
                    
                    // No manual navigation needed
                } else {
                    // Backend returned failure - show friendly message
                    usernameError = "Tài khoản hoặc mật khẩu không đúng"
                }
            case .failure(let error):
                // Network or parsing error
                let errorDesc = error.localizedDescription
                if errorDesc.contains("400") || errorDesc.contains("Bad Request") {
                    usernameError = "Tài khoản hoặc mật khẩu không đúng"
                } else {
                    errorMessage = "Lỗi kết nối: \(errorDesc)"
                    showError = true
                }
            }
        }
    }
}

// MARK: - Custom TextField Style
struct RoundedTextFieldStyle: TextFieldStyle {
    func _body(configuration: TextField<Self._Label>) -> some View {
        configuration
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
    }
}


struct LoginView_Previews: PreviewProvider {
    static var previews: some View {
        LoginView()
    }
}
