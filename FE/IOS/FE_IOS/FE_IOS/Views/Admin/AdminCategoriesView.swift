import SwiftUI

struct AdminCategoriesView: View {
    @State private var categories: [Category] = []
    @State private var isLoading = false
    @State private var showAddSheet = false
    @State private var editingCategory: Category?
    @State private var newCategoryName = ""
    @State private var newCategoryDescription = ""
    @State private var newCategoryIcon = "🌾"
    
    let icons = ["🌾", "🥬", "🍎", "🐔", "🐟", "🥩", "🌽", "🍚", "☕", "🌶️"]
    
    var body: some View {
        VStack(spacing: 0) {
            // Add Button
            Button {
                resetForm()
                showAddSheet = true
            } label: {
                HStack {
                    Image(systemName: "plus.circle.fill")
                    Text("Thêm danh mục mới")
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color(hex: "#2E7D32"))
                .foregroundColor(.white)
                .cornerRadius(12)
            }
            .padding()
            
            // Categories List
            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if categories.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "folder")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Chưa có danh mục nào")
                        .foregroundColor(.gray)
                }
                Spacer()
            } else {
                List {
                    ForEach(categories) { category in
                        CategoryRow(category: category) {
                            editingCategory = category
                            newCategoryName = category.name
                            newCategoryDescription = category.description ?? ""
                            newCategoryIcon = category.icon ?? "🌾"
                            showAddSheet = true
                        }
                    }
                    .onDelete(perform: deleteCategories)
                }
                .listStyle(PlainListStyle())
            }
        }
        .navigationTitle("Quản lý danh mục")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadCategories()
        }
        .sheet(isPresented: $showAddSheet) {
            NavigationView {
                categoryForm
            }
        }
    }
    
    // MARK: - Category Form
    private var categoryForm: some View {
        Form {
            Section(header: Text("Icon")) {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 12) {
                        ForEach(icons, id: \.self) { icon in
                            Button {
                                newCategoryIcon = icon
                            } label: {
                                Text(icon)
                                    .font(.title)
                                    .padding(8)
                                    .background(
                                        newCategoryIcon == icon
                                        ? Color(hex: "#E8F5E9")
                                        : Color(.systemGray6)
                                    )
                                    .cornerRadius(12)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 12)
                                            .stroke(
                                                newCategoryIcon == icon
                                                ? Color(hex: "#2E7D32")
                                                : Color.clear,
                                                lineWidth: 2
                                            )
                                    )
                            }
                        }
                    }
                }
            }
            
            Section(header: Text("Thông tin")) {
                TextField("Tên danh mục", text: $newCategoryName)
                TextField("Mô tả (tùy chọn)", text: $newCategoryDescription)
            }
        }
        .navigationTitle(editingCategory == nil ? "Thêm danh mục" : "Sửa danh mục")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarItems(
            leading: Button("Hủy") {
                showAddSheet = false
            },
            trailing: Button("Lưu") {
                saveCategory()
            }
            .disabled(newCategoryName.isEmpty)
        )
    }
    
    private func resetForm() {
        editingCategory = nil
        newCategoryName = ""
        newCategoryDescription = ""
        newCategoryIcon = "🌾"
    }
    
    private func loadCategories() {
        isLoading = true
        
        APIClient.shared.request(
            endpoint: APIConfig.Categories.list,
            method: .get
        ) { (result: Result<ApiResponse<[Category]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                categories = data
            }
        }
    }
    
    private func saveCategory() {
        let body: [String: String] = [
            "name": newCategoryName,
            "description": newCategoryDescription,
            "icon": newCategoryIcon
        ]
        
        let endpoint = editingCategory == nil
            ? APIConfig.Categories.list
            : "\(APIConfig.Categories.list)/\(editingCategory!.id)"
        
        let method: HTTPMethod = editingCategory == nil ? .post : .put
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: method,
            body: body
        ) { (result: Result<ApiResponse<Category>, Error>) in
            showAddSheet = false
            loadCategories()
        }
    }
    
    private func deleteCategories(at offsets: IndexSet) {
        for index in offsets {
            let category = categories[index]
            
            APIClient.shared.request(
                endpoint: "\(APIConfig.Categories.list)/\(category.id)",
                method: .delete,
                body: nil as String?
            ) { (result: Result<ApiResponse<String>, Error>) in
                loadCategories()
            }
        }
    }
}

// MARK: - Category Row
struct CategoryRow: View {
    let category: Category
    let onEdit: () -> Void
    
    var body: some View {
        HStack(spacing: 16) {
            Text(category.icon ?? "🌾")
                .font(.title)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(category.name)
                    .font(.headline)
                
                if let desc = category.description, !desc.isEmpty {
                    Text(desc)
                        .font(.caption)
                        .foregroundColor(.gray)
                        .lineLimit(1)
                }
            }
            
            Spacer()
            
            Button {
                onEdit()
            } label: {
                Image(systemName: "pencil.circle.fill")
                    .foregroundColor(Color(hex: "#2E7D32"))
                    .font(.title2)
            }
        }
        .padding(.vertical, 4)
    }
}

struct AdminCategoriesView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            AdminCategoriesView()
        }
    }
}
