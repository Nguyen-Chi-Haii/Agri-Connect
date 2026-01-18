import SwiftUI

struct CreatePostView: View {
    @Environment(\.presentationMode) var presentationMode
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
                }
                
                // Title
                FormField(
                    title: "Tiêu đề",
                    placeholder: "VD: Gạo ST25 hữu cơ",
                    text: $title
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
                FormField(
                    title: "Số lượng",
                    placeholder: "VD: 100",
                    text: $quantity,
                    keyboardType: .numberPad
                )
                
                // Location
                HStack(spacing: 12) {
                    FormField(
                        title: "Tỉnh/Thành",
                        placeholder: "VD: An Giang",
                        text: $province
                    )
                    
                    FormField(
                        title: "Quận/Huyện",
                        placeholder: "VD: Châu Đốc",
                        text: $district
                    )
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
            loadCategories()
        }
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .sheet(isPresented: $showImagePicker) {
            MultiImagePicker(images: $selectedImages, selectionLimit: 5)
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
        guard let category = selectedCategory else {
            errorMessage = "Vui lòng chọn danh mục"
            showError = true
            return
        }
        
        guard !title.isEmpty else {
            errorMessage = "Vui lòng nhập tiêu đề"
            showError = true
            return
        }
        
        isLoading = true
        
        let request = CreatePostRequest(
            categoryId: category.id,
            title: title,
            description: description,
            price: Double(price) ?? 0,
            unit: unit,
            quantity: Double(quantity) ?? 0,
            images: nil,
            province: province.isEmpty ? nil : province,
            district: district.isEmpty ? nil : district
        )
        
        APIClient.shared.request(
            endpoint: APIConfig.Posts.list,
            method: .post,
            body: request
        ) { (result: Result<ApiResponse<Post>, Error>) in
            isLoading = false
            
            switch result {
            case .success(let response):
                if response.success {
                    showSuccess = true
                    presentationMode.wrappedValue.dismiss()
                } else {
                    errorMessage = response.message ?? "Đăng bài thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
}

struct CreatePostView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            CreatePostView()
        }
    }
}

// MARK: - Multi Image Picker Helper
import PhotosUI

struct MultiImagePicker: UIViewControllerRepresentable {
    @Binding var images: [UIImage]
    @Environment(\.presentationMode) var presentationMode
    var selectionLimit: Int = 5
    
    func makeUIViewController(context: Context) -> PHPickerViewController {
        var configuration = PHPickerConfiguration()
        configuration.filter = .images
        configuration.selectionLimit = selectionLimit
        
        let picker = PHPickerViewController(configuration: configuration)
        picker.delegate = context.coordinator
        return picker
    }
    
    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {}
    
    func makeCoordinator() -> Coordinator {
        Coordinator(self)
    }
    
    class Coordinator: NSObject, PHPickerViewControllerDelegate {
        let parent: MultiImagePicker
        
        init(_ parent: MultiImagePicker) {
            self.parent = parent
        }
        
        func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
            parent.presentationMode.wrappedValue.dismiss()
            
            var loadedImages: [UIImage] = []
            let dispatchGroup = DispatchGroup()
            
            for result in results {
                dispatchGroup.enter()
                if result.itemProvider.canLoadObject(ofClass: UIImage.self) {
                    result.itemProvider.loadObject(ofClass: UIImage.self) { image, _ in
                        if let image = image as? UIImage {
                            loadedImages.append(image)
                        }
                        dispatchGroup.leave()
                    }
                } else {
                    dispatchGroup.leave()
                }
            }
            
            dispatchGroup.notify(queue: .main) {
                self.parent.images.append(contentsOf: loadedImages)
            }
        }
    }
}
