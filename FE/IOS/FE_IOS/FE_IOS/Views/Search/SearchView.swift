import SwiftUI

struct SearchView: View {
    @State private var searchText = ""
    @State private var selectedCategory: String = ""
    @State private var minPrice = ""
    @State private var maxPrice = ""
    @State private var selectedProvince = ""
    @State private var posts: [Post] = []
    @State private var categories: [Category] = []
    @State private var isLoading = false
    @State private var showFilters = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Search Bar
            HStack {
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("Tìm kiếm nông sản...", text: $searchText)
                        .onSubmit {
                            search()
                        }
                }
                .padding()
                .background(Color(.systemGray6))
                .cornerRadius(12)
                
                Button {
                    showFilters.toggle()
                } label: {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                        .font(.title2)
                        .foregroundColor(Color(hex: "#2E7D32"))
                }
            }
            .padding()
            
            // Filters
            if showFilters {
                VStack(spacing: 12) {
                    // Categories
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            FilterChip(
                                title: "Tất cả",
                                isSelected: selectedCategory.isEmpty
                            ) {
                                selectedCategory = ""
                            }
                            
                            ForEach(categories) { category in
                                FilterChip(
                                    title: category.name,
                                    isSelected: selectedCategory == category.id
                                ) {
                                    selectedCategory = category.id
                                }
                            }
                        }
                    }
                    
                    // Price Range
                    HStack(spacing: 12) {
                        TextField("Giá từ", text: $minPrice)
                            .keyboardType(.numberPad)
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                        
                        Text("-")
                        
                        TextField("Đến", text: $maxPrice)
                            .keyboardType(.numberPad)
                            .padding()
                            .background(Color(.systemGray6))
                            .cornerRadius(8)
                    }
                    
                    // Province
                    TextField("Tỉnh/Thành phố", text: $selectedProvince)
                        .padding()
                        .background(Color(.systemGray6))
                        .cornerRadius(8)
                    
                    // Search Button
                    Button {
                        search()
                    } label: {
                        Text("Tìm kiếm")
                            .fontWeight(.semibold)
                            .frame(maxWidth: .infinity)
                            .padding()
                            .background(Color(hex: "#2E7D32"))
                            .foregroundColor(.white)
                            .cornerRadius(12)
                    }
                }
                .padding(.horizontal)
                .padding(.bottom)
            }
            
            Divider()
            
            // Results
            if isLoading {
                Spacer()
                ProgressView()
                Spacer()
            } else if posts.isEmpty {
                Spacer()
                VStack(spacing: 12) {
                    Image(systemName: "magnifyingglass")
                        .font(.system(size: 50))
                        .foregroundColor(.gray)
                    Text("Nhập từ khóa để tìm kiếm")
                        .foregroundColor(.gray)
                }
                Spacer()
            } else {
                List(posts) { post in
                    NavigationLink(destination: PostDetailView(postId: post.id)) {
                        SearchResultRow(post: post)
                    }
                }
                .listStyle(PlainListStyle())
            }
        }
        .navigationTitle("Tìm kiếm")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            loadCategories()
        }
    }
    
    private func loadCategories() {
        APIClient.shared.request(
            endpoint: APIConfig.Categories.list,
            method: .get
        ) { (result: Result<ApiResponse<[Category]>, Error>) in
            if case .success(let response) = result, let data = response.data {
                categories = data
            }
        }
    }
    
    private func search() {
        isLoading = true
        
        var endpoint = "\(APIConfig.Posts.search)?keyword=\(searchText)"
        
        if !selectedCategory.isEmpty {
            endpoint += "&categoryId=\(selectedCategory)"
        }
        if !minPrice.isEmpty {
            endpoint += "&minPrice=\(minPrice)"
        }
        if !maxPrice.isEmpty {
            endpoint += "&maxPrice=\(maxPrice)"
        }
        if !selectedProvince.isEmpty {
            endpoint += "&province=\(selectedProvince)"
        }
        
        APIClient.shared.request(
            endpoint: endpoint,
            method: .get
        ) { (result: Result<ApiResponse<PagedResponse<Post>>, Error>) in
            isLoading = false
            if case .success(let response) = result, let data = response.data {
                posts = data.content
            }
        }
    }
}

// MARK: - Filter Chip
struct FilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.subheadline)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(isSelected ? Color(hex: "#2E7D32") : Color(.systemGray6))
                .foregroundColor(isSelected ? .white : .primary)
                .cornerRadius(20)
        }
    }
}

// MARK: - Search Result Row
struct SearchResultRow: View {
    let post: Post
    
    var body: some View {
        HStack(spacing: 12) {
            // Image
            if let images = post.images, let firstImage = images.first, let url = URL(string: firstImage) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .success(let image):
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    default:
                        Rectangle().fill(Color.gray.opacity(0.3))
                    }
                }
                .frame(width: 80, height: 80)
                .cornerRadius(8)
                .clipped()
            } else {
                Rectangle()
                    .fill(Color.gray.opacity(0.2))
                    .frame(width: 80, height: 80)
                    .cornerRadius(8)
            }
            
            // Content
            VStack(alignment: .leading, spacing: 4) {
                Text(post.title)
                    .font(.headline)
                    .lineLimit(2)
                
                if let price = post.price, let unit = post.unit {
                    Text("\(formatPrice(price)) / \(unit)")
                        .font(.subheadline)
                        .foregroundColor(Color(hex: "#2E7D32"))
                }
                
                if let province = post.province {
                    HStack(spacing: 4) {
                        Image(systemName: "mappin")
                        Text(province)
                    }
                    .font(.caption)
                    .foregroundColor(.gray)
                }
            }
            
            Spacer()
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

struct SearchView_Previews: PreviewProvider {
    static var previews: some View {
        NavigationView {
            SearchView()
        }
    }
}
