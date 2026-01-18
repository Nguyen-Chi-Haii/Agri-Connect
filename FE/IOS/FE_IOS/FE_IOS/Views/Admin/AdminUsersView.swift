import SwiftUI

struct AdminUsersView: View {
    @State private var users: [UserProfile] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedFilter = "all"
    
    let filters = [
        ("all", "Tất cả"),
        ("FARMER", "Nông dân"),
        ("TRADER", "Thương lái"),
        ("PENDING", "Chờ KYC")
    ]
    
    var filteredUsers: [UserProfile] {
        var result = users
        
        if !searchText.isEmpty {
            result = result.filter {
                $0.fullName.localizedCaseInsensitiveContains(searchText) ||
                ($0.phone ?? "").contains(searchText)
            }
        }
        
        if selectedFilter != "all" {
            if selectedFilter == "PENDING" {
                result = result.filter { $0.kycStatus == "PENDING" }
            } else {
                result = result.filter { $0.role == selectedFilter }
            }
        }
        
        return result
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("Tìm người dùng...", text: $searchText)
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
            .padding()
            
            // Filters
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(filters, id: \.0) { filter in
                        Button {
                            selectedFilter = filter.0
                        } label: {
                            Text(filter.1)
                                .font(.subheadline)
                                .padding(.horizontal, 16)
                                .padding(.vertical, 8)
                                .background(
                                    selectedFilter == filter.0
                                    ? Color(hex: "#2E7D32")
                                    : Color(.systemGray6)
                                )
                                .foregroundColor(
                                    selectedFilter == filter.0
                                    ? .white
                                    : .primary
                                )
                                .cornerRadius(20)
                        }
                    }
                }
                .padding(.horizontal)
            }
            
            Divider()
                .padding(.top, 8)
            
            // Users List
            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if filteredUsers.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "person.slash")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Không tìm thấy người dùng")
                        .foregroundColor(.gray)
                }
                Spacer()
            } else {
                List(filteredUsers, id: \.id) { user in
                    AdminUserRow(user: user) {
                        loadUsers()
                    }
                }
                .listStyle(PlainListStyle())
            }
        }
        .navigationTitle("Quản lý người dùng")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadUsers()
        }
    }
    
    private func loadUsers() {
        isLoading = true
        
        APIClient.shared.request(
            endpoint: "/users",
            method: .get
        ) { (result: Result<ApiResponse<[UserProfile]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                users = data
            }
        }
    }
}

// MARK: - Admin User Row
struct AdminUserRow: View {
    let user: UserProfile
    let onUpdate: () -> Void
    
    @State private var showActionSheet = false
    @State private var isProcessing = false
    @State private var errorMessage = ""
    @State private var showError = false
    
    var body: some View {
        HStack(spacing: 12) {
            // Avatar
            ZStack {
                Circle()
                    .fill(Color(hex: "#E8F5E9"))
                    .frame(width: 50, height: 50)
                
                Text(String(user.fullName.prefix(1)))
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundColor(Color(hex: "#2E7D32"))
            }
            
            // Info
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(user.fullName)
                        .font(.headline)
                    
                    if user.verified == true {
                        Image(systemName: "checkmark.seal.fill")
                            .font(.caption)
                            .foregroundColor(.blue)
                    }
                }
                
                Text(roleText(user.role))
                    .font(.caption)
                    .foregroundColor(.gray)
                
                if let kycStatus = user.kycStatus {
                    KYCBadge(status: kycStatus)
                }
            }
            
            Spacer()
            
            // Actions
            Button {
                showActionSheet = true
            } label: {
                Image(systemName: "ellipsis.circle")
                    .font(.title2)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 8)
        .actionSheet(isPresented: $showActionSheet) {
            ActionSheet(
                title: Text("Hành động"),
                buttons: actionButtons()
            )
        }
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .disabled(isProcessing)
        .opacity(isProcessing ? 0.6 : 1.0)
    }
    
    private func actionButtons() -> [ActionSheet.Button] {
        var buttons: [ActionSheet.Button] = []
        
        if user.kycStatus == "PENDING" {
            buttons.append(.default(Text("Xác minh KYC")) { verifyKyc() })
            buttons.append(.destructive(Text("Từ chối KYC")) { rejectKyc() })
        }
        
        buttons.append(.destructive(Text("Khóa tài khoản")) { lockUser() })
        buttons.append(.cancel(Text("Hủy")))
        
        return buttons
    }
    
    private func roleText(_ role: String) -> String {
        switch role {
        case "FARMER": return "🌾 Nông dân"
        case "TRADER": return "🚛 Thương lái"
        case "ADMIN": return "👨‍💼 Admin"
        default: return role
        }
    }
    
    private func verifyKyc() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: "/users/\(user.id)/kyc/verify",
            method: .put,
            body: nil as String?
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            switch result {
            case .success(let response):
                if response.success {
                    onUpdate()
                } else {
                    errorMessage = response.message ?? "Xác minh thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
    
    private func rejectKyc() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: "/users/\(user.id)/kyc/reject",
            method: .put,
            body: nil as String?
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            switch result {
            case .success(let response):
                if response.success {
                    onUpdate()
                } else {
                    errorMessage = response.message ?? "Từ chối thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
    
    private func lockUser() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: "/users/\(user.id)/lock",
            method: .put,
            body: nil as String?
        ) { (result: Result<ApiResponse<String>, Error>) in
            isProcessing = false
            switch result {
            case .success(let response):
                if response.success {
                    onUpdate()
                } else {
                    errorMessage = response.message ?? "Khóa tài khoản thất bại"
                    showError = true
                }
            case .failure(let error):
                errorMessage = "Lỗi: \(error.localizedDescription)"
                showError = true
            }
        }
    }
}

// MARK: - KYC Badge
struct KYCBadge: View {
    let status: String
    
    var color: Color {
        switch status {
        case "VERIFIED": return .green
        case "PENDING": return .orange
        case "REJECTED": return .red
        default: return .gray
        }
    }
    
    var text: String {
        switch status {
        case "VERIFIED": return "Đã xác minh"
        case "PENDING": return "Chờ duyệt"
        case "REJECTED": return "Từ chối"
        default: return status
        }
    }
    
    var body: some View {
        Text(text)
            .font(.caption2)
            .padding(.horizontal, 8)
            .padding(.vertical, 2)
            .background(color.opacity(0.2))
            .foregroundColor(color)
            .cornerRadius(4)
    }
}

struct AdminUsersView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            AdminUsersView()
        }
    }
}
