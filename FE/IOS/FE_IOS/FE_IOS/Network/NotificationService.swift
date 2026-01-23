import Foundation

class NotificationService {
    static let shared = NotificationService()
    
    private init() {}
    
    func fetchNotifications(completion: @escaping (Result<[AppNotification], Error>) -> Void) {
        APIClient.shared.request(
            endpoint: APIConfig.Notifications.list,
            method: .get
        ) { (result: Result<ApiResponse<[AppNotification]>, Error>) in
            switch result {
            case .success(let response):
                completion(.success(response.data ?? []))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func getUnreadCount(completion: @escaping (Result<Int, Error>) -> Void) {
        // Backend returns Long/Int. swift expects Int usually ok.
        APIClient.shared.request(
            endpoint: APIConfig.Notifications.unreadCount,
            method: .get
        ) { (result: Result<ApiResponse<Int>, Error>) in
             switch result {
            case .success(let response):
                completion(.success(response.data ?? 0))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func markAsRead(_ notificationId: String, completion: @escaping (Result<Void, Error>) -> Void) {
        APIClient.shared.request(
            endpoint: APIConfig.Notifications.markRead(notificationId),
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in // Generic string/void
             switch result {
            case .success:
                completion(.success(()))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
    
    func markAllAsRead(completion: @escaping (Result<Void, Error>) -> Void) {
        APIClient.shared.request(
            endpoint: APIConfig.Notifications.readAll,
            method: .put
        ) { (result: Result<ApiResponse<String>, Error>) in
             switch result {
            case .success:
                completion(.success(()))
            case .failure(let error):
                completion(.failure(error))
            }
        }
    }
}
