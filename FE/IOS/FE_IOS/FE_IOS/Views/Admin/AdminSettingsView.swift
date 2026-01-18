import SwiftUI

struct AdminSettingsView: View {
    @State private var navigateToLogin = false
    
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
                
                // Hidden navigation
                NavigationLink(destination: LoginView().navigationBarHidden(true), isActive: $navigateToLogin) {
                    EmptyView()
                }
            }
            .navigationTitle("Cài đặt")
        }
        .navigationViewStyle(StackNavigationViewStyle())
    }
    
    private func logout() {
        TokenManager.shared.clearAll()
        navigateToLogin = true
    }
}

struct AdminSettingsView_Previews: PreviewProvider {
    static var previews: some View {
        AdminSettingsView()
    }
}
