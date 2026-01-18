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
    
    @State private var usernameError: String?
    @State private var phoneError: String?
    @State private var fullNameError: String?
    @State private var addressError: String?
    @State private var passwordError: String?
    @State private var confirmPasswordError: String?
    
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
                        ValidatedFormField(
                            title: "Tên đăng nhập",
                            placeholder: "Nhập tên đăng nhập",
                            text: $username,
                            error: $usernameError
                        )
                        
                        ValidatedFormField(
                            title: "Số điện thoại",
                            placeholder: "0912345678",
                            text: $phone,
                            error: $phoneError,
                            keyboardType: .phonePad
                        )
                        
                        ValidatedFormField(
                            title: "Họ và tên",
                            placeholder: "Nguyễn Văn A",
                            text: $fullName,
                            error: $fullNameError
                        )
                        
                        VStack(alignment: .leading, spacing: 4) {
                            HStack {
                                Text("Địa chỉ")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                                Spacer()
                                LocationFillButton { province, district in
                                    self.address = "\(district), \(province)"
                                    self.addressError = nil // Clear error on fill
                                }
                            }
                            TextField("Xã, Huyện, Tỉnh", text: $address)
                                .textFieldStyle(RoundedTextFieldStyle())
                                .autocapitalization(.words)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(addressError != nil ? Color.red : Color.clear, lineWidth: 1)
                                )
                            
                            if let error = addressError {
                                Text(error)
                                    .font(.caption)
                                    .foregroundColor(.red)
                            }
                        }
                        
                        ValidatedFormField(
                            title: "Mật khẩu",
                            placeholder: "Tối thiểu 6 ký tự",
                            text: $password,
                            error: $passwordError,
                            isSecure: true
                        )
                        
                        ValidatedFormField(
                            title: "Xác nhận mật khẩu",
                            placeholder: "Nhập lại mật khẩu",
                            text: $confirmPassword,
                            error: $confirmPasswordError,
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
        var isValid = true
        
        // Reset errors
        usernameError = nil
        phoneError = nil
        fullNameError = nil
        addressError = nil
        passwordError = nil
        confirmPasswordError = nil
        
        if username.isEmpty {
            usernameError = "Vui lòng nhập tên đăng nhập"
            isValid = false
        }
        
        if fullName.isEmpty {
            fullNameError = "Vui lòng nhập họ tên"
            isValid = false
        }
        
        if address.isEmpty {
            addressError = "Vui lòng nhập địa chỉ"
            isValid = false
        }
        
        if password.isEmpty {
            passwordError = "Vui lòng nhập mật khẩu"
            isValid = false
        } else if password.count < 6 {
            passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
            isValid = false
        }
        
        if confirmPassword != password {
            confirmPasswordError = "Mật khẩu xác nhận không khớp"
            isValid = false
        }
        
        guard isValid else { return }
        
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
// Removed generic FormField struct as it is replaced by ValidatedFormField

struct CreateAccountView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            CreateAccountView(role: "FARMER")
        }
    }
}
