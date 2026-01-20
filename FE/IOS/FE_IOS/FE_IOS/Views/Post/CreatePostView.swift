import SwiftUI

struct CreatePostView: View {
    @Binding var tabSelection: Int
    @State private var title = ""
    @State private var description = ""
    @State private var price = ""
    @State private var unit = "kg"
    @State private var quantity = ""
    @State private var province = ""
    @State private var district = ""
    @State private var selectedCategory: Category?
    @State private var categories: [Category] = []
    @State private var selectedImages: [UIImage] = []
    @State private var isLoading = false
    @State private var showError = false
    @State private var errorMessage = ""
    @State private var showSuccess = false
    @State private var showImagePicker = false
    
    @State private var titleError: String?
    @State private var descriptionError: String?
    @State private var priceError: String?
    @State private var quantityError: String?
    @State private var provinceError: String?
    @State private var districtError: String?
    @State private var categoryError: String?
    @State private var showKYCAlert = false
    @State private var kycAlertTitle = ""
    @State private var kycAlertMessage = ""
    
    let units = ["kg", "tấn", "bao", "con", "cây", "trái", "chục"]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                // Images
                VStack(alignment: .leading, spacing: 8) {
                    Text("Hình ảnh")
                        .font(.headline)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            // Add button
                            Button(action: {
                                showImagePicker = true
                            }) {
                                VStack {
                                    Image(systemName: "plus.circle.fill")
                                        .font(.system(size: 30))
                                    Text("Thêm ảnh")
                                        .font(.caption)
                                }
                                .frame(width: 80, height: 80)
                                .background(Color(.systemGray6))
                                .cornerRadius(12)
                            }
                            .foregroundColor(Color(hex: "#2E7D32"))
                            
                            ForEach(selectedImages.indices, id: \.self) { index in
                                ZStack(alignment: .topTrailing) {
                                    Image(uiImage: selectedImages[index])
                                        .resizable()
                                        .aspectRatio(contentMode: .fill)
                                        .frame(width: 80, height: 80)
                                        .cornerRadius(12)
                                        .clipped()
                                    
                                    Button {
                                        selectedImages.remove(at: index)
                                    } label: {
                                        Image(systemName: "xmark.circle.fill")
                                            .foregroundColor(.red)
                                    }
                                    .offset(x: 5, y: -5)
                                }
                            }
                        }
                    }
                }
                
                // Category
                VStack(alignment: .leading, spacing: 8) {
                    Text("Danh mục")
                        .font(.headline)
                    
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(categories) { category in
                                Button {
                                    selectedCategory = category
                                    categoryError = nil
                                } label: {
                                    Text(category.name)
                                        .padding(.horizontal, 16)
                                        .padding(.vertical, 8)
                                        .background(
                                            selectedCategory?.id == category.id
                                            ? Color(hex: "#2E7D32")
                                            : Color(.systemGray6)
                                        )
                                        .foregroundColor(
                                            selectedCategory?.id == category.id
                                            ? .white
                                            : .primary
                                        )
                                        .cornerRadius(20)
                                }
                            }
                        }
                    }
                    if let error = categoryError {
                        Text(error)
                            .font(.caption)
                            .foregroundColor(.red)
                    }
                }
                
                // Title
                ValidatedFormField(
                    title: "Tiêu đề",
                    placeholder: "VD: Gạo ST25 hữu cơ",
                    text: $title,
                    error: $titleError
                )
                
                // Description
                VStack(alignment: .leading, spacing: 4) {
                    Text("Mô tả")
                        .font(.caption)
                        .foregroundColor(.gray)
                    TextEditor(text: $description)
                        .frame(height: 100)
                        .padding(8)
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                }
                
                // Price and Unit
                HStack(spacing: 12) {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Giá")
                            .font(.caption)
                            .foregroundColor(.gray)
                        TextField("0", text: $price)
                            .keyboardType(.numberPad)
                            .textFieldStyle(RoundedTextFieldStyle())
                            .overlay(
                                RoundedRectangle(cornerRadius: 12)
                                    .stroke(priceError != nil ? Color.red : Color.clear, lineWidth: 1)
                            )
                        if let error = priceError {
                            Text(error)
                                .font(.caption)
                                .foregroundColor(.red)
                        }
                    }
                    
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Đơn vị")
                            .font(.caption)
                            .foregroundColor(.gray)
                        Picker("Đơn vị", selection: $unit) {
                            ForEach(units, id: \.self) { u in
                                Text(u).tag(u)
                            }
                        }
                        .pickerStyle(MenuPickerStyle())
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                    }
                }
                
                // Quantity
                ValidatedFormField(
                    title: "Số lượng",
                    placeholder: "VD: 100",
                    text: $quantity,
                    error: $quantityError,
                    keyboardType: .numberPad
                )
                
                // Location
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("Vị trí")
                            .font(.headline)
                        Spacer()
                        LocationFillButton { province, district in
                            self.province = province
                            self.district = district
                            self.provinceError = nil
                            self.districtError = nil
                        }
                    }
                    
                    HStack(spacing: 12) {
                        ValidatedFormField(
                            title: "Tỉnh/Thành",
                            placeholder: "VD: An Giang",
                            text: $province,
                            error: $provinceError
                        )
                        .disabled(LocationManager.shared.isLoading)
                        
                        ValidatedFormField(
                            title: "Quận/Huyện",
                            placeholder: "VD: Châu Đốc",
                            text: $district,
                            error: $districtError
                        )
                        .disabled(LocationManager.shared.isLoading)
                    }
                }
                
                // Submit Button
                Button(action: submitPost) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Đăng bài")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(Color(hex: "#2E7D32"))
                .foregroundColor(.white)
                .cornerRadius(12)
                .disabled(isLoading)
            }
            .padding()
        }
        .navigationTitle("Tạo bài đăng")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            // Clear location fields to prevent cached values
            province = ""
            district = ""
            loadCategories()
            checkKYCStatus()
        }
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .alert(isPresented: $showKYCAlert) {
            KYCHelper.showKYCAlert(
                title: kycAlertTitle,
                message: kycAlertMessage,
                navigateToProfile: Binding(
                    get: { false },
                    set: { shouldNavigate in
                        if shouldNavigate {
                            // User clicked "Verify Now" -> Go to Profile (4)
                            self.tabSelection = 4
                        } else {
                            // User clicked "Later" -> Go to Home (0) to block access
                            self.tabSelection = 0
                        }
                    }
                )
            )
        }
        .alert("Thành công", isPresented: $showSuccess) {
            Button("Xem bài đăng") {
                clearForm()
                tabSelection = 4 // Go to Profile
            }
            Button("Tiếp tục đăng") {
                clearForm()
            }
        } message: {
            Text("Bài đăng của bạn đã được khởi tạo và đang chờ duyệt.")
        }
        .sheet(isPresented: $showImagePicker) {
            UnifiedMultiImagePicker(images: $selectedImages, selectionLimit: 5)
        }
    }
    
    private func loadCategories() {
        APIClient.shared.request(
            endpoint: APIConfig.Categories.list,
            method: .get
        ) { (result: Result<ApiResponse<[Category]>, Error>) in
            if case .success(let response) = result, let data = response.data {
                categories = data
            }
        }
    }
    
    private func submitPost() {
        // Double check KYC before submitting
        KYCHelper.shared.requireVerified(
            onSuccess: {
                proceedSubmission()
            },
            onFailure: { title, message in
                kycAlertTitle = title
                kycAlertMessage = message ?? "Cần xác thực"
                showKYCAlert = true
            }
        )
    }
    
    private func proceedSubmission() {
        var isValid = true
        
        // Reset errors
        categoryError = nil
        titleError = nil
        priceError = nil
        quantityError = nil
        provinceError = nil
        districtError = nil
        
        if selectedCategory == nil {
            categoryError = "Vui lòng chọn danh mục"
            isValid = false
        }
        
        if title.isEmpty {
            titleError = "Vui lòng nhập tiêu đề"
            isValid = false
        }
        
        if price.isEmpty {
            priceError = "Vui lòng nhập giá"
            isValid = false
        }
        
        if quantity.isEmpty {
            quantityError = "Vui lòng nhập số lượng"
            isValid = false
        }
        
        if province.isEmpty {
            provinceError = "Bắt buộc"
            isValid = false
        }
        
        if district.isEmpty {
            districtError = "Bắt buộc"
            isValid = false
        }
        
        guard isValid else { return }
        
        isLoading = true
        
        // 1. Upload images first if any
        if !selectedImages.isEmpty {
            APIClient.shared.uploadImages(selectedImages, folder: "posts") { result in
                DispatchQueue.main.async {
                    switch result {
                    case .success(let urls):
                        // 2. Proceed to create post with URLs
                        self.createPost(imageUrls: urls)
                    case .failure(let error):
                        self.isLoading = false
                        self.errorMessage = "Lỗi upload ảnh: \(error.localizedDescription)"
                        self.showError = true
                    }
                }
            }
        } else {
            // No images, just create post
            createPost(imageUrls: nil)
        }
    }
    
    private func createPost(imageUrls: [String]?) {
        let request = CreatePostRequest(
            categoryId: selectedCategory!.id,
            title: title,
            description: description,
            price: Double(price) ?? 0,
            unit: unit,
            quantity: Double(quantity) ?? 0,
            images: imageUrls,
            location: CreateLocationRequest(province: province, district: district)
        )
        
        APIClient.shared.request(
            endpoint: APIConfig.Posts.list,
            method: .post,
            body: request
        ) { (result: Result<ApiResponse<Post>, Error>) in
            DispatchQueue.main.async {
                self.isLoading = false
                
                switch result {
                case .success(let response):
                    if response.success {
                        self.showSuccess = true
                    } else {
                        self.errorMessage = response.message ?? "Đăng bài thất bại"
                        self.showError = true
                    }
                case .failure(let error):
                    self.errorMessage = "Lỗi: \(error.localizedDescription)"
                    self.showError = true
                }
            }
        }
    }
    
    private func clearForm() {
        title = ""
        description = ""
        price = ""
        quantity = ""
        province = ""
        district = ""
        selectedCategory = nil
        selectedImages = []
        
        // Reset validation errors
        categoryError = nil
        titleError = nil
        priceError = nil
        quantityError = nil
        provinceError = nil
        districtError = nil
    }
    
    private func checkKYCStatus() {
        KYCHelper.shared.requireVerified(
            onSuccess: {
                // User is verified, can proceed
            },
            onFailure: { title, message in
                kycAlertTitle = title
                kycAlertMessage = message ?? "Cần xác thực"
                showKYCAlert = true
            }
        )
    }
}


struct CreatePostView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            CreatePostView(tabSelection: .constant(2))
        }
    }
}

// MARK: - Multi Image Picker Helper

