import SwiftUI

struct EditPostView: View {
    let post: Post
    let onUpdate: () -> Void
    
    @Environment(\.presentationMode) var presentationMode
    
    // Form fields - pre-populated from post
    @State private var title: String
    @State private var description: String
    @State private var price: String
    @State private var quantity: String
    @State private var unit: String
    @State private var selectedCategory: String?
    @State private var province: String
    @State private var district: String
    
    // Image editing
    @State private var selectedImages: [UIImage] = []
    @State private var existingImageUrls: [String] = []
    @State private var showImagePicker = false
    
    // Categories
    @State private var categories: [Category] = []
    
    // State
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showError = false
    
    // Units
    let units = ["kg", "tấn", "yến", "bó", "cái", "thùng"]
    
    init(post: Post, onUpdate: @escaping () -> Void) {
        self.post = post
        self.onUpdate = onUpdate
        
        // Initialize state from post
        _title = State(initialValue: post.title)
        _description = State(initialValue: post.description ?? "")
        _price = State(initialValue: String(post.price ?? 0))
        _quantity = State(initialValue: String(post.quantity ?? 0))
        _unit = State(initialValue: post.unit ?? "kg")
        _selectedCategory = State(initialValue: post.categoryId)
        _province = State(initialValue: post.province ?? "")
        _district = State(initialValue: post.district ?? "")
        
        // Initialize existing images
        _existingImageUrls = State(initialValue: post.images ?? [])
    }
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Thông tin sản phẩm")) {
                    TextField("Tiêu đề", text: $title)
                    
                    Picker("Danh mục", selection: $selectedCategory) {
                        Text("Chọn danh mục").tag(nil as String?)
                        ForEach(categories) { category in
                            Text(category.name).tag(category.id as String?)
                        }
                    }
                    
                    TextEditor(text: $description)
                        .frame(height: 100)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                        )
                }
                
                // Images Section
                Section(header: Text("Hình ảnh")) {
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
                    
                    if !existingImageUrls.isEmpty {
                        Text("Bài đăng hiện có \(existingImageUrls.count) ảnh. Thêm ảnh mới để thay thế hoặc giữ nguyên.")
                            .font(.caption)
                            .foregroundColor(.gray)
                    }
                }
                
                Section(header: Text("Giá cả")) {
                    HStack {
                        TextField("Giá", text: $price)
                            .keyboardType(.numberPad)
                        Text("VNĐ")
                            .foregroundColor(.gray)
                    }
                    
                    HStack {
                        TextField("Số lượng", text: $quantity)
                            .keyboardType(.decimalPad)
                        
                        Picker("Đơn vị", selection: $unit) {
                            ForEach(units, id: \.self) { unit in
                                Text(unit).tag(unit)
                            }
                        }
                        .pickerStyle(.menu)
                    }
                }
                
                Section(header: Text("Vị trí")) {
                    TextField("Tỉnh/Thành phố", text: $province)
                    TextField("Quận/Huyện", text: $district)
                }
                
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
            }
            .navigationTitle("Sửa bài đăng")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Hủy") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Lưu") {
                        updatePost()
                    }
                    .disabled(isLoading || !isValid)
                }
            }
            .onAppear {
                loadCategories()
            }
            .sheet(isPresented: $showImagePicker) {
                UnifiedMultiImagePicker(images: $selectedImages, selectionLimit: 5)
            }
        }
    }
    
    private var isValid: Bool {
        !title.isEmpty && 
        selectedCategory != nil &&
        !price.isEmpty &&
        !quantity.isEmpty
    }
    
    private func loadCategories() {
        APIClient.shared.request(
            endpoint: APIConfig.Categories.list,
            method: .get
        ) { (result: Result<ApiResponse<[Category]>, Error>) in
            if case .success(let response) = result, let categoryList = response.data {
                categories = categoryList
            }
        }
    }
    
    private func updatePost() {
        guard isValid else {
            errorMessage = "Vui lòng điền đầy đủ thông tin"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        struct UpdatePostRequest: Encodable {
            let title: String
            let description: String
            let price: Double
            let quantity: Double
            let unit: String
            let categoryId: String
            let location: LocationRequest
            let images: [String]?
        }
        
        struct LocationRequest: Encodable {
            let province: String
            let district: String
        }
        
        // Check if images were modified
        if !selectedImages.isEmpty {
            // User added new images - upload them first
            APIClient.shared.uploadImages(selectedImages, folder: "posts") { result in
                DispatchQueue.main.async {
                    switch result {
                    case .success(let urls):
                        // Proceed with update using new image URLs
                        self.sendUpdateRequest(imageUrls: urls)
                    case .failure(let error):
                        self.isLoading = false
                        self.errorMessage = "Lỗi upload ảnh: \(error.localizedDescription)"
                        self.showError = true
                    }
                }
            }
        } else {
            // No new images - keep existing ones
            sendUpdateRequest(imageUrls: existingImageUrls.isEmpty ? nil : existingImageUrls)
        }
    }
    
    private func sendUpdateRequest(imageUrls: [String]?) {
        let body = UpdatePostRequest(
            title: title,
            description: description,
            price: Double(price) ?? 0,
            quantity: Double(quantity) ?? 0,
            unit: unit,
            categoryId: selectedCategory ?? "",
            location: LocationRequest(province: province, district: district),
            images: imageUrls
        )
        
        APIClient.shared.request(
            endpoint: APIConfig.Posts.update(post.id),
            method: .put,
            body: body
        ) { (result: Result<ApiResponse<Post>, Error>) in
            isLoading = false
            
            switch result {
            case .success:
                onUpdate()
                presentationMode.wrappedValue.dismiss()
                
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
}
