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
                    MarketPrice(id: "1", categoryId: nil, productName: "Gạo ST25", date: nil, avgPrice: 25000, minPrice: 24000, maxPrice: 26000, postCount: 10, categoryName: "Lúa gạo"),
                    MarketPrice(id: "2", categoryId: nil, productName: "Cà phê robusta", date: nil, avgPrice: 65000, minPrice: 60000, maxPrice: 70000, postCount: 5, categoryName: "Cà phê"),
                    MarketPrice(id: "3", categoryId: nil, productName: "Hồ tiêu", date: nil, avgPrice: 85000, minPrice: 80000, maxPrice: 90000, postCount: 8, categoryName: "Hồ tiêu"),
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
