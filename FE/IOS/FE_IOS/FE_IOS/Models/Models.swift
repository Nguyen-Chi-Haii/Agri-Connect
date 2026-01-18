import Foundation
import SwiftUI

// MARK: - Color Hex Extension
extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let a, r, g, b: UInt64
        switch hex.count {
        case 6:
            (a, r, g, b) = (255, int >> 16, int >> 8 & 0xFF, int & 0xFF)
        default:
            (a, r, g, b) = (255, 0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: Double(a) / 255
        )
    }
}


// MARK: - Generic API Response
struct ApiResponse<T: Decodable>: Decodable {
    let success: Bool
    let message: String?
    let data: T?
    let timestamp: String?
}

// MARK: - Auth Models
struct LoginRequest: Encodable {
    let username: String
    let password: String
}

struct RegisterRequest: Encodable {
    let username: String
    let phone: String?
    let password: String
    let fullName: String
    let address: String?
    let role: String // FARMER or TRADER
}

struct JwtResponse: Decodable {
    let accessToken: String
    let refreshToken: String
    let tokenType: String?
    let userId: String
    let fullName: String
    let role: String
}

struct TokenRefreshRequest: Encodable {
    let refreshToken: String
}

// MARK: - User Models
// MARK: - User Models
struct UserProfile: Decodable, Identifiable {
    let id: String
    let username: String?
    let phone: String?
    let fullName: String
    let avatar: String?
    let address: String?
    let role: String
    let verified: Bool?
    let kycStatus: String?
    let createdAt: String?
    let kyc: KycInfo?
}

struct KycInfo: Decodable {
    let id: String?
    let status: String?
    let kycType: String?
    let idNumber: String?
    let idFrontImage: String?
    let idBackImage: String?
    let taxCode: String?
    let companyName: String?
    let businessLicense: String?
    let reason: String?
}

struct KycSubmissionRequest: Encodable {
    let kycType: String // "CCCD" or "TAX_CODE"
    
    // FARMER - CCCD
    let idNumber: String?
    let idFrontImage: String?
    let idBackImage: String?
    
    // TRADER - Tax Code
    let taxCode: String?
    let companyName: String?
    let businessLicense: String?
}

// MARK: - Post Models
struct Post: Decodable, Identifiable {
    let id: String
    let userId: String?
    let authorName: String?
    let categoryId: String?
    let categoryName: String?
    let title: String
    let description: String?
    let price: Double?
    let unit: String?
    let quantity: Double?
    let images: [String]?
    let province: String?
    let district: String?
    let status: String?
    let createdAt: String?
}

struct CreatePostRequest: Encodable {
    let categoryId: String
    let title: String
    let description: String
    let price: Double
    let unit: String
    let quantity: Double
    let province: String?
    let district: String?
    let images: [String]?
}

// MARK: - Category Model
struct Category: Decodable, Identifiable {
    let id: String
    let name: String
    let description: String?
    let icon: String?
}

// MARK: - Market Price Model
struct MarketPrice: Decodable, Identifiable {
    let id: String
    let productName: String
    let price: Double
    let unit: String
    let province: String?
    let updatedAt: String?
}

// MARK: - Chat Models
struct Conversation: Decodable, Identifiable {
    let id: String
    let participantIds: [String]?
    let participantNames: [String]?
    let lastMessage: String?
    let lastMessageTime: String?
    let unreadCount: Int?
}

struct Message: Decodable, Identifiable {
    let id: String
    let conversationId: String?
    let senderId: String
    let content: String
    let read: Bool?
    let createdAt: String?
}

// MARK: - Paged Response
// MARK: - Paged Response
struct PagedResponse<T: Decodable>: Decodable {
    let content: [T]
    let page: Int
    let size: Int
    let totalElements: Int
    let totalPages: Int
    
    enum CodingKeys: String, CodingKey {
        case content
        case page = "currentPage"
        case size
        case totalElements
        case totalPages
    }
}
