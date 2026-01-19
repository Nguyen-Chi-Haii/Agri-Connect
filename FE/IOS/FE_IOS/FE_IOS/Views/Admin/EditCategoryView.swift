import SwiftUI

struct EditCategoryView: View {
    let category: Category
    let onUpdate: () -> Void
    
    @Environment(\.presentationMode) var presentationMode
    
    @State private var name: String
    @State private var description: String
    @State private var icon: String
    
    @State private var isLoading = false
    @State private var errorMessage: String?
    
    init(category: Category, onUpdate: @escaping () -> Void) {
        self.category = category
        self.onUpdate = onUpdate
        
        _name = State(initialValue: category.name)
        _description = State(initialValue: category.description ?? "")
        _icon = State(initialValue: category.icon ?? "📦")
    }
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Thông tin danh mục")) {
                    TextField("Tên danh mục", text: $name)
                    
                    TextField("Icon (emoji)", text: $icon)
                    
                    TextEditor(text: $description)
                        .frame(height: 100)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.gray.opacity(0.2), lineWidth: 1)
                        )
                }
                
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundColor(.red)
                            .font(.caption)
                    }
                }
            }
            .navigationTitle("Sửa danh mục")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Hủy") {
                        presentationMode.wrappedValue.dismiss()
                    }
                }
                
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Lưu") {
                        updateCategory()
                    }
                    .disabled(isLoading || name.isEmpty)
                }
            }
        }
    }
    
    private func updateCategory() {
        isLoading = true
        errorMessage = nil
        
        let body: [String: Any] = [
            "name": name,
            "description": description,
            "icon": icon
        ]
        
        APIClient.shared.request(
            endpoint: APIConfig.Categories.update(category.id),
            method: .put,
            body: body
        ) { (result: Result<ApiResponse<Category>, Error>) in
            isLoading = false
            
            switch result {
            case .success:
                onUpdate()
                presentationMode.wrappedValue.dismiss()
                
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
            }
        }
    }
}
