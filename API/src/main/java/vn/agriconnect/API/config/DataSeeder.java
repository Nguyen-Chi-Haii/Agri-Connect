package vn.agriconnect.API.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import vn.agriconnect.API.model.*;
import vn.agriconnect.API.model.embedded.KycInfo;
import vn.agriconnect.API.model.embedded.Location;
import vn.agriconnect.API.model.enums.*;
import vn.agriconnect.API.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * DataSeeder - Tạo dữ liệu mẫu khi khởi động ứng dụng
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final FeedbackRepository feedbackRepository;
    private final MarketPriceRepository marketPriceRepository;
    private final AdminLogRepository adminLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed users if empty
        if (userRepository.count() == 0) {
            log.info("Bắt đầu tạo users mẫu...");
            List<User> savedUsers = userRepository.saveAll(createUsers());
            log.info("Đã tạo {} users", savedUsers.size());
        }

        // Seed categories if empty
        if (categoryRepository.count() == 0) {
            log.info("Bắt đầu tạo danh mục mẫu...");
            List<Category> savedCategories = categoryRepository.saveAll(createCategories());
            log.info("Đã tạo {} categories", savedCategories.size());
        }

        // Seed posts if empty
        if (postRepository.count() == 0) {
            log.info("Bắt đầu tạo bài đăng mẫu...");
            List<User> users = userRepository.findAll();
            List<Category> categories = categoryRepository.findAll();
            if (!users.isEmpty() && !categories.isEmpty()) {
                List<Post> savedPosts = postRepository.saveAll(createPosts(users, categories));
                log.info("Đã tạo {} posts", savedPosts.size());
            }
        }

        // Seed market prices if empty
        if (marketPriceRepository.count() == 0) {
            log.info("Bắt đầu tạo giá thị trường mẫu...");
            List<Category> categories = categoryRepository.findAll();
            if (!categories.isEmpty()) {
                List<MarketPrice> savedPrices = marketPriceRepository.saveAll(createMarketPrices(categories));
                log.info("Đã tạo {} market prices", savedPrices.size());
            }
        }

        // Skip further seeding if users already exist (to avoid complexity in this fix)
        if (userRepository.count() > 5) {
            return;
        }

        log.info("Bắt đầu tạo dữ liệu mẫu...");

        try {
            // 1. Tạo Users
            List<User> savedUsers = userRepository.saveAll(createUsers());
            log.info("Đã tạo {} users", savedUsers.size());

            // 2. Tạo Categories
            List<Category> savedCategories = categoryRepository.saveAll(createCategories());
            log.info("Đã tạo {} categories", savedCategories.size());

            // 3. Tạo Posts
            List<Post> savedPosts = postRepository.saveAll(createPosts(savedUsers, savedCategories));
            log.info("Đã tạo {} posts", savedPosts.size());

            // 4. Tạo Conversations và Messages
            List<Conversation> savedConversations = conversationRepository.saveAll(
                    createConversations(savedUsers, savedPosts));
            log.info("Đã tạo {} conversations", savedConversations.size());

            try {
                List<Message> savedMessages = messageRepository.saveAll(
                        createMessages(savedConversations, savedUsers, savedPosts));
                log.info("Đã tạo {} messages", savedMessages.size());

                // Cập nhật lastMessage cho conversations
                updateConversationsLastMessage(savedConversations, savedMessages);
            } catch (Exception e) {
                log.error("Lỗi khi tạo messages: {}", e.getMessage(), e);
            }

            // 5. Tạo Notifications
            try {
                List<Notification> savedNotifications = notificationRepository.saveAll(
                        createNotifications(savedUsers, savedPosts));
                log.info("Đã tạo {} notifications", savedNotifications.size());
            } catch (Exception e) {
                log.error("Lỗi khi tạo notifications: {}", e.getMessage(), e);
            }

            // 6. Tạo Feedbacks
            try {
                List<Feedback> savedFeedbacks = feedbackRepository.saveAll(createFeedbacks(savedUsers));
                log.info("Đã tạo {} feedbacks", savedFeedbacks.size());
            } catch (Exception e) {
                log.error("Lỗi khi tạo feedbacks: {}", e.getMessage(), e);
            }

            // 7. Tạo MarketPrices
            try {
                List<MarketPrice> savedMarketPrices = marketPriceRepository.saveAll(
                        createMarketPrices(savedCategories));
                log.info("Đã tạo {} market prices", savedMarketPrices.size());
            } catch (Exception e) {
                log.error("Lỗi khi tạo market prices: {}", e.getMessage(), e);
            }

            // 8. Tạo AdminLogs
            try {
                List<AdminLog> savedAdminLogs = adminLogRepository.saveAll(createAdminLogs(savedUsers));
                log.info("Đã tạo {} admin logs", savedAdminLogs.size());
            } catch (Exception e) {
                log.error("Lỗi khi tạo admin logs: {}", e.getMessage(), e);
            }

            log.info("✅ Hoàn thành tạo dữ liệu mẫu!");
        } catch (Exception e) {
            log.error("❌ Lỗi khi seed data: {}", e.getMessage(), e);
        }
    }

    private List<User> createUsers() {
        // Admin
        User admin = new User();
        admin.setUsername("admin");
        admin.setPhone("0901234567");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Admin AgriConnect");
        admin.setRole(Role.ADMIN);
        admin.setActive(true);

        // Nông dân 1
        User farmer1 = new User();
        farmer1.setUsername("farmer1");
        farmer1.setPhone("0912345678");
        farmer1.setPassword(passwordEncoder.encode("farmer123"));
        farmer1.setFullName("Nguyễn Văn Nông");
        farmer1.setAddress("Xã Tân Phú, Huyện Đồng Phú, Bình Phước");
        farmer1.setRole(Role.FARMER);
        farmer1.setActive(true);
        KycInfo kyc1 = new KycInfo();
        kyc1.setCccd("079201012345");
        kyc1.setStatus("VERIFIED");
        farmer1.setKyc(kyc1);

        // Nông dân 2
        User farmer2 = new User();
        farmer2.setUsername("farmer2");
        farmer2.setPhone("0923456789");
        farmer2.setPassword(passwordEncoder.encode("farmer123"));
        farmer2.setFullName("Trần Thị Hoa");
        farmer2.setAddress("Xã Bình Minh, Huyện Châu Thành, Tiền Giang");
        farmer2.setRole(Role.FARMER);
        farmer2.setActive(true);

        // Thương lái 1
        User trader1 = new User();
        trader1.setUsername("trader1");
        trader1.setPhone("0934567890");
        trader1.setPassword(passwordEncoder.encode("trader123"));
        trader1.setFullName("Lê Văn Buôn");
        trader1.setAddress("Quận Ninh Kiều, TP. Cần Thơ");
        trader1.setRole(Role.TRADER);
        trader1.setActive(true);

        // Thương lái 2
        User trader2 = new User();
        trader2.setUsername("trader2");
        trader2.setPhone("0945678901");
        trader2.setPassword(passwordEncoder.encode("trader123"));
        trader2.setFullName("Phạm Thị Thu");
        trader2.setAddress("Quận Bình Thạnh, TP. Hồ Chí Minh");
        trader2.setRole(Role.TRADER);
        trader2.setActive(true);

        return Arrays.asList(admin, farmer1, farmer2, trader1, trader2);
    }

    private List<Category> createCategories() {
        Category cat1 = new Category();
        cat1.setName("Lúa gạo");
        cat1.setIcon("🌾");
        cat1.setDescription("Các loại lúa, gạo");

        Category cat2 = new Category();
        cat2.setName("Rau củ");
        cat2.setIcon("🥬");
        cat2.setDescription("Rau xanh, củ quả các loại");

        Category cat3 = new Category();
        cat3.setName("Trái cây");
        cat3.setIcon("🍊");
        cat3.setDescription("Trái cây tươi ngon");

        Category cat4 = new Category();
        cat4.setName("Thủy sản");
        cat4.setIcon("🐟");
        cat4.setDescription("Cá, tôm, cua và các loại thủy sản");

        Category cat5 = new Category();
        cat5.setName("Gia súc");
        cat5.setIcon("🐄");
        cat5.setDescription("Heo, bò, dê và gia súc khác");

        Category cat6 = new Category();
        cat6.setName("Gia cầm");
        cat6.setIcon("🐔");
        cat6.setDescription("Gà, vịt, ngan, ngỗng");

        Category cat7 = new Category();
        cat7.setName("Vật tư nông nghiệp");
        cat7.setIcon("🧪");
        cat7.setDescription("Phân bón, thuốc BVTV, hạt giống");

        return Arrays.asList(cat1, cat2, cat3, cat4, cat5, cat6, cat7);
    }

    private List<Post> createPosts(List<User> users, List<Category> categories) {
        User farmer1 = users.get(1);
        User farmer2 = users.get(2);

        Post post1 = new Post();
        post1.setSellerId(farmer1.getId());
        post1.setCategoryId(categories.get(0).getId());
        post1.setTitle("Bán 5 tấn lúa ST25 vụ Đông Xuân");
        post1.setDescription("Lúa ST25 chất lượng cao, thu hoạch mới, độ ẩm 14%. Giao tận nơi trong tỉnh.");
        post1.setPrice(8500000.0);
        post1.setUnit("tấn");
        post1.setQuantity(5);
        post1.setImages(Arrays.asList("/uploads/lua-st25-1.jpg", "/uploads/lua-st25-2.jpg"));
        post1.setStatus(PostStatus.APPROVED);
        Location loc1 = new Location();
        loc1.setProvince("Bình Phước");
        loc1.setDistrict("Đồng Phú");
        loc1.setWard("Tân Phú");
        post1.setLocation(loc1);

        Post post2 = new Post();
        post2.setSellerId(farmer2.getId());
        post2.setCategoryId(categories.get(1).getId());
        post2.setTitle("Bán rau muống, rau cải tươi ngon");
        post2.setDescription("Rau trồng theo phương pháp hữu cơ, không thuốc trừ sâu. Giao hàng mỗi sáng.");
        post2.setPrice(15000.0);
        post2.setUnit("kg");
        post2.setQuantity(100);
        post2.setStatus(PostStatus.APPROVED);
        Location loc2 = new Location();
        loc2.setProvince("Tiền Giang");
        loc2.setDistrict("Châu Thành");
        loc2.setWard("Bình Minh");
        post2.setLocation(loc2);

        Post post3 = new Post();
        post3.setSellerId(farmer2.getId());
        post3.setCategoryId(categories.get(2).getId());
        post3.setTitle("Xoài cát Hòa Lộc loại 1");
        post3.setDescription("Xoài cát Hòa Lộc chín cây, thơm ngon, ngọt lịm. Đóng hộp quà tặng theo yêu cầu.");
        post3.setPrice(65000.0);
        post3.setUnit("kg");
        post3.setQuantity(500);
        post3.setImages(Arrays.asList("/uploads/xoai-1.jpg"));
        post3.setStatus(PostStatus.APPROVED);
        Location loc3 = new Location();
        loc3.setProvince("Tiền Giang");
        loc3.setDistrict("Cái Bè");
        post3.setLocation(loc3);

        Post post4 = new Post();
        post4.setSellerId(farmer1.getId());
        post4.setCategoryId(categories.get(4).getId());
        post4.setTitle("Bán bò giống Brahman");
        post4.setDescription("Bò giống Brahman 2 năm tuổi, khỏe mạnh, đã tiêm phòng đầy đủ.");
        post4.setPrice(35000000.0);
        post4.setUnit("con");
        post4.setQuantity(3);
        post4.setStatus(PostStatus.PENDING);
        Location loc4 = new Location();
        loc4.setProvince("Bình Phước");
        loc4.setDistrict("Bù Đăng");
        post4.setLocation(loc4);

        return Arrays.asList(post1, post2, post3, post4);
    }

    private List<Conversation> createConversations(List<User> users, List<Post> posts) {
        User farmer1 = users.get(1);
        User trader1 = users.get(3);
        User trader2 = users.get(4);

        // Conversation 1: Trader1 hỏi mua lúa của Farmer1
        Conversation conv1 = new Conversation();
        conv1.setParticipants(Arrays.asList(farmer1.getId(), trader1.getId()));

        // Conversation 2: Trader2 hỏi mua xoài
        Conversation conv2 = new Conversation();
        conv2.setParticipants(Arrays.asList(users.get(2).getId(), trader2.getId()));

        return Arrays.asList(conv1, conv2);
    }

    private List<Message> createMessages(List<Conversation> conversations, List<User> users, List<Post> posts) {
        if (conversations.size() < 2 || users.size() < 5 || posts.size() < 3) {
            log.warn("Không đủ dữ liệu để tạo messages: conversations={}, users={}, posts={}",
                    conversations.size(), users.size(), posts.size());
            return Arrays.asList();
        }

        Conversation conv1 = conversations.get(0);
        Conversation conv2 = conversations.get(1);
        User trader1 = users.get(3);
        User farmer1 = users.get(1);
        User trader2 = users.get(4);
        User farmer2 = users.get(2);

        // Messages for Conversation 1
        Message msg1 = new Message();
        msg1.setConversationId(conv1.getId());
        msg1.setSenderId(trader1.getId());
        msg1.setType(MessageType.TEXT);
        msg1.setContent("Chào anh, em muốn hỏi về lô lúa ST25 của anh");
        msg1.setCreatedAt(Instant.now().minusSeconds(3600));

        Message msg2 = new Message();
        msg2.setConversationId(conv1.getId());
        msg2.setSenderId(farmer1.getId());
        msg2.setType(MessageType.TEXT);
        msg2.setContent("Chào em, anh còn 5 tấn, em cần bao nhiêu?");
        msg2.setCreatedAt(Instant.now().minusSeconds(3500));

        Message msg3 = new Message();
        msg3.setConversationId(conv1.getId());
        msg3.setSenderId(trader1.getId());
        msg3.setType(MessageType.TEXT);
        msg3.setContent("Em cần 3 tấn, anh có thể giảm giá được không ạ?");
        msg3.setCreatedAt(Instant.now().minusSeconds(3400));

        // Messages for Conversation 2 - simple text only
        Message msg4 = new Message();
        msg4.setConversationId(conv2.getId());
        msg4.setSenderId(trader2.getId());
        msg4.setType(MessageType.TEXT);
        msg4.setContent("Chị ơi, em quan tâm đến xoài của chị");
        msg4.setCreatedAt(Instant.now().minusSeconds(1800));

        Message msg5 = new Message();
        msg5.setConversationId(conv2.getId());
        msg5.setSenderId(farmer2.getId());
        msg5.setType(MessageType.TEXT);
        msg5.setContent("Dạ em cần bao nhiêu kg? Chị có thể ship trong ngày luôn");
        msg5.setCreatedAt(Instant.now().minusSeconds(1700));

        return Arrays.asList(msg1, msg2, msg3, msg4, msg5);
    }

    private void updateConversationsLastMessage(List<Conversation> conversations, List<Message> messages) {
        log.info("Bỏ qua updateConversationsLastMessage cho lần debug này");
    }

    private List<Notification> createNotifications(List<User> users, List<Post> posts) {
        User farmer1 = users.get(1);
        User farmer2 = users.get(2);
        User trader1 = users.get(3);

        Notification n1 = new Notification();
        n1.setUserId(farmer1.getId());
        n1.setType(NotificationType.POST_APPROVED);
        n1.setTitle("Bài đăng đã được duyệt");
        n1.setContent("Bài đăng 'Bán 5 tấn lúa ST25' của bạn đã được duyệt");
        // n1.setIsRead(false); // default là false rồi
        n1.setCreatedAt(Instant.now().minusSeconds(7200));

        Notification n2 = new Notification();
        n2.setUserId(farmer2.getId());
        n2.setType(NotificationType.NEW_MESSAGE);
        n2.setTitle("Tin nhắn mới");
        n2.setContent("Bạn có tin nhắn mới từ Phạm Thị Thu");
        // n2.setIsRead(false); // default là false rồi
        n2.setCreatedAt(Instant.now().minusSeconds(1800));

        Notification n3 = new Notification();
        n3.setUserId(trader1.getId());
        n3.setType(NotificationType.SYSTEM);
        n3.setTitle("Chào mừng đến AgriConnect");
        n3.setContent("Cảm ơn bạn đã đăng ký. Hãy khám phá các sản phẩm nông sản chất lượng!");
        n3.setRead(true);
        n3.setCreatedAt(Instant.now().minusSeconds(86400));

        Notification n4 = new Notification();
        n4.setUserId(farmer1.getId());
        n4.setType(NotificationType.PRICE_UPDATE);
        n4.setTitle("Cập nhật giá thị trường");
        n4.setContent("Giá lúa gạo hôm nay: 8.500đ - 9.000đ/kg");
        // n4.setIsRead(false); // default là false rồi
        n4.setCreatedAt(Instant.now().minusSeconds(3600));

        return Arrays.asList(n1, n2, n3, n4);
    }

    private List<Feedback> createFeedbacks(List<User> users) {
        User farmer1 = users.get(1);
        User trader1 = users.get(3);

        Feedback f1 = new Feedback();
        f1.setUserId(farmer1.getId());
        f1.setTitle("Đề xuất thêm tính năng");
        f1.setContent("Xin hãy thêm tính năng thông báo qua Zalo khi có người hỏi mua sản phẩm");
        f1.setType(FeedbackType.SUGGESTION);
        f1.setStatus(FeedbackStatus.IN_PROGRESS);
        f1.setCreatedAt(Instant.now().minusSeconds(172800));

        Feedback f2 = new Feedback();
        f2.setUserId(trader1.getId());
        f2.setTitle("Lỗi hiển thị ảnh");
        f2.setContent("Ảnh sản phẩm không hiển thị được trên điện thoại iPhone");
        f2.setType(FeedbackType.BUG);
        f2.setStatus(FeedbackStatus.NEW);
        f2.setCreatedAt(Instant.now().minusSeconds(43200));

        Feedback f3 = new Feedback();
        f3.setUserId(farmer1.getId());
        f3.setTitle("App rất hữu ích");
        f3.setContent("Cảm ơn đội ngũ phát triển. App giúp tôi bán được nhiều nông sản hơn.");
        f3.setType(FeedbackType.OTHER);
        f3.setStatus(FeedbackStatus.RESOLVED);
        f3.setCreatedAt(Instant.now().minusSeconds(604800));

        return Arrays.asList(f1, f2, f3);
    }

    private List<MarketPrice> createMarketPrices(List<Category> categories) {
        LocalDate today = LocalDate.now();

        // Lúa gạo
        MarketPrice mp1 = new MarketPrice();
        mp1.setCategoryId(categories.get(0).getId());
        mp1.setProductName("Lúa ST25");
        mp1.setDate(today);
        mp1.setAvgPrice(8500.0);
        mp1.setMinPrice(8000.0);
        mp1.setMaxPrice(9000.0);
        mp1.setPostCount(15);

        MarketPrice mp1b = new MarketPrice();
        mp1b.setCategoryId(categories.get(0).getId());
        mp1b.setProductName("Gạo Nàng Hương");
        mp1b.setDate(today.minusDays(1));
        mp1b.setAvgPrice(12000.0);
        mp1b.setMinPrice(11000.0);
        mp1b.setMaxPrice(13000.0);
        mp1b.setPostCount(12);

        // Rau củ
        MarketPrice mp2 = new MarketPrice();
        mp2.setCategoryId(categories.get(1).getId());
        mp2.setProductName("Rau muống");
        mp2.setDate(today);
        mp2.setAvgPrice(15000.0);
        mp2.setMinPrice(10000.0);
        mp2.setMaxPrice(25000.0);
        mp2.setPostCount(45);

        // Trái cây
        MarketPrice mp3 = new MarketPrice();
        mp3.setCategoryId(categories.get(2).getId());
        mp3.setProductName("Xoài cát Hòa Lộc");
        mp3.setDate(today);
        mp3.setAvgPrice(55000.0);
        mp3.setMinPrice(30000.0);
        mp3.setMaxPrice(80000.0);
        mp3.setPostCount(32);

        // Thủy sản
        MarketPrice mp4 = new MarketPrice();
        mp4.setCategoryId(categories.get(3).getId());
        mp4.setProductName("Cá tra phi lê");
        mp4.setDate(today);
        mp4.setAvgPrice(45000.0);
        mp4.setMinPrice(38000.0);
        mp4.setMaxPrice(55000.0);
        mp4.setPostCount(18);

        return Arrays.asList(mp1, mp1b, mp2, mp3, mp4);
    }

    private List<AdminLog> createAdminLogs(List<User> users) {
        User admin = users.get(0);

        AdminLog log1 = new AdminLog();
        log1.setAdminId(admin.getId());
        log1.setAction("APPROVE_POST");
        log1.setDetail("Duyệt bài đăng 'Bán 5 tấn lúa ST25'");
        log1.setTimestamp(Instant.now().minusSeconds(7200));

        AdminLog log2 = new AdminLog();
        log2.setAdminId(admin.getId());
        log2.setAction("APPROVE_POST");
        log2.setDetail("Duyệt bài đăng 'Bán rau muống, rau cải tươi ngon'");
        log2.setTimestamp(Instant.now().minusSeconds(7100));

        AdminLog log3 = new AdminLog();
        log3.setAdminId(admin.getId());
        log3.setAction("VERIFY_USER");
        log3.setDetail("Xác minh KYC cho user Nguyễn Văn Nông (0912345678)");
        log3.setTimestamp(Instant.now().minusSeconds(86400));

        AdminLog log4 = new AdminLog();
        log4.setAdminId(admin.getId());
        log4.setAction("UPDATE_FEEDBACK");
        log4.setDetail("Đánh dấu feedback 'Đề xuất thêm tính năng' là IN_PROGRESS");
        log4.setTimestamp(Instant.now().minusSeconds(172700));

        return Arrays.asList(log1, log2, log3, log4);
    }
}
