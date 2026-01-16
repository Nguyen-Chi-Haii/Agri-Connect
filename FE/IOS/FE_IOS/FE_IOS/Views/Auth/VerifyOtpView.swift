import SwiftUI

struct VerifyOtpView: View {
    let phone: String
    
    @Environment(\.presentationMode) var presentationMode
    @State private var otpCode = ""
    @State private var isLoading = false
    @State private var errorMessage = ""
    @State private var showError = false
    @State private var timeRemaining = 60
    @State private var canResend = false
    
    let timer = Timer.publish(every: 1, on: .main, in: .common).autoconnect()
    
    var body: some View {
        ZStack {
            Color(.systemBackground)
                .ignoresSafeArea()
            
            VStack(spacing: 32) {
                Spacer()
                
                // Icon
                Image(systemName: "lock.shield.fill")
                    .font(.system(size: 80))
                    .foregroundColor(Color(hex: "#2E7D32"))
                
                // Title
                VStack(spacing: 8) {
                    Text("Xác thực OTP")
                        .font(.title)
                        .fontWeight(.bold)
                    
                    Text("Mã xác thực đã được gửi đến")
                        .foregroundColor(.gray)
                    
                    Text(phone)
                        .fontWeight(.semibold)
                        .foregroundColor(Color(hex: "#2E7D32"))
                }
                
                // OTP Input
                HStack(spacing: 12) {
                    ForEach(0..<6, id: \.self) { index in
                        OTPDigitBox(
                            digit: getDigit(at: index),
                            isFocused: index == otpCode.count
                        )
                    }
                }
                .padding(.horizontal)
                
                // Hidden TextField for input
                TextField("", text: $otpCode)
                    .keyboardType(.numberPad)
                    .frame(width: 1, height: 1)
                    .opacity(0.01)
                    .onChange(of: otpCode) { newValue in
                        if newValue.count > 6 {
                            otpCode = String(newValue.prefix(6))
                        }
                        if newValue.count == 6 {
                            verifyOtp()
                        }
                    }
                
                // Timer / Resend
                if canResend {
                    Button("Gửi lại mã") {
                        resendOtp()
                    }
                    .foregroundColor(Color(hex: "#2E7D32"))
                } else {
                    Text("Gửi lại mã sau \(timeRemaining)s")
                        .foregroundColor(.gray)
                }
                
                // Verify Button
                Button(action: verifyOtp) {
                    if isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("Xác nhận")
                            .fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity)
                .frame(height: 50)
                .background(otpCode.count == 6 ? Color(hex: "#2E7D32") : Color.gray)
                .foregroundColor(.white)
                .cornerRadius(12)
                .padding(.horizontal, 24)
                .disabled(otpCode.count < 6 || isLoading)
                
                Spacer()
            }
        }
        .onReceive(timer) { _ in
            if timeRemaining > 0 {
                timeRemaining -= 1
            } else {
                canResend = true
            }
        }
        .alert(isPresented: $showError) {
            Alert(title: Text("Lỗi"), message: Text(errorMessage), dismissButton: .default(Text("OK")))
        }
    }
    
    private func getDigit(at index: Int) -> String {
        guard index < otpCode.count else { return "" }
        let startIndex = otpCode.index(otpCode.startIndex, offsetBy: index)
        return String(otpCode[startIndex])
    }
    
    private func verifyOtp() {
        guard otpCode.count == 6 else { return }
        
        isLoading = true
        
        // TODO: API call to verify OTP
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            isLoading = false
            // Navigate to next screen
        }
    }
    
    private func resendOtp() {
        timeRemaining = 60
        canResend = false
        // TODO: API call to resend OTP
    }
}

// MARK: - OTP Digit Box
struct OTPDigitBox: View {
    let digit: String
    let isFocused: Bool
    
    var body: some View {
        Text(digit)
            .font(.title)
            .fontWeight(.bold)
            .frame(width: 45, height: 55)
            .background(Color(.systemGray6))
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        isFocused ? Color(hex: "#2E7D32") : Color.clear,
                        lineWidth: 2
                    )
            )
    }
}

struct VerifyOtpView_Previews: PreviewProvider {
    static var previews: some View {
        VerifyOtpView(phone: "0912345678")
    }
}
