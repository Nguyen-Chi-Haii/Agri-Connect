import SwiftUI

struct MarketView: View {
    @State private var prices: [MarketPrice] = []
    @State private var isLoading = false
    @State private var searchText = ""
    
    var filteredPrices: [MarketPrice] {
        if searchText.isEmpty {
            return prices
        }
        return prices.filter { $0.productName.localizedCaseInsensitiveContains(searchText) }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.gray)
                TextField("Tìm kiếm giá...", text: $searchText)
            }
            .padding()
            .background(Color(.systemGray6))
            .cornerRadius(12)
            .padding()
            
            // List
            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if filteredPrices.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "chart.line.uptrend.xyaxis")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Chưa có dữ liệu giá")
                        .foregroundColor(.gray)
                }
                Spacer()
            } else {
                List(filteredPrices) { price in
                    MarketPriceRow(price: price)
                }
                .listStyle(PlainListStyle())
            }
        }
        .navigationTitle("Giá thị trường")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadPrices()
        }
    }
    
    private func loadPrices() {
        isLoading = true
        
        APIClient.shared.request(
            endpoint: "/market-prices",
            method: .get
        ) { (result: Result<ApiResponse<[MarketPrice]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                prices = data
            } else {
                // Mock data if API fails
                prices = [
                    MarketPrice(id: "1", productName: "Gạo ST25", price: 25000, unit: "kg", province: "An Giang", updatedAt: nil),
                    MarketPrice(id: "2", productName: "Cà phê robusta", price: 65000, unit: "kg", province: "Đắk Lắk", updatedAt: nil),
                    MarketPrice(id: "3", productName: "Hồ tiêu", price: 85000, unit: "kg", province: "Bình Phước", updatedAt: nil),
                    MarketPrice(id: "4", productName: "Điều thô", price: 45000, unit: "kg", province: "Bình Phước", updatedAt: nil),
                    MarketPrice(id: "5", productName: "Thanh long", price: 15000, unit: "kg", province: "Bình Thuận", updatedAt: nil),
                ]
            }
        }
    }
}

// MARK: - Market Price Row
struct MarketPriceRow: View {
    let price: MarketPrice
    
    var body: some View {
        HStack(spacing: 12) {
            // Icon
            ZStack {
                Circle()
                    .fill(Color(hex: "#E8F5E9"))
                    .frame(width: 50, height: 50)
                
                Text("🌾")
                    .font(.title2)
            }
            
            // Info
            VStack(alignment: .leading, spacing: 4) {
                Text(price.productName)
                    .font(.headline)
                
                if let province = price.province {
                    Text(province)
                        .font(.caption)
                        .foregroundColor(.gray)
                }
            }
            
            Spacer()
            
            // Price
            VStack(alignment: .trailing, spacing: 2) {
                Text(formatPrice(price.price))
                    .font(.headline)
                    .foregroundColor(Color(hex: "#2E7D32"))
                
                Text("/ \(price.unit)")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
        }
        .padding(.vertical, 4)
    }
    
    private func formatPrice(_ price: Double) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.groupingSeparator = "."
        return (formatter.string(from: NSNumber(value: price)) ?? "\(price)") + "đ"
    }
}

struct MarketView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            MarketView()
        }
    }
}
