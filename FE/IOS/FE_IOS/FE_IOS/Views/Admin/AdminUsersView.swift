import SwiftUI

struct AdminUsersView: View {
    @State private var users: [UserProfile] = []
    @State private var isLoading = false
    @State private var searchText = ""
    @State private var selectedRole: String = "ALL" // ALL, FARMER, TRADER
    @State private var selectedKycStatus: String = "ALL" // ALL, VERIFIED, PENDING
    
    let roleFilters = [
        ("ALL", "Tất cả"),
        ("FARMER", "Nông dân"),
        ("TRADER", "Thương lái")
    ]
    
    let kycFilters = [
        ("ALL", "Tất cả KYC"),
        ("PENDING", "Chờ duyệt"),
        ("VERIFIED", "Đã duyệt")
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
            // Filters
            VStack(spacing: 8) {
                // Role Filters
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(roleFilters, id: \.0) { filter in
                            Button {
                                selectedRole = filter.0
                                loadUsers()
                            } label: {
                                Text(filter.1)
                                    .font(.subheadline)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(
                                        selectedRole == filter.0
                                        ? Color(hex: "#2E7D32")
                                        : Color(.systemGray6)
                                    )
                                    .foregroundColor(
                                        selectedRole == filter.0
                                        ? .white
                                        : .primary
                                    )
                                    .cornerRadius(20)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
                
                // KYC Filters
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(kycFilters, id: \.0) { filter in
                            Button {
                                selectedKycStatus = filter.0
                                loadUsers()
                            } label: {
                                Text(filter.1)
                                    .font(.subheadline)
                                    .padding(.horizontal, 16)
                                    .padding(.vertical, 8)
                                    .background(
                                        selectedKycStatus == filter.0
                                        ? Color.orange
                                        : Color(.systemGray6)
                                    )
                                    .foregroundColor(
                                        selectedKycStatus == filter.0
                                        ? .white
                                        : .primary
                                    )
                                    .cornerRadius(20)
                            }
                        }
                    }
                    .padding(.horizontal)
                }
            }
            .padding(.bottom, 8)
            
            Divider()
            
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
            } else {
                List(users) { user in
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
        
        var params: [String: String] = [:]
        if !searchText.isEmpty { params["search"] = searchText }
        if selectedRole != "ALL" { params["role"] = selectedRole }
        if selectedKycStatus != "ALL" { params["kycStatus"] = selectedKycStatus }
        
        var endpoint = APIConfig.Users.list
        if !params.isEmpty {
            let queryString = params.map { "\($0.key)=\($0.value)" }.joined(separator: "&")
            endpoint += "?\(queryString)"
        }
        
        // DEBUG
        print("📡 [AdminUsers] Query: \(endpoint)")
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<[UserProfile]>, Error>) in
            isLoading = false
            switch result {
            case .success(let response):
                if let data = response.data {
                    users = data
                }
            case .failure(let error):
                print("Error loading users: \(error)")
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
                .onTapGesture {
                    showKycDetail = true
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
        .sheet(isPresented: $showKycDetail) {
            KycDetailView(user: user)
        }
        .background(
            TextFieldAlert(
                isPresented: $showReasonInput,
                title: "Từ chối KYC",
                text: $rejectionReason,
                placeholder: "Nhập lý do",
                action: rejectKyc
            )
        )
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
        .disabled(isProcessing)
        .opacity(isProcessing ? 0.6 : 1.0)
    }
    
    @State private var showKycDetail = false
    @State private var showReasonInput = false
    @State private var rejectionReason = ""
    
    private func actionButtons() -> [ActionSheet.Button] {
        var buttons: [ActionSheet.Button] = []
        
        buttons.append(.default(Text("Xem chi tiết hồ sơ")) { showKycDetail = true })
        
        if user.kycStatus == "PENDING" {
            buttons.append(.default(Text("Xác minh KYC")) { verifyKyc() })
            buttons.append(.destructive(Text("Từ chối KYC")) { 
                rejectionReason = ""
                showReasonInput = true 
            })
        }
        
        // Lock/Unlock Logic (Assuming API supports it)
        // Check if user is active or locked? Model needs 'active' or 'locked' flag.
        // Assuming user.active is implied or we just show both for now.
        // Android has separate Lock/Unlock callbacks.
        buttons.append(.destructive(Text("Khóa tài khoản")) { lockUser() })
        buttons.append(.default(Text("Mở khóa tài khoản")) { unlockUser() })
        
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
            endpoint: APIConfig.Users.verifyKyc(user.id),
            method: .put
        ) { (result: Result<ApiResponse<Void>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func rejectKyc() {
        isProcessing = true
        let reasonParam = rejectionReason.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        APIClient.shared.request(
            endpoint: APIConfig.Users.rejectKyc(user.id) + "?reason=\(reasonParam)",
            method: .put
        ) { (result: Result<ApiResponse<Void>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func lockUser() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: APIConfig.Users.lock(user.id),
            method: .put
        ) { (result: Result<ApiResponse<Void>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func unlockUser() {
        isProcessing = true
        APIClient.shared.request(
            endpoint: APIConfig.Users.unlock(user.id),
            method: .put
        ) { (result: Result<ApiResponse<Void>, Error>) in
            isProcessing = false
            handleResult(result)
        }
    }
    
    private func handleResult(_ result: Result<ApiResponse<Void>, Error>) {
        switch result {
        case .success(let response):
            if response.success {
                onUpdate()
            } else {
                errorMessage = response.message ?? "Thao tác thất bại"
                showError = true
            }
        case .failure(let error):
            errorMessage = "Lỗi: \(error.localizedDescription)"
            showError = true
        }
    }
}

// MARK: - KYC Detail View
struct KycDetailView: View {
    let user: UserProfile
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        NavigationView {
            Form {
                Section(header: Text("Thông tin cá nhân")) {
                    DetailRow(label: "Họ tên", value: user.fullName)
                    DetailRow(label: "Số điện thoại", value: user.phone ?? "Chưa cập nhật")
                    DetailRow(label: "Địa chỉ", value: user.address ?? "Chưa cập nhật")
                    DetailRow(label: "Vai trò", value: user.role)
                }
                
                if let kyc = user.kyc {
                    Section(header: Text("Thông tin KYC")) {
                        DetailRow(label: "Trạng thái", value: kyc.status ?? "")
                        DetailRow(label: "Loại giấy tờ", value: kyc.kycType ?? "CCCD")
                        
                        if let idNumber = kyc.idNumber {
                            DetailRow(label: "Số CCCD", value: idNumber)
                        }
                        
                        if let taxCode = kyc.taxCode {
                            DetailRow(label: "Mã số thuế", value: taxCode)
                        }
                    }
                    
                    Section(header: Text("Hình ảnh xác minh")) {
                        if let front = kyc.idFrontImage {
                            KycImage(url: front, title: "Mặt trước")
                        }
                        if let back = kyc.idBackImage {
                            KycImage(url: back, title: "Mặt sau")
                        }
                        if let license = kyc.businessLicense {
                             KycImage(url: license, title: "Giấy phép kinh doanh")
                        }
                    }
                } else {
                    Text("Người dùng chưa gửi thông tin KYC")
                        .foregroundColor(.gray)
                        .padding()
                }
            }
            .navigationTitle("Chi tiết hồ sơ")
            .navigationBarItems(trailing: Button("Đóng") {
                presentationMode.wrappedValue.dismiss()
            })
        }
    }
}

struct DetailRow: View {
    let label: String
    let value: String
    
    var body: some View {
        HStack {
            Text(label).foregroundColor(.gray)
            Spacer()
            Text(value)
        }
    }
}

struct KycImage: View {
    let url: String
    let title: String
    @State private var isFullScreen = false
    
    var body: some View {
        VStack(alignment: .leading) {
            Text(title).font(.caption).foregroundColor(.gray)
            
            AsyncImage(url: URL(string: url)) { phase in
                switch phase {
                case .empty:
                    HStack {
                        Spacer()
                        ProgressView()
                        Spacer()
                    }
                    .frame(height: 150)
                    .background(Color.gray.opacity(0.1))
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 200)
                        .clipped()
                        .cornerRadius(8)
                        .onTapGesture {
                            isFullScreen = true
                        }
                case .failure:
                    HStack {
                        Spacer()
                        Image(systemName: "photo")
                            .foregroundColor(.gray)
                        Spacer()
                    }
                    .frame(height: 150)
                    .background(Color.gray.opacity(0.1))
                @unknown default:
                    EmptyView()
                }
            }
        }
        .padding(.vertical, 4)
        .fullScreenCover(isPresented: $isFullScreen) {
            FullScreenImageView(imageUrl: url, isPresented: $isFullScreen)
        }
    }
}

struct FullScreenImageView: View {
    let imageUrl: String
    @Binding var isPresented: Bool
    
    var body: some View {
        ZStack {
            Color.black.ignoresSafeArea()
            
            AsyncImage(url: URL(string: imageUrl)) { image in
                image
                    .resizable()
                    .aspectRatio(contentMode: .fit)
            } placeholder: {
                ProgressView()
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
            }
            
            VStack {
                HStack {
                    Spacer()
                    Button(action: { isPresented = false }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.system(size: 30))
                            .foregroundColor(.white)
                            .padding()
                    }
                }
                Spacer()
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
