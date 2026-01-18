import SwiftUI

struct AdminSettingsView: View {
    // No state needed for navigation, Root View handles it via TokenManager observation
    
    var body: some View {
        NavigationView {
            List {
                Section(header: Text("Tài khoản")) {
                    HStack {
                        Image(systemName: "person.circle.fill")
                        .font(.largeTitle)
                        .foregroundColor(.gray)
                        VStack(alignment: .leading) {
                            Text("Admin")
                                .font(.headline)
                            Text("Quản trị viên hệ thống")
                                .font(.caption)
                                .foregroundColor(.gray)
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                Section {
                    Button(action: logout) {
                        HStack {
                            Text("Đăng xuất")
                                .foregroundColor(.red)
                            Spacer()
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .foregroundColor(.red)
                        }
                    }
                }
            }
            .navigationTitle("Cài đặt")
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func logout() {
        print("👋 [AdminSettingsView] User tapped Logout. Calling TokenManager.clearAll()")
        // Just clear tokens. FE_IOSApp will detect change and switch Root View to LoginView automatically.
        TokenManager.shared.clearAll()
    }
}

struct AdminSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        AdminSettingsView()
    }
}
