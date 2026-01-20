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
    
    func requestLocation() {
        isLoading = true
        
        switch manager.authorizationStatus {
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .restricted, .denied:
            locationError = NSError(domain: "Location", code: 1, userInfo: [NSLocalizedDescriptionKey: "Vui lòng cấp quyền truy cập vị trí trong Cài đặt"])
            isLoading = false
        case .authorizedWhenInUse, .authorizedAlways:
            manager.requestLocation()
        @unknown default:
            break
        }
    }
    
    // MARK: - CLLocationManagerDelegate
    
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
            manager.requestLocation()
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if let location = locations.first {
            self.location = location
            reverseGeocode(location: location)
        }
    }
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        isLoading = false
        locationError = error
        print("❌ [LocationManager] Error: \(error.localizedDescription)")
    }
    
    // MARK: - Geocoding
    
    private func reverseGeocode(location: CLLocation) {
        let geocoder = CLGeocoder()
        geocoder.reverseGeocodeLocation(location) { [weak self] placemarks, error in
            DispatchQueue.main.async {
                self?.isLoading = false
                
                if let error = error {
                    self?.locationError = error
                    print("❌ [LocationManager] Geocoding error: \(error.localizedDescription)")
                    return
                }
                
                if let placemark = placemarks?.first {
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
                    district = district.replacingOccurrences(of: "Quận ", with: "")
                                       .replacingOccurrences(of: "Huyện ", with: "")
                    
                    print("📍 [LocationManager] Geocoded: \(district), \(province) (Country: \(placemark.isoCountryCode ?? "unknown"))")
                    self?.addressComponents = (province, district)
                }
            }
        }
    }
}
