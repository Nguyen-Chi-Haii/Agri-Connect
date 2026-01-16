import SwiftUI

struct RoleSelectionView: View {
    @Environment(\.presentationMode) var presentationMode
    
    var body: some View {
        ZStack {
            // Background
            LinearGradient(
                colors: [Color(hex: "#E8F5E9"), Color.white],
                startPoint: .top,
                endPoint: .bottom
            )
            .ignoresSafeArea()
            
            VStack(spacing: 32) {
                Spacer()
                
                // Logo
                Image(systemName: "leaf.circle.fill")
                    .font(.system(size: 80))
                    .foregroundColor(Color(hex: "#2E7D32"))
                
                // Title
                VStack(spacing: 8) {
                    Text("Chọn vai trò của bạn")
                        .font(.title2)
                        .fontWeight(.bold)
                    
                    Text("Bạn là ai trong chuỗi cung ứng nông sản?")
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                }
                
                // Role Cards
                VStack(spacing: 16) {
                    NavigationLink(destination: CreateAccountView(role: "FARMER")) {
                        RoleCard(
                            icon: "🌾",
                            title: "Nông dân",
                            description: "Tôi là người trồng trọt, chăn nuôi và muốn bán sản phẩm",
                            color: Color(hex: "#4CAF50")
                        )
                    }
                    
                    NavigationLink(destination: CreateAccountView(role: "TRADER")) {
                        RoleCard(
                            icon: "🚛",
                            title: "Thương lái",
                            description: "Tôi là người thu mua nông sản từ nông dân",
                            color: Color(hex: "#FF9800")
                        )
                    }
                }
                .padding(.horizontal, 24)
                
                Spacer()
                
                // Back to login
                HStack {
                    Text("Đã có tài khoản?")
                        .foregroundColor(.gray)
                    Button(action: {
                        presentationMode.wrappedValue.dismiss()
                    }) {
                        Text("Đăng nhập")
                            .fontWeight(.semibold)
                            .foregroundColor(Color(hex: "#2E7D32"))
                    }
                }
                .padding(.bottom, 32)
            }
        }
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Role Card Component
struct RoleCard: View {
    let icon: String
    let title: String
    let description: String
    let color: Color
    
    var body: some View {
        HStack(spacing: 16) {
            Text(icon)
                .font(.system(size: 40))
            
            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.primary)
                
                Text(description)
                    .font(.caption)
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.leading)
            }
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: color.opacity(0.2), radius: 8, x: 0, y: 4)
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(color.opacity(0.3), lineWidth: 1)
        )
    }
}

struct RoleSelectionView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            RoleSelectionView()
        }
    }
}
