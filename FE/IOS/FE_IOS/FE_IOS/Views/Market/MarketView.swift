import SwiftUI

struct MarketView: View {
    @State private var prices: [MarketPrice] = []
    @State private var categories: [Category] = []
    @State private var selectedCategory: String? = nil
    @State private var isLoading = false
    @State private var searchText = ""
    
    var filteredPrices: [MarketPrice] {
        return prices
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Search & Filter Bar
            VStack(spacing: 12) {
                // Search
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("Tìm kiếm giá...", text: $searchText)
                        .onSubmit {
                            loadPrices()
                        }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(12)
                
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(categories) { category in
                            CategoryFilterChip(
                                title: category.name,
                                isSelected: selectedCategory == category.id,
                                action: {
                                    selectedCategory = category.id
                                    loadPrices()
                                }
                            )
                        }
                    }
                }
            }
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
        
        var endpoint = selectedCategory == nil
            ? APIConfig.MarketPrices.list
            : APIConfig.MarketPrices.byCategory(selectedCategory!)
        
        if !searchText.isEmpty {
            endpoint = "/market-prices?search=\(searchText.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? "")"
        }
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<[MarketPrice]>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                prices = data
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

// MARK: - Category Filter Chip
struct CategoryFilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .fontWeight(isSelected ? .semibold : .regular)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color(hex: "#2E7D32") : Color(.systemGray6))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}

struct MarketView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            MarketView()
        }
    }
}
