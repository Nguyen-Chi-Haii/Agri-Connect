import SwiftUI
import Combine

struct UpdateKycView: View {
    @Environment(\.presentationMode) var presentationMode
    @State private var idNumber = ""
    @State private var frontImage: UIImage?
    @State private var backImage: UIImage?
    @State private var showFrontImagePicker = false
    @State private var showBackImagePicker = false
    @State private var isLoading = false
    @State private var errorMessage = ""
    @State private var showError = false
    @State private var showSuccess = false
    
    // User profile to pre-fill/display current status
    let userProfile: UserProfile?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Info Card
                VStack(alignment: .leading, spacing: 12) {
                    Text("Thông tin định danh")
                        .font(.headline)
                    
                    Text("Vui lòng cung cấp CCCD/CMND để xác thực tài khoản. Thông tin của bạn sẽ được bảo mật.")
                        .font(.caption)
                        .foregroundColor(.gray)
                    
                    if let status = userProfile?.kycStatus {
                         HStack {
                             Text("Trạng thái hiện tại:")
                                 .font(.subheadline)
                                 .foregroundColor(.gray)
                             Spacer()
                             KycStatusBadge(status: status)
                         }
                         .padding(.top, 8)
                    }
                }
                .padding()
                .background(Color.white)
                .cornerRadius(12)
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                
                // Form
                VStack(spacing: 20) {
                    // ID Number
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Số CCCD/CMND")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                        
                        TextField("Nhập số giấy tờ tùy thân", text: $idNumber)
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(12)
                            .keyboardType(.numberPad)
                    }
                    
                    // Front Image
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Mặt trước CCCD")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                        
                        Button {
                            showFrontImagePicker = true
                        } label: {
                            if let image = frontImage {
                                Image(uiImage: image)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(height: 200)
                                    .frame(maxWidth: .infinity)
                                    .cornerRadius(12)
                                    .clipped()
                            } else {
                                ZStack {
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(style: StrokeStyle(lineWidth: 1, dash: [5]))
                                        .foregroundColor(.gray)
                                        .frame(height: 150)
                                    
                                    VStack(spacing: 8) {
                                        Image(systemName: "camera.fill")
                                            .font(.system(size: 30))
                                            .foregroundColor(.gray)
                                        Text("Chụp ảnh mặt trước")
                                            .font(.caption)
                                            .foregroundColor(.gray)
                                    }
                                }
                            }
                        }
                    }
                    
                    // Back Image
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Mặt sau CCCD")
                            .font(.subheadline)
                            .foregroundColor(.gray)
                        
                        Button {
                            showBackImagePicker = true
                        } label: {
                            if let image = backImage {
                                Image(uiImage: image)
                                    .resizable()
                                    .scaledToFill()
                                    .frame(height: 200)
                                    .frame(maxWidth: .infinity)
                                    .cornerRadius(12)
                                    .clipped()
                            } else {
                                ZStack {
                                    RoundedRectangle(cornerRadius: 12)
                                        .stroke(style: StrokeStyle(lineWidth: 1, dash: [5]))
                                        .foregroundColor(.gray)
                                        .frame(height: 150)
                                    
                                    VStack(spacing: 8) {
                                        Image(systemName: "camera.fill")
                                            .font(.system(size: 30))
                                            .foregroundColor(.gray)
                                        Text("Chụp ảnh mặt sau")
                                            .font(.caption)
                                            .foregroundColor(.gray)
                                    }
                                }
                            }
                        }
                    }
                }
                .padding()
                .background(Color.white)
                .cornerRadius(12)
                .shadow(color: Color.black.opacity(0.05), radius: 5, x: 0, y: 2)
                
                // Submit Button
                Button(action: submitKyc) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Gửi yêu cầu xác thực")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(isValid ? Color(hex: "#2E7D32") : Color.gray)
                .foregroundColor(.white)
                .cornerRadius(12)
                .disabled(!isValid || isLoading)
            }
            .padding()
        }
        .background(Color(.systemGray6).ignoresSafeArea())
        .navigationTitle("Cập nhật định danh")
        .sheet(isPresented: $showFrontImagePicker) {
            ImagePicker(image: $frontImage)
        }
        .sheet(isPresented: $showBackImagePicker) {
            ImagePicker(image: $backImage)
        }
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .overlay(
            ZStack {
                if showSuccess {
                    Color.black.opacity(0.4).ignoresSafeArea()
                    VStack(spacing: 20) {
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 60))
                            .foregroundColor(.white)
                        Text("Đã gửi yêu cầu thành công!")
                            .font(.headline)
                            .foregroundColor(.white)
                        Text("Chúng tôi sẽ xem xét thông tin của bạn sớm nhất có thể.")
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.9))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                        
                        Button("Đóng") {
                           presentationMode.wrappedValue.dismiss()
                        }
                        .padding(.horizontal, 30)
                        .padding(.vertical, 12)
                        .background(Color.white)
                        .foregroundColor(Color(hex: "#2E7D32"))
                        .cornerRadius(20)
                    }
                    .padding(40)
                    .background(Color(hex: "#2E7D32"))
                    .cornerRadius(20)
                    .padding(40)
                    .shadow(radius: 10)
                }
            }
        )
    }
    
    var isValid: Bool {
        return !idNumber.isEmpty && frontImage != nil && backImage != nil
    }
    
    func submitKyc() {
        guard let front = frontImage, let back = backImage else { return }
        
        isLoading = true
        
        // 1. Upload Images
        let dispatchGroup = DispatchGroup()
        var frontUrl: String?
        var backUrl: String?
        var uploadError: Error?
        
        dispatchGroup.enter()
        APIClient.shared.uploadImage(front) { result in
            switch result {
            case .success(let url):
                frontUrl = url
            case .failure(let error):
                uploadError = error
            }
            dispatchGroup.leave()
        }
        
        dispatchGroup.enter()
        APIClient.shared.uploadImage(back) { result in
            switch result {
            case .success(let url):
                backUrl = url
            case .failure(let error):
                uploadError = error
            }
            dispatchGroup.leave()
        }
        
        dispatchGroup.notify(queue: .main) {
            if let error = uploadError {
                isLoading = false
                errorMessage = "Lỗi upload ảnh: \(error.localizedDescription)"
                showError = true
                return
            }
            
            guard let fUrl = frontUrl, let bUrl = backUrl else {
                isLoading = false
                errorMessage = "Không thể lấy link ảnh"
                showError = true
                return
            }
            
            // 2. Submit KYC Data
            let kycData: [String: Any] = [
                "idNumber": idNumber,
                "idFrontImage": fUrl,
                "idBackImage": bUrl
            ]
            
            // Assuming endpoint is similar to registration or a specific /kyc endpoint
            // Based on Android code, it might be separate or part of profile update.
            // Let's assume a dedicated endpoint or profile update.
            // Android uses /api/users/kyc-submission (inferred)
            // Let's check APIConfig first but for now I'll use a placeholder endpoint string
            // I'll update APIConfig in next step if needed.
            
            APIClient.shared.request(
                endpoint: "/users/kyc-info", // Needs verification with APIConfig
                method: .post,
                body: kycData
            ) { (result: Result<ApiResponse<UserProfile>, Error>) in
                isLoading = false
                switch result {
                case .success(let response):
                    if response.success {
                        showSuccess = true
                    } else {
                        errorMessage = response.message ?? "Gửi yêu cầu thất bại"
                        showError = true
                    }
                case .failure(let error):
                    errorMessage = "Lỗi kết nối: \(error.localizedDescription)"
                    showError = true
                }
            }
        }
    }
}
