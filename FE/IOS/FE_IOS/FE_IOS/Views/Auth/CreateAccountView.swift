import SwiftUI

struct CreateAccountView: View {
    let role: String
    
    @Environment(\.presentationMode) var presentationMode
    @State private var username = ""
    @State private var phone = ""
    @State private var fullName = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var address = ""
    @State private var isLoading = false
    @State private var errorMessage = ""
    @State private var showError = false
    @State private var navigateToMain = false
    
    var roleTitle: String {
        role == "FARMER" ? "Nông dân" : "Thương lái"
    }
    
    var roleIcon: String {
        role == "FARMER" ? "🌾" : "🚛"
    }
    
    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()
            
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    VStack(spacing: 8) {
                        Text(roleIcon)
                            .font(.system(size: 60))
                        
                        Text("Đăng ký \(roleTitle)")
                            .font(.title2)
                            .fontWeight(.bold)
                    }
                    .padding(.top, 20)
                    
                    // Form
                    VStack(spacing: 16) {
                        FormField(
                            title: "Tên đăng nhập",
                            placeholder: "Nhập tên đăng nhập",
                            text: $username
                        )
                        
                        FormField(
                            title: "Số điện thoại",
                            placeholder: "0912345678",
                            text: $phone,
                            keyboardType: .phonePad
                        )
                        
                        FormField(
                            title: "Họ và tên",
                            placeholder: "Nguyễn Văn A",
                            text: $fullName
                        )
                        
                        FormField(
                            title: "Địa chỉ",
                            placeholder: "Xã, Huyện, Tỉnh",
                            text: $address
                        )
                        
                        FormField(
                            title: "Mật khẩu",
                            placeholder: "Tối thiểu 6 ký tự",
                            text: $password,
                            isSecure: true
                        )
                        
                        FormField(
                            title: "Xác nhận mật khẩu",
                            placeholder: "Nhập lại mật khẩu",
                            text: $confirmPassword,
                            isSecure: true
                        )
                    }
                    .padding(.horizontal, 24)
                    
                    // Register Button
                    Button(action: register) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        } else {
                            Text("Đăng ký")
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
                    
                    // Hidden navigation
                    NavigationLink(destination: MainTabView().navigationBarHidden(true), isActive: $navigateToMain) {
                        EmptyView()
                    }
                    
                    // Terms
                    Text("Bằng việc đăng ký, bạn đồng ý với Điều khoản sử dụng và Chính sách bảo mật của chúng tôi")
                        .font(.caption)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 24)
                }
                .padding(.bottom, 32)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
    }
    
    private func register() {
        // Validation
        guard !username.isEmpty, !fullName.isEmpty, !password.isEmpty else {
            errorMessage = "Vui lòng điền đầy đủ thông tin"
            showError = true
            return
        }
        
        guard password == confirmPassword else {
            errorMessage = "Mật khẩu xác nhận không khớp"
            showError = true
            return
        }
        
        guard password.count >= 6 else {
            errorMessage = "Mật khẩu phải có ít nhất 6 ký tự"
            showError = true
            return
        }
        
        isLoading = true
        
        let request = RegisterRequest(
            username: username,
            phone: phone.isEmpty ? nil : phone,
            password: password,
            fullName: fullName,
            address: address.isEmpty ? nil : address,
            role: role
        )
        
        APIClient.shared.request(
            endpoint: APIConfig.Auth.register,
            method: .post,
            body: request
        ) { (result: Result<ApiResponse<JwtResponse>, Error>) in
            isLoading = false
            
            switch result {
            case .success(let response):
                if response.success, let jwt = response.data {
                    TokenManager.shared.saveTokens(access: jwt.accessToken, refresh: jwt.refreshToken)
                    TokenManager.shared.saveUserInfo(id: jwt.userId, name: jwt.fullName, role: jwt.role)
                    navigateToMain = true
                } else {
                    errorMessage = response.message ?? "Đăng ký thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi kết nối: \(error.localizedDescription)"
                showError = true
            }
        }
    }
}

// MARK: - Form Field Component
struct FormField: View {
    let title: String
    let placeholder: String
    @Binding var text: String
    var keyboardType: UIKeyboardType = .default
    var isSecure: Bool = false
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundColor(.gray)
            
            if isSecure {
                SecureField(placeholder, text: $text)
                    .textFieldStyle(RoundedTextFieldStyle())
            } else {
                TextField(placeholder, text: $text)
                    .textFieldStyle(RoundedTextFieldStyle())
                    .keyboardType(keyboardType)
                    .autocapitalization(.none)
            }
        }
    }
}

struct CreateAccountView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            CreateAccountView(role: "FARMER")
        }
    }
}
