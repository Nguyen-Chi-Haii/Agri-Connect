import Foundation
import CoreLocation
import Combine

class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    static let shared = LocationManager()
    
    private let manager = CLLocationManager()
    
    @Published var location: CLLocation?
    @Published var locationError: Error?
    @Published var isLoading = false
    @Published var addressComponents: (province: String, district: String)?
    
    override private init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
    }
    
    private var timeoutTimer: Timer?
    
    func requestLocation() {
        isLoading = true
        locationError = nil
        location = nil // Reset location to ensure we get a fresh one or fail
        addressComponents = nil // Reset address to ensure UI clears old data
        
        // Invalidate existing timer
        timeoutTimer?.invalidate()
        
        // Start new timeout timer (15s)
        timeoutTimer = Timer.scheduledTimer(withTimeInterval: 15.0, repeats: false) { [weak self] _ in
            guard let self = self else { return }
            if self.isLoading {
                print("⚠️ [LocationManager] Timeout reached. Stopping updates.")
                self.manager.stopUpdatingLocation()
                
                DispatchQueue.main.async {
                    self.isLoading = false
                    self.locationError = NSError(domain: "Location", code: 2, userInfo: [NSLocalizedDescriptionKey: "Không thể lấy vị trí. Vui lòng kiểm tra GPS và thử lại."])
                }
            }
        }
        
        // ... (timer setup remains) ...
        
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .restricted, .denied:
            timeoutTimer?.invalidate()
            locationError = NSError(domain: "Location", code: 1, userInfo: [NSLocalizedDescriptionKey: "Vui lòng cấp quyền truy cập vị trí trong Cài đặt"])
            isLoading = false
        case .authorizedWhenInUse, .authorizedAlways:
            // Use startUpdatingLocation instead of requestLocation for better Simulator reliability
            manager.startUpdatingLocation()
        @unknown default:
            break
        }
    }
    
    // MARK: - CLLocationManagerDelegate
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            manager.startUpdatingLocation()
        } else if manager.authorizationStatus == .denied || manager.authorizationStatus == .restricted {
             timeoutTimer?.invalidate()
             isLoading = false
             locationError = NSError(domain: "Location", code: 1, userInfo: [NSLocalizedDescriptionKey: "Vui lòng cấp quyền truy cập vị trí trong Cài đặt"])
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            // STOP updating immediately to act like a one-shot request
            manager.stopUpdatingLocation()
            
            self.location = location
            // reverseGeocode will handle isLoading = false
            reverseGeocode(location: location)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        // Ignore simple errors if we are still trying, but for requestLocation logic we usually fail fast
        // taking care not to fail immediately on minor temporary errors if we were using startUpdatingLocation, 
        // but for requestLocation it typically fails once.
        
        // If error is kCLErrorLocationUnknown, it might resolve soon, but we will let the timeout handle it if it persists too long? 
        // Or fail immediately. CoreLocation usually retries. Let's just log.
        print("❌ [LocationManager] Error: \(error.localizedDescription)")
        
        if let clError = error as? CLError, clError.code == .locationUnknown {
            // Keep waiting until timeout
            return
        }
        
        DispatchQueue.main.async {
            self.isLoading = false
            self.timeoutTimer?.invalidate()
            self.locationError = error
        }
    }
    
    // MARK: - Geocoding
    
    private func reverseGeocode(location: CLLocation) {
        let geocoder = CLGeocoder()
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                
                if let error = error {
                    self?.timeoutTimer?.invalidate()
                    self?.locationError = error
                    print("❌ [LocationManager] Geocoding error: \(error.localizedDescription)")
                    return
                }
                
                if let placemark = placemarks?.first {
                    self?.timeoutTimer?.invalidate()
                    // Check if we're in Vietnam
                    let isVietnam = placemark.isoCountryCode == "VN"
                    
                    var province = ""
                    var district = ""
                    
                    if isVietnam {
                        // Vietnam format:
                        // administrativeArea = Tỉnh/Thành phố (e.g., "An Giang", "Hồ Chí Minh")
                        // subAdministrativeArea or locality = Quận/Huyện
                        province = placemark.administrativeArea ?? ""
                        district = placemark.subAdministrativeArea ?? placemark.locality ?? ""
                    } else {
                        // Fallback for non-Vietnam (e.g., Simulator with default US location)
                        province = placemark.administrativeArea ?? ""
                        district = placemark.locality ?? ""
                        print("⚠️ [LocationManager] Warning: Not in Vietnam. Country: \(placemark.isoCountryCode ?? "unknown")")
                    }
                    
                    // Clean up prefixes if present
                    province = province.replacingOccurrences(of: "Tỉnh ", with: "")
                                       .replacingOccurrences(of: "Thành phố ", with: "")
                                       .replacingOccurrences(of: "Quận ", with: "")
                                       .replacingOccurrences(of: "Huyện ", with: "")
                    
                    // Similar cleanup for district if needed, though usually prefix is on the value
                    district = district.replacingOccurrences(of: "Quận ", with: "")
                                       .replacingOccurrences(of: "Huyện ", with: "")
                                       .replacingOccurrences(of: "Thị xã ", with: "")
                                       .replacingOccurrences(of: "Thành phố ", with: "")

                    
                    print("📍 [LocationManager] Geocoded: \(district), \(province) (Country: \(placemark.isoCountryCode ?? "unknown"))")
                    self?.addressComponents = (province, district)
                }
            }
        }
    }
}
