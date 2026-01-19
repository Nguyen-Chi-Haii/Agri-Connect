import Foundation

// MARK: - API Configuration
struct APIConfig {
    static var baseURL: String {
        return Configuration.shared.baseURL
    }
    
    // Endpoints
    struct Auth {
        static let login = "/auth/login"
        static let register = "/auth/register"
        static let refreshToken = "/auth/refresh-token"
        static let logout = "/auth/logout"
    }
    
    struct Admin {
        static let dashboardStats = "/admin/dashboard"
    }
    
    struct Users {
        static let list = "/users"
        static let profile = "/users/profile"
        static let kycSubmit = "/users/kyc/submit"
        
        static func verifyKyc(_ id: String) -> String { return "/users/\(id)/kyc/verify" }
        static func rejectKyc(_ id: String) -> String { return "/users/\(id)/kyc/reject" }
        static func lock(_ id: String) -> String { return "/users/\(id)/lock" }
        static func unlock(_ id: String) -> String { return "/users/\(id)/unlock" }
    }
    
    struct Posts {
        static let list = "/posts"
        static let approved = "/posts/approved"
        static let myPosts = "/posts/my-posts"
        static let search = "/posts"
        
        static func detail(_ id: String) -> String { return "/posts/\(id)" }
        static func update(_ id: String) -> String { return "/posts/\(id)" }
        static func approve(_ id: String) -> String { return "/posts/\(id)/approve" }
        static func reject(_ id: String) -> String { return "/posts/\(id)/reject" }
        static func delete(_ id: String) -> String { return "/posts/\(id)" }
        static func close(_ id: String) -> String { return "/posts/\(id)/close" }
    }
    
    struct Categories {
        static let list = "/categories"
        static func detail(_ id: String) -> String { return "/categories/\(id)" }
        static func update(_ id: String) -> String { return "/categories/\(id)" }
    }
    
    struct MarketPrices {
        static let list = "/market-prices"
        static func byCategory(_ categoryId: String) -> String {
            return "/market-prices/category/\(categoryId)"
        }
    }
    
    struct Statistics {
        static let summary = "/statistics/summary"
    }
    
    struct Chat {
        static let conversations = "/chat/conversations"
        static let messages = "/chat/messages"
        
        static func createConversation(_ userId: String) -> String {
            return "/chat/conversations/\(userId)"
        }
        
        static func markRead(_ conversationId: String) -> String {
            return "/chat/conversations/\(conversationId)/read"
        }
    }
    
    struct Upload {
        static let single = "/upload"
        static let multiple = "/upload/multiple"
    }
}

// MARK: - APIClient Extension for Image Upload
extension APIClient {
    func uploadImage(
        _ image: UIImage,
        completion: @escaping (Result<String, Error>) -> Void
    ) {
        guard let imageData = image.jpegData(compressionQuality: 0.7) else {
            completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "Cannot convert image"])))
            return
        }
        
        let boundary = "Boundary-\(UUID().uuidString)"
        let url = URL(string: "\(Configuration.shared.baseURL)\(APIConfig.Upload.single)")!
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("multipart/form-data; boundary=\(boundary)", forHTTPHeaderField: "Content-Type")
        
        // Add token if available
        if let token = TokenManager.shared.accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        var body = Data()
        
        // Add file part
        body.append("--\(boundary)\r\n".data(using: .utf8)!)
        body.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n".data(using: .utf8)!)
        body.append("Content-Type: image/jpeg\r\n\r\n".data(using: .utf8)!)
        body.append(imageData)
        body.append("\r\n".data(using: .utf8)!)
        body.append("--\(boundary)--\r\n".data(using: .utf8)!)
        
        request.httpBody = body
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            DispatchQueue.main.async {
                if let error = error {
                    completion(.failure(error))
                    return
                }
                
                guard let data = data else {
                    completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "No data"])))
                    return
                }
                
                do {
                    let decoder = JSONDecoder()
                    let apiResponse = try decoder.decode(ApiResponse<String>.self, from: data)
                    
                    if let imageUrl = apiResponse.data {
                        completion(.success(imageUrl))
                    } else {
                        completion(.failure(NSError(domain: "", code: -1, userInfo: [NSLocalizedDescriptionKey: "No URL in response"])))
                    }
                } catch {
                    completion(.failure(error))
                }
            }
        }.resume()
    }
}

// MARK: - Private Helpers
fileprivate struct AnyEncodable: Encodable {
    let value: Encodable
    func encode(to encoder: Encoder) throws {
        try value.encode(to: encoder)
    }
}

// MARK: - API Client
class APIClient {
    static let shared = APIClient()
    
    private init() {}
    
    // MARK: - Generic Request
    func request<T: Decodable>(
        endpoint: String,
        method: HTTPMethod = .get,
        body: Encodable? = nil,
        completion: @escaping (Result<ApiResponse<T>, Error>) -> Void
    ) {
        guard let url = URL(string: APIConfig.baseURL + endpoint) else {
            completion(.failure(APIError.invalidURL))
            return
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method.rawValue
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        
        // Add auth token if available
        if let token = TokenManager.shared.accessToken {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        // Add body if present
        if let body = body {
            request.httpBody = try? JSONEncoder().encode(AnyEncodable(value: body))
        }
        
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                DispatchQueue.main.async { completion(.failure(error)) }
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse else {
                DispatchQueue.main.async { completion(.failure(APIError.serverError("Invalid response"))) }
                return
            }
            
            // Check for 401 Unauthorized
            if httpResponse.statusCode == 401 {
                DispatchQueue.main.async { completion(.failure(APIError.unauthorized)) }
                return
            }
            
            // Check for other error codes
            guard (200...299).contains(httpResponse.statusCode) else {
                DispatchQueue.main.async { completion(.failure(APIError.serverError("HTTP \(httpResponse.statusCode)"))) }
                return
            }
            
            guard let data = data else {
                DispatchQueue.main.async { completion(.failure(APIError.noData)) }
                return
            }
            
            // Handle Empty Body for specific cases (like Void)
            if data.isEmpty {
                // If T is Void or String, maybe we can pass? But ApiResponse usually has structure.
                // Assuming ApiResponse always sends valid JSON even for Void (success: true).
                // If really empty, it's an error for JSONDecoder.
                 DispatchQueue.main.async { completion(.failure(APIError.noData)) }
                 return
            }
            
            do {
                let decoder = JSONDecoder()
                let apiResponse = try decoder.decode(ApiResponse<T>.self, from: data)
                DispatchQueue.main.async {
                    completion(.success(apiResponse))
                }
            } catch {
                if let dataString = String(data: data, encoding: .utf8) {
                    print("❌ JSON Decoding Error: \(error)")
                    print("📄 Raw Response: \(dataString)")
                }
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
            }
        }.resume()
    }

}

// MARK: - HTTP Method
enum HTTPMethod: String {
    case get = "GET"
    case post = "POST"
    case put = "PUT"
    case delete = "DELETE"
}

// MARK: - API Errors
enum APIError: Error, LocalizedError {
    case invalidURL
    case noData
    case unauthorized
    case serverError(String)
    
    var errorDescription: String? {
        switch self {
        case .invalidURL: return "Invalid URL"
        case .noData: return "No data received"
        case .unauthorized: return "Unauthorized"
        case .serverError(let msg): return msg
        }
    }
}
