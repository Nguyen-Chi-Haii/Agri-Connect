import Foundation
import Security

import Combine

// MARK: - Token Manager
class TokenManager: ObservableObject {
    static let shared = TokenManager()
    
    private let accessTokenKey = "agriconnect_access_token"
    private let refreshTokenKey = "agriconnect_refresh_token"
    private let userIdKey = "agriconnect_user_id"
    private let userNameKey = "agriconnect_user_name"
    private let userRoleKey = "agriconnect_user_role"
    
    private init() {
        self.accessToken = KeychainHelper.get(key: accessTokenKey)
    }
    
    // MARK: - Token Properties
    @Published var accessToken: String? {
        didSet {
            if let value = accessToken {
                KeychainHelper.save(key: accessTokenKey, value: value)
            } else {
                KeychainHelper.delete(key: accessTokenKey)
            }
        }
    }
    
    var refreshToken: String? {
        get { KeychainHelper.get(key: refreshTokenKey) }
        set {
            if let value = newValue {
                KeychainHelper.save(key: refreshTokenKey, value: value)
            } else {
                KeychainHelper.delete(key: refreshTokenKey)
            }
        }
    }
    
    // MARK: - User Info
    var userId: String? {
        get { UserDefaults.standard.string(forKey: userIdKey) }
        set { UserDefaults.standard.set(newValue, forKey: userIdKey) }
    }
    
    var userName: String? {
        get { UserDefaults.standard.string(forKey: userNameKey) }
        set { UserDefaults.standard.set(newValue, forKey: userNameKey) }
    }
    
    var userRole: String? {
        get { UserDefaults.standard.string(forKey: userRoleKey) }
        set { UserDefaults.standard.set(newValue, forKey: userRoleKey) }
    }
    
    var isLoggedIn: Bool {
        return accessToken != nil
    }
    
    // MARK: - Methods
    func saveTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }
    
    func saveUserInfo(id: String, name: String, role: String) {
        userId = id
        userName = name
        userRole = role
    }
    
    func clearAll() {
        objectWillChange.send()
        accessToken = nil
        refreshToken = nil
        userId = nil
        userName = nil
        userRole = nil
    }
}

// MARK: - Keychain Helper
class KeychainHelper {
    static func save(key: String, value: String) {
        guard let data = value.data(using: .utf8) else { return }
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data
        ]
        
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }
    
    static func get(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var result: AnyObject?
        SecItemCopyMatching(query as CFDictionary, &result)
        
        guard let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    static func delete(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}
