import Foundation

enum APIEnvironment {
    case localhost
    case device(ip: String)
    case production
    
    var baseURL: String {
        switch self {
        case .localhost:
            return "http://localhost:8080/api"
        case .device(let ip):
            return "http://\(ip):8080/api"
        case .production:
            return "https://api.agriconnect.com/api" // Replace with actual prod URL
        }
    }
}

struct Configuration {
    static let shared = Configuration()
    
    // CHANGE THIS to switch environments
    // Example: .device(ip: "192.168.1.10") or .localhost
    let environment: APIEnvironment = .device(ip: "192.168.145.1")
    
    var baseURL: String {
        return environment.baseURL
    }
}
