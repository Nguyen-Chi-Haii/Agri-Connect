import SwiftUI

struct LocationFillButton: View {
    @ObservedObject var locationManager = LocationManager.shared
    var onAddressReceived: (_ province: String, _ district: String) -> Void
    var onError: ((String) -> Void)?
    
    var body: some View {
        Button(action: {
            locationManager.requestLocation()
        }) {
            HStack(spacing: 4) {
                if locationManager.isLoading {
                    ProgressView()
                        .scaleEffect(0.7)
                } else {
                    Image(systemName: "location.fill")
                }
                Text("Lấy vị trí")
                    .font(.caption)
                    .fontWeight(.semibold)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(Color(hex: "#E8F5E9"))
            .foregroundColor(Color(hex: "#2E7D32"))
            .cornerRadius(8)
        }
        .onReceive(locationManager.$addressComponents) { components in
            if let components = components {
                onAddressReceived(components.province, components.district)
            }
        }
        .onReceive(locationManager.$locationError) { error in
            if let error = error {
                onError?(error.localizedDescription)
            }
        }
    }
}
