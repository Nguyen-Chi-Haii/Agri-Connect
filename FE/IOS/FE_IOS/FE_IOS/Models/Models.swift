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

// MARK: - Dashboard Stats (Match AdminService Map)
struct DashboardStats: Decodable {
    let totalUsers: Int?
    let totalPosts: Int?
    let pendingPosts: Int?
    let approvedPosts: Int?
    let rejectedPosts: Int?
    let totalMessages: Int?
    let totalConversations: Int?
    let totalFeedbacks: Int?
    let pendingKyc: Int? // Not in backend yet, but required by View
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
    let active: Bool? // iOS name
    let verified: Bool?
    private let _kycStatus: String? 
    let kyc: KycInfo?
    let createdAt: String?
    
    var kycStatus: String? {
        return _kycStatus ?? kyc?.status
    }
    
    enum CodingKeys: String, CodingKey {
        case id, username, fullName, phone, avatar, address, role, verified, kyc, createdAt
        case active = "isActive" // Map JSON isActive -> active
        case _kycStatus = "kycStatus"
    }
}

struct KycInfo: Decodable {
    let id: String? // Unused/nil in Backend?
    let status: String?
    let kycType: String? // Backend doesn't have this?
    let idNumber: String?
    let idFrontImage: String?
    let idBackImage: String?
    let taxCode: String?
    let companyName: String? // Backend missing?
    let businessLicense: String? // Backend missing?
    let reason: String?
    
    enum CodingKeys: String, CodingKey {
        case id, status, kycType
        case idNumber = "cccd"
        case idFrontImage = "cccdFrontImage"
        case idBackImage = "cccdBackImage"
        case taxCode
        case companyName, businessLicense
        case reason = "rejectionReason"
    }
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
struct Location: Decodable {
    let province: String?
    let district: String?
    let ward: String?
    let address: String?
}

struct Post: Decodable, Identifiable {
    let id: String
    let sellerId: String?
    let sellerName: String?
    let categoryId: String?
    let categoryName: String?
    let title: String
    let description: String?
    let price: Double?
    let unit: String?
    let quantity: Double?
    let images: [String]?
    let location: Location?
    let status: String?
    let createdAt: String?
    let likeCount: Int?
    let commentCount: Int?
    let viewCount: Int?
    let isLiked: Bool?
    
    // Convenience Accessors
    var province: String? { location?.province }
    var district: String? { location?.district }
    var authorName: String? { sellerName } // Alias for backward compatibility if needed
}

struct PostInteractionResponse: Decodable {
    let likeCount: Int?
    let commentCount: Int?
    let isLiked: Bool?
}

struct CreateLocationRequest: Encodable {
    let province: String?
    let district: String?
}

struct CreatePostRequest: Encodable {
    let categoryId: String
    let title: String
    let description: String
    let price: Double
    let unit: String
    let quantity: Double
    let images: [String]?
    let location: CreateLocationRequest?
}

// MARK: - Category Model
struct Category: Decodable, Identifiable {
    let id: String
    let name: String
    let description: String?
    let icon: String?
    let parentId: String?
}

// MARK: - Market Price Model
struct MarketPrice: Decodable, Identifiable {
    let id: String
    let categoryId: String?
    let productName: String
    let date: String? // "yyyy-MM-dd"
    let avgPrice: Double
    let minPrice: Double
    let maxPrice: Double
    let postCount: Int?
    let categoryName: String? // Transient
    
    // Compatibility accessors for old code
    var price: Double { avgPrice }
    var unit: String { "kg" } // Backend doesn't return unit in MarketPrice entity yet
    var province: String? { nil } // Backend removed province from MarketPrice
    var updatedAt: String? { date }
}

// MARK: - Chat Models
struct LastMessage: Decodable {
    let content: String?
    let senderId: String?
    let type: String?
    let timestamp: String?
}

struct Conversation: Decodable, Identifiable {
    let id: String
    let participants: [String]?
    let participantName: String?
    let participantAvatar: String?
    let lastMessage: LastMessage?
    let updatedAt: String?
    let unreadCount: Int?
    
    // Convenience for UI
    var lastMessageContent: String? { lastMessage?.content }
}

struct Message: Decodable, Identifiable {
    let id: String
    let conversationId: String?
    let senderId: String
    let content: String
    let type: String? // TEXT, IMAGE, PRODUCT_CARD
    let images: [String]?
    let read: Bool?
    let createdAt: String?
    
    enum CodingKeys: String, CodingKey {
        case id, conversationId, senderId, content, type, images, createdAt
        case read // Backend uses "read" or "isRead" depending on Jackson. Assuming "read" for now due to Lombok @Data usually producing "read" for Boolean.
    }
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

// MARK: - Comment Model
struct Comment: Codable, Identifiable {
    let id: String
    let postId: String
    let userId: String
    let userName: String?
    let content: String
    let createdAt: String
}

// MARK: - Empty Response (for DELETE, etc.)
struct EmptyResponse: Codable {}
