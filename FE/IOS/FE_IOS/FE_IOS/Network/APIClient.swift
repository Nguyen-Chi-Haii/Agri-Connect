import Foundation

// MARK: - API Configuration
struct APIConfig {
    static let baseURL = "http://localhost:8080/api"
    
    // Endpoints
    struct Auth {
        static let login = "/auth/login"
        static let register = "/auth/register"
        static let refreshToken = "/auth/refresh-token"
        static let logout = "/auth/logout"
    }
    
    struct Users {
        static let profile = "/users/profile"
        static let kycSubmit = "/users/kyc/submit"
    }
    
    struct Posts {
        static let list = "/posts"
        static let approved = "/posts/approved"
        static let myPosts = "/posts/my-posts"
        static let search = "/posts/search"
    }
    
    struct Categories {
        static let list = "/categories"
    }
    
    struct MarketPrices {
        static let list = "/market-prices"
    }
    
    struct Chat {
        static let conversations = "/chat/conversations"
        static let messages = "/chat/messages"
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
                DispatchQueue.main.async {
                    completion(.failure(error))
                }
                return
            }
            
            guard let data = data else {
                DispatchQueue.main.async {
                    completion(.failure(APIError.noData))
                }
                return
            }
            
            do {
                let decoder = JSONDecoder()
                let apiResponse = try decoder.decode(ApiResponse<T>.self, from: data)
                DispatchQueue.main.async {
                    completion(.success(apiResponse))
                }
            } catch {
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
