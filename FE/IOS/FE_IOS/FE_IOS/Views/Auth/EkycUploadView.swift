import SwiftUI

struct EkycUploadView: View {
    @Environment(\.presentationMode) var presentationMode
    
    let userRole: String
    
    // FARMER - CCCD fields
    @State private var idNumber = ""
    @State private var idFrontImage: UIImage?
    @State private var idBackImage: UIImage?
    
    // TRADER - Tax Code fields
    @State private var taxCode = ""
    @State private var companyName = ""
    @State private var businessLicenseImage: UIImage?
    
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showSuccess = false
    @State private var showImagePicker = false
    @State private var currentImageSelection: ImageSelection = .front
    
    enum ImageSelection {
        case front, back, license
    }
    
    var isFarmer: Bool {
        userRole == "FARMER"
    }
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                // Header
                VStack(spacing: 12) {
                    Image(systemName: "checkmark.shield.fill")
                        .font(.system(size: 60))
                        .foregroundColor(Color(hex: "#2E7D32"))
                    
                    Text("Xác minh danh tính")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text(isFarmer 
                         ? "Vui lòng cung cấp CCCD để xác minh" 
                         : "Vui lòng cung cấp mã số thuế để xác minh")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                }
                .padding(.top)
                
                if isFarmer {
                    farmerForm
                } else {
                    traderForm
                }
                
                // Submit Button
                Button(action: submitKyc) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Gửi xác minh")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(Color(hex: "#2E7D32"))
                .foregroundColor(.white)
                .cornerRadius(12)
                .disabled(isLoading)
                
                // Note
                VStack(alignment: .leading, spacing: 8) {
                    HStack {
                        Image(systemName: "info.circle.fill")
                        Text("Lưu ý:")
                            .fontWeight(.semibold)
                    }
                    .font(.subheadline)
                    
                    Text("• Thông tin sẽ được xác minh trong 24-48 giờ")
                    Text("• Đảm bảo hình ảnh rõ ràng, không bị mờ")
                    if isFarmer {
                        Text("• CCCD phải còn hạn sử dụng")
                    } else {
                        Text("• Mã số thuế phải còn hiệu lực")
                    }
                }
                .font(.caption)
                .foregroundColor(.gray)
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(12)
            }
            .padding()
        }
        .navigationTitle("Xác minh eKYC")
        .navigationBarTitleDisplayMode(.inline)
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .sheet(isPresented: $showImagePicker) {
            ImagePicker(image: bindingForCurrentSelection())
        }
    }
    
    private func bindingForCurrentSelection() -> Binding<UIImage?> {
        switch currentImageSelection {
        case .front:
            return $idFrontImage
        case .back:
            return $idBackImage
        case .license:
            return $businessLicenseImage
        }
    }
    
    // MARK: - Farmer Form
    private var farmerForm: some View {
        VStack(spacing: 16) {
            FormField(
                title: "Số CCCD",
                placeholder: "Nhập 12 số CCCD",
                text: $idNumber,
                keyboardType: .numberPad
            )
            
            ImageUploadCard(
                title: "Ảnh mặt trước CCCD",
                image: idFrontImage,
                onTap: {
                    currentImageSelection = .front
                    showImagePicker = true
                }
            )
            
            ImageUploadCard(
                title: "Ảnh mặt sau CCCD",
                image: idBackImage,
                onTap: {
                    currentImageSelection = .back
                    showImagePicker = true
                }
            )
        }
    }
    
    // MARK: - Trader Form
    private var traderForm: some View {
        VStack(spacing: 16) {
            FormField(
                title: "Mã số thuế",
                placeholder: "Nhập mã số thuế",
                text: $taxCode
            )
            
            FormField(
                title: "Tên công ty / Hộ kinh doanh",
                placeholder: "Nhập tên doanh nghiệp",
                text: $companyName
            )
            
            ImageUploadCard(
                title: "Ảnh giấy phép kinh doanh",
                image: businessLicenseImage,
                onTap: {
                    currentImageSelection = .license
                    showImagePicker = true
                }
            )
        }
    }
    
    private func submitKyc() {
        if isFarmer {
            guard !idNumber.isEmpty else {
                errorMessage = "Vui lòng nhập số CCCD"
                showError = true
                return
            }
        } else {
            guard !taxCode.isEmpty, !companyName.isEmpty else {
                errorMessage = "Vui lòng nhập đầy đủ thông tin"
                showError = true
                return
            }
        }
        
        isLoading = true
        
        let request = KycSubmissionRequest(
            kycType: isFarmer ? "CCCD" : "TAX_CODE",
            idNumber: isFarmer ? idNumber : nil,
            idFrontImage: nil,
            idBackImage: nil,
            taxCode: isFarmer ? nil : taxCode,
            companyName: isFarmer ? nil : companyName,
            businessLicense: nil
        )
        
        APIClient.shared.request(
            endpoint: APIConfig.Users.kycSubmit,
            method: .post,
            body: request
        ) { (result: Result<ApiResponse<UserProfile>, Error>) in
            isLoading = false
            
            switch result {
            case .success(let response):
                if response.success {
                    showSuccess = true
                } else {
                    errorMessage = response.message ?? "Gửi xác minh thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
}

// MARK: - Image Upload Card
struct ImageUploadCard: View {
    let title: String
    let image: UIImage?
    let onTap: () -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title)
                .font(.caption)
                .foregroundColor(.gray)
            
            Button(action: onTap) {
                if let image = image {
                    Image(uiImage: image)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 150)
                        .clipped()
                        .cornerRadius(12)
                } else {
                    VStack(spacing: 12) {
                        Image(systemName: "camera.fill")
                            .font(.system(size: 30))
                        Text("Chạm để tải ảnh lên")
                            .font(.subheadline)
                    }
                    .foregroundColor(.gray)
                    .frame(maxWidth: .infinity)
                    .frame(height: 150)
                    .background(Color(.systemGray6))
                    .cornerRadius(12)
                    .overlay(
                        RoundedRectangle(cornerRadius: 12)
                            .stroke(style: StrokeStyle(lineWidth: 2, dash: [5]))
                            .foregroundColor(.gray.opacity(0.5))
                    )
                }
            }
        }
    }
}

struct EkycUploadView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            EkycUploadView(userRole: "FARMER")
        }
    }
}

// MARK: - Image Picker Helper
import PhotosUI

struct ImagePicker: UIViewControllerRepresentable {
    @Binding var image: UIImage?
    @Environment(\.presentationMode) var presentationMode
    
    func makeUIViewController(context: Context) -> PHPickerViewController {
        var configuration = PHPickerConfiguration()
        configuration.filter = .images
        configuration.selectionLimit = 1
        
        let picker = PHPickerViewController(configuration: configuration)
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let parent: ImagePicker
        
        init(_ parent: ImagePicker) {
            self.parent = parent
        }
        
        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            parent.presentationMode.wrappedValue.dismiss()
            
            guard let provider = results.first?.itemProvider else { return }
            
            if provider.canLoadObject(ofClass: UIImage.self) {
                provider.loadObject(ofClass: UIImage.self) { image, _ in
                    DispatchQueue.main.async {
                        self.parent.image = image as? UIImage
                    }
                }
            }
        }
    }
}
