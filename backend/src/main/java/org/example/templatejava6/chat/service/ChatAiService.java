package org.example.templatejava6.chat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.templatejava6.chat.dto.ChatRequestDto;
import org.example.templatejava6.chat.dto.ChatResponseDto;
import org.example.templatejava6.chat.entity.PhienChatAi;
import org.example.templatejava6.chat.entity.TinNhanChatAi;
import org.example.templatejava6.chat.repository.PhienChatAiRepository;
import org.example.templatejava6.chat.repository.TinNhanChatAiRepository;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.entity.SanPhamCongDung;
import org.example.templatejava6.product.entity.SanPhamLoaiDa;
import org.example.templatejava6.product.entity.SanPhamThanhPhan;
import org.example.templatejava6.product.model.response.SanPhamResponse;
import org.example.templatejava6.product.repository.ChiTietSanPhamRepository;
import org.example.templatejava6.product.repository.SanPhamCongDungRepository;
import org.example.templatejava6.product.repository.SanPhamLoaiDaRepository;
import org.example.templatejava6.product.repository.SanPhamRepository;
import org.example.templatejava6.product.repository.SanPhamThanhPhanRepository;
import org.example.templatejava6.product.service.SanPhamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ChatAiService {

    private static final int HISTORY_LIMIT = 10;
    private static final long CATALOG_TTL_MS = 5 * 60 * 1000L;
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\\[PRODUCT_ID:\\s*(\\d+)\\]");
    private static final NumberFormat VND = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Autowired private PhienChatAiRepository phienChatAiRepository;
    @Autowired private TinNhanChatAiRepository tinNhanChatAiRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private SanPhamService sanPhamService;
    @Autowired private SanPhamRepository sanPhamRepository;
    @Autowired private SanPhamCongDungRepository sanPhamCongDungRepository;
    @Autowired private SanPhamLoaiDaRepository sanPhamLoaiDaRepository;
    @Autowired private SanPhamThanhPhanRepository sanPhamThanhPhanRepository;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${app.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String geminiBaseUrl;

    @Value("${app.gemini.model:gemini-flash-latest}")
    private String geminiModel;

    private final RestTemplate restTemplate;
    private final AtomicReference<CachedCatalog> catalogCache = new AtomicReference<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ChatAiService.class);

    public ChatAiService(org.springframework.boot.web.client.RestTemplateBuilder builder) {
        // Cùng mẫu GhnClient: build 1 lần, có timeout để lỗi mạng không treo request chat
        this.restTemplate = builder
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .readTimeout(java.time.Duration.ofSeconds(20))
                .build();
    }

    @Transactional
    public PhienChatAi taoPhienMoi(Integer idKhachHang) {
        PhienChatAi phien = new PhienChatAi();
        if (idKhachHang != null) {
            KhachHang kh = khachHangRepository.findById(idKhachHang).orElse(null);
            phien.setKhachHang(kh);
        }
        phien.setTieuDe("Chat tư vấn ngày " + LocalDateTime.now().toLocalDate());
        phien.setTrangThai("DANG_MO");
        phien.setThoiGianBatDau(LocalDateTime.now());
        return phienChatAiRepository.save(phien);
    }

    public List<PhienChatAi> getPhienCuaKhachHang(Integer idKhachHang) {
        return phienChatAiRepository.findByKhachHangIdOrderByThoiGianBatDauDesc(idKhachHang);
    }

    public List<ChatResponseDto> getLichSuTinNhan(Integer idPhien) {
        Map<Integer, SanPhamResponse> byId = catalogById();
        return tinNhanChatAiRepository.findByPhienChatAiIdOrderByThoiGianAsc(idPhien)
                .stream()
                .map(t -> mapToDto(t, byId))
                .collect(Collectors.toList());
    }

    @Transactional
    public ChatResponseDto guiTinNhan(ChatRequestDto request) {
        PhienChatAi phien;
        if (request.getIdPhien() != null) {
            phien = phienChatAiRepository.findById(request.getIdPhien())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy phiên chat"));
        } else {
            phien = taoPhienMoi(request.getIdKhachHang());
        }

        TinNhanChatAi tinKhach = new TinNhanChatAi();
        tinKhach.setPhienChatAi(phien);
        tinKhach.setNguoiGui("KHACH");
        tinKhach.setNoiDung(request.getNoiDung());
        tinKhach.setThoiGian(LocalDateTime.now());
        tinNhanChatAiRepository.save(tinKhach);

        List<ProductCatalogItem> catalog = loadActiveCatalog();
        Map<Integer, SanPhamResponse> byId = catalog.stream()
                .collect(Collectors.toMap(ProductCatalogItem::id, ProductCatalogItem::response, (a, b) -> a));

        List<TinNhanChatAi> history = loadChatHistory(phien.getId(), tinKhach.getId());
        TinNhanChatAi tinAi = generateGeminiResponse(request.getNoiDung(), phien, catalog, history);
        tinNhanChatAiRepository.save(tinAi);

        return mapToDto(tinAi, byId);
    }

    private List<TinNhanChatAi> loadChatHistory(Integer idPhien, Integer excludeId) {
        List<TinNhanChatAi> recent = tinNhanChatAiRepository.findRecentByPhien(
                idPhien, PageRequest.of(0, HISTORY_LIMIT + 5));
        List<TinNhanChatAi> filtered = recent.stream()
                .filter(t -> excludeId == null || !Objects.equals(t.getId(), excludeId))
                .limit(HISTORY_LIMIT)
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(filtered);
        return filtered;
    }

    private TinNhanChatAi generateGeminiResponse(
            String noiDung,
            PhienChatAi phien,
            List<ProductCatalogItem> catalog,
            List<TinNhanChatAi> history) {
        TinNhanChatAi ai = new TinNhanChatAi();
        ai.setPhienChatAi(phien);
        ai.setNguoiGui("AI");
        ai.setThoiGian(LocalDateTime.now());

        String apiKey = resolveGeminiApiKey();
        if (apiKey == null) {
            log.warn("Gemini API key chưa cấu hình (GEMINI_API_KEY / .env) — fallback");
            return generateRuleBasedResponse(noiDung, phien, catalog);
        }

        try {
            String catalogText = catalog.stream()
                    .map(ProductCatalogItem::promptLine)
                    .collect(Collectors.joining("\n"));
            String systemPrompt = buildSystemPrompt(catalogText);
            systemPrompt += """

                    Khi gợi ý sản phẩm, ghi kèm [PRODUCT_ID: xxx] cho TẤT CẢ sản phẩm được đề cập (có thể nhiều ID).
                    Không dùng link HTML; hệ thống tự hiển thị thẻ sản phẩm. Chỉ tư vấn sản phẩm trong danh sách.""";

            ObjectNode rootNode = objectMapper.createObjectNode();
            ObjectNode systemInstruction = rootNode.putObject("system_instruction");
            systemInstruction.putArray("parts").addObject().put("text", systemPrompt);

            ArrayNode contents = rootNode.putArray("contents");
            appendGeminiHistory(contents, history);
            ObjectNode userTurn = contents.addObject();
            userTurn.put("role", "user");
            userTurn.putArray("parts").addObject().put("text", noiDung != null ? noiDung : "");

            ObjectNode generationConfig = rootNode.putObject("generationConfig");
            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 2048);

            String body = objectMapper.writeValueAsString(rootNode);
            String preferred = (geminiModel == null || geminiModel.isBlank())
                    ? "gemini-flash-latest" : geminiModel.trim();
            List<String> modelsToTry = new ArrayList<>(
                    new LinkedHashSet<>(List.of(preferred, "gemini-flash-latest", "gemini-flash-lite-latest", "gemini-2.5-flash-lite")));

            String aiText = null;
            Exception lastError = null;
            for (String model : modelsToTry) {
                try {
                    aiText = callGeminiGenerateContent(model, apiKey, body);
                    if (aiText != null && !aiText.isBlank()) {
                        log.info("Gemini OK với model={}", model);
                        break;
                    }
                } catch (Exception ex) {
                    lastError = ex;
                    log.warn("Gemini model={} lỗi: {}", model, ex.getMessage());
                }
            }
            if (aiText == null || aiText.isBlank()) {
                throw lastError != null ? lastError : new IllegalStateException("Gemini không trả được nội dung");
            }

            List<String> foundIds = new ArrayList<>();
            SanPham spGoiY = null;
            Matcher matcher = PRODUCT_ID_PATTERN.matcher(aiText);
            while (matcher.find()) {
                Integer idSp = Integer.parseInt(matcher.group(1));
                boolean allowed = catalog.stream().anyMatch(c -> Objects.equals(c.id(), idSp));
                if (allowed) {
                    foundIds.add(idSp.toString());
                    if (spGoiY == null) {
                        spGoiY = sanPhamRepository.findById(idSp).orElse(null);
                    }
                }
            }
            if (!foundIds.isEmpty()) {
                ai.setDanhSachSpGoiY(String.join(",", foundIds));
            }
            aiText = PRODUCT_ID_PATTERN.matcher(aiText).replaceAll("").replaceAll("[ \\t]{2,}", " ").trim();

            ai.setNoiDung(aiText);
            ai.setSanPhamGoiY(spGoiY);
            return ai;
        } catch (org.springframework.web.client.HttpStatusCodeException httpEx) {
            int status = httpEx.getStatusCode().value();
            if (status == 401 || status == 403) {
                log.warn("Gemini auth lỗi (HTTP {}) — kiểm tra API key. Chuyển fallback.", status);
            } else if (status == 429) {
                log.warn("Gemini vượt hạn mức (HTTP 429). Chuyển fallback.");
            } else {
                log.warn("Gemini HTTP {} — fallback. Body: {}", status, truncateForLog(httpEx.getResponseBodyAsString()));
            }
            return generateRuleBasedResponse(noiDung, phien, catalog);
        } catch (org.springframework.web.client.ResourceAccessException timeoutEx) {
            log.warn("Gemini timeout/không kết nối — fallback: {}", timeoutEx.getMessage());
            return generateRuleBasedResponse(noiDung, phien, catalog);
        } catch (Exception e) {
            log.warn("Gemini lỗi — fallback: {}", e.getMessage());
            return generateRuleBasedResponse(noiDung, phien, catalog);
        }
    }

    @jakarta.annotation.PostConstruct
    void logGeminiConfig() {
        String key = resolveGeminiApiKey();
        log.info("Chat AI Gemini: model={}, keyConfigured={}", geminiModel, key != null);
    }

    private String resolveGeminiApiKey() {
        if (geminiApiKey != null) {
            String trimmed = geminiApiKey.trim();
            if (!trimmed.isEmpty() && !"YOUR_API_KEY_HERE".equals(trimmed)) {
                return trimmed;
            }
        }
        String env = System.getenv("GEMINI_API_KEY");
        if (env != null) {
            String trimmed = env.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return null;
    }

    private String callGeminiGenerateContent(String model, String apiKey, String bodyJson) {
        String base = (geminiBaseUrl == null || geminiBaseUrl.isBlank())
                ? "https://generativelanguage.googleapis.com/v1beta"
                : geminiBaseUrl.trim().replaceAll("/+$", "");
        String url = base + "/models/" + model + ":generateContent";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8));
        headers.set("x-goog-api-key", apiKey);

        HttpEntity<String> httpRequest = new HttpEntity<>(bodyJson, headers);
        String responseStr = restTemplate.postForObject(url, httpRequest, String.class);
        if (responseStr == null || responseStr.isBlank()) {
            throw new IllegalStateException("Gemini trả về body rỗng");
        }

        try {
            JsonNode resNode = objectMapper.readTree(responseStr);
            if (resNode.has("error")) {
                String msg = resNode.path("error").path("message").asText("unknown error");
                throw new IllegalStateException(msg);
            }
            JsonNode parts = resNode.path("candidates").path(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                String blockReason = resNode.path("promptFeedback").path("blockReason").asText("");
                throw new IllegalStateException("Không có candidates/text"
                        + (blockReason.isBlank() ? "" : (" — blockReason=" + blockReason)));
            }
            StringBuilder aiTextBuilder = new StringBuilder();
            for (JsonNode part : parts) {
                String t = part.path("text").asText("");
                if (!t.isBlank()) {
                    if (!aiTextBuilder.isEmpty()) aiTextBuilder.append('\n');
                    aiTextBuilder.append(t);
                }
            }
            String aiText = aiTextBuilder.toString().trim();
            if (aiText.isBlank()) {
                throw new IllegalStateException("Gemini trả về text trống");
            }
            return aiText;
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception parseEx) {
            throw new IllegalStateException("Không parse được response Gemini: " + parseEx.getMessage(), parseEx);
        }
    }

    /** Ghép lịch sử KHACH→user, AI→model; gộp lượt liên tiếp cùng role để đúng format Gemini. */
    private void appendGeminiHistory(ArrayNode contents, List<TinNhanChatAi> history) {
        if (history == null || history.isEmpty()) {
            return;
        }
        String pendingRole = null;
        StringBuilder pendingText = new StringBuilder();
        for (TinNhanChatAi old : history) {
            String role = "AI".equalsIgnoreCase(old.getNguoiGui()) ? "model" : "user";
            String text = old.getNoiDung() != null ? old.getNoiDung().trim() : "";
            if (text.isBlank()) {
                continue;
            }
            if (pendingRole == null) {
                pendingRole = role;
                pendingText.append(text);
            } else if (pendingRole.equals(role)) {
                pendingText.append("\n").append(text);
            } else {
                flushGeminiTurn(contents, pendingRole, pendingText.toString());
                pendingRole = role;
                pendingText.setLength(0);
                pendingText.append(text);
            }
        }
        if (pendingRole != null && !pendingText.isEmpty()) {
            flushGeminiTurn(contents, pendingRole, pendingText.toString());
        }
        // Gemini thường yêu cầu contents bắt đầu bằng user — bỏ các model đứng đầu nếu có
        while (!contents.isEmpty() && "model".equals(contents.get(0).path("role").asText())) {
            contents.remove(0);
        }
    }

    private void flushGeminiTurn(ArrayNode contents, String role, String text) {
        ObjectNode turn = contents.addObject();
        turn.put("role", role);
        turn.putArray("parts").addObject().put("text", text);
    }

    private String truncateForLog(String body) {
        if (body == null) return "";
        String clean = body.replaceAll("(?i)(AQ\\.[A-Za-z0-9_-]+)", "[REDACTED_KEY]");
        return clean.length() > 300 ? clean.substring(0, 300) + "…" : clean;
    }

    private String buildSystemPrompt(String catalogText) {
        return """
                Bạn là chuyên viên tư vấn chống nắng của cửa hàng mỹ phẩm SUNOVA.
                Trả lời BẰNG TIẾNG VIỆT, ngắn gọn, thân thiện, dễ hiểu.

                === NHÓM CÂU HỎI KIẾN THỨC ===
                Bạn ĐƯỢC PHÉP trả lời kiến thức mà KHÔNG cần gợi ý sản phẩm, ví dụ:
                - Ban đêm có cần bôi kem chống nắng không?
                - SPF / PA là gì?
                - Bôi lại sau bao lâu?
                - Chống nắng vật lý khác hóa học chỗ nào?
                - Ngồi trong nhà / văn phòng có cần bôi không?
                Với các câu này: trả lời đúng kiến thức, KHÔNG ép nhét sản phẩm, KHÔNG gắn [PRODUCT_ID: x].

                === NHÓM CÂU HỎI THEO ĐỊA ĐIỂM / KHÍ HẬU VN ===
                Ví dụ: "ở Gia Lâm, Hà Nội nên dùng loại nào".
                Bối cảnh Việt Nam: khí hậu nhiệt đới, nắng gắt, UV thường cao (đặc biệt 10h–15h),
                độ ẩm cao, dễ đổ mồ hôi. Ưu tiên SPF50+/PA++++, kết cấu mỏng nhẹ/kiềm dầu,
                kháng nước nếu ra ngoài lâu. Sau đó mới chọn sản phẩm khớp từ danh sách.

                === GỢI Ý SẢN PHẨM ===
                Gắn [PRODUCT_ID: x] cho mỗi sản phẩm cụ thể được gợi ý (có thể nhiều ID).
                Không có sản phẩm phù hợp thì nói thật, KHÔNG bịa ID / thông số.
                TUYỆT ĐỐI không bịa SPF/PA/thành phần ngoài danh sách được cấp.
                Không tư vấn y tế hay thuốc chữa bệnh; da có vấn đề nghiêm trọng -> khuyên gặp bác sĩ da liễu.

                === DANH SÁCH SẢN PHẨM ĐANG BÁN (còn hàng) ===
                %s
                """.formatted(catalogText.isBlank() ? "(Hiện không có sản phẩm còn hàng.)" : catalogText);
    }

    private TinNhanChatAi generateRuleBasedResponse(
            String noiDung, PhienChatAi phien, List<ProductCatalogItem> catalog) {
        TinNhanChatAi ai = new TinNhanChatAi();
        ai.setPhienChatAi(phien);
        ai.setNguoiGui("AI");
        ai.setThoiGian(LocalDateTime.now());

        String nd = noiDung != null ? noiDung.toLowerCase(Locale.ROOT) : "";

        // Kiến thức — không ép sản phẩm
        if (containsAny(nd, "ban đêm", "ban dem", "đi ngủ", "di ngu", "tối có cần", "toi có cần")) {
            ai.setNoiDung("Ban đêm thường không cần kem chống nắng vì không còn bức xạ UV mặt trời. "
                    + "Tập trung dưỡng ẩm / phục hồi. Buổi sáng mới cần chống nắng lại.");
            return ai;
        }
        if (containsAny(nd, "spf là gì", "pa là gì", "spf/pa", "spf pa", "chỉ số spf", "chi so spf")) {
            ai.setNoiDung("SPF cho biết khả năng chống UVB (cháy nắng). PA (+ đến ++++) cho biết mức chống UVA (lão hóa). "
                    + "Ra nắng nhiều / ngoài trời lâu nên ưu tiên SPF50+ và PA++++.");
            return ai;
        }
        if (containsAny(nd, "bôi lại", "boi lai", "bao lâu", "bao lau", "reapply")) {
            ai.setNoiDung("Nên bôi lại khoảng mỗi 2–3 giờ khi ở ngoài trời, hoặc sớm hơn nếu đổ mồ hôi nhiều / bơi / lau mặt. "
                    + "Trong văn phòng ít ra ngoài có thể bôi sáng và khi ra ngoài.");
            return ai;
        }
        if (containsAny(nd, "vật lý", "vat ly", "hóa học", "hoa hoc", "khác nhau")) {
            ai.setNoiDung("Chống nắng vật lý (Zinc/Titanium) phản xạ tia, thường lành tính hơn với da nhạy cảm. "
                    + "Hóa học hấp thụ tia, thường mỏng nhẹ hơn. Lai kết hợp cả hai. Chọn theo loại da và cảm giác trên da.");
            return ai;
        }
        if (containsAny(nd, "trong nhà", "văn phòng", "van phong", "ngồi nhà", "ngoi nha")) {
            ai.setNoiDung("Trong nhà vẫn có thể nhận UVA qua cửa kính và ánh sáng xanh. Nếu ngồi gần cửa sổ hoặc "
                    + "ra ngoài trong ngày, vẫn nên bôi chống nắng buổi sáng với lớp mỏng nhẹ.");
            return ai;
        }
        if (containsAny(nd, "chào", "chao", "hello", "hi", "hii", "xin chào", "xin chao", "hey")) {
            ai.setNoiDung("Chào bạn! Tôi là chuyên viên tư vấn chống nắng SUNOVA. "
                    + "Bạn muốn hỏi kiến thức (SPF/PA, cách bôi…) hay cần gợi ý sản phẩm theo loại da?");
            return ai;
        }

        // Gợi ý theo loại da / công dụng thật
        ProductCatalogItem match = null;
        String advice = null;

        if (containsAny(nd, "da dầu", "da dau", "kiềm dầu", "kiem dau", "đổ dầu", "do dau")) {
            advice = "Với da dầu / đổ dầu, nên chọn kết cấu mỏng nhẹ, kiềm dầu, SPF50+/PA++++, tránh lớp quá dày.";
            match = findBySkinOrBenefit(catalog, List.of("dầu", "dau", "hỗn hợp", "hon hop"),
                    List.of("kiềm dầu", "kiem dau", "mỏng nhẹ", "mong nhe"));
        } else if (containsAny(nd, "da mụn", "da mun", "trị mụn", "tri mun", "mẩn đỏ", "man do")) {
            advice = "Da mụn nên ưu tiên dịu nhẹ, không gây bí, thành phần lành tính; tránh kích ứng.";
            match = findBySkinOrBenefit(catalog, List.of("mụn", "mun", "nhạy cảm", "nhay cam"),
                    List.of("dịu", "dieu", "phục hồi", "phuc hoi", "mụn", "mun"));
        } else if (containsAny(nd, "da khô", "da kho", "cấp ẩm", "cap am", "dưỡng ẩm", "duong am")) {
            advice = "Da khô cần chống nắng kèm cấp ẩm (HA/Ceramide nếu có), tránh cảm giác căng da.";
            match = findBySkinOrBenefit(catalog, List.of("khô", "kho"),
                    List.of("cấp ẩm", "cap am", "dưỡng ẩm", "duong am"));
        } else if (containsAny(nd, "nhạy cảm", "nhay cam", "kích ứng", "kich ung")) {
            advice = "Da nhạy cảm nên ưu tiên vật lý hoặc công thức dịu nhẹ, ít hương liệu.";
            match = findBySkinOrBenefit(catalog, List.of("nhạy cảm", "nhay cam"),
                    List.of("vật lý", "vat ly", "dịu", "dieu", "nhạy cảm", "nhay cam"));
        } else if (containsAny(nd, "hỗn hợp", "hon hop", "chữ t", "chu t")) {
            advice = "Da hỗn hợp thường đổ dầu vùng T; ưu tiên dạng sữa/gel mỏng nhẹ, cân bằng ẩm.";
            match = findBySkinOrBenefit(catalog, List.of("hỗn hợp", "hon hop"),
                    List.of("mỏng nhẹ", "mong nhe", "gel", "sữa", "sua"));
        } else if (containsAny(nd, "nâng tone", "nang tone", "trắng", "trang", "sáng da", "sang da")) {
            advice = "Nếu cần nâng tone / sáng da, chọn sản phẩm có công dụng nâng tone trong danh mục SUNOVA.";
            match = findBySkinOrBenefit(catalog, List.of(),
                    List.of("nâng tone", "nang tone", "trắng", "trang", "sáng", "sang"));
        } else if (isVietnamLocationOrRecommendAsk(nd)) {
            advice = "Ở Việt Nam (nắng gắt, UV cao 10h–15h, ẩm/mồ hôi), nên ưu tiên SPF50+/PA++++, "
                    + "kết cấu mỏng nhẹ/kiềm dầu; ra ngoài lâu nên chọn kháng nước.";
            match = findBestForVietnamClimate(catalog);
        } else if (containsAny(nd, "giá bao", "gia bao", "bao nhiêu", "bao nhieu", "giá sp", "giá sản")) {
            ai.setNoiDung("Giá kem chống nắng SUNOVA thường dao động theo dung tích và thương hiệu. "
                    + "Bạn cho mình biết loại da hoặc ngân sách để gợi ý phù hợp hơn nhé.");
            return ai;
        }

        if (advice != null) {
            if (match != null) {
                ai.setNoiDung(advice + " Bạn có thể tham khảo: " + match.ten() + ".");
                ai.setSanPhamGoiY(sanPhamRepository.findById(match.id()).orElse(null));
            } else {
                ai.setNoiDung(advice + " Hiện mình chưa tìm thấy sản phẩm khớp đủ tiêu chí trong kho còn hàng. "
                        + "Bạn mô tả thêm loại da / ngân sách để mình tư vấn tiếp nhé.");
            }
            return ai;
        }

        ai.setNoiDung("Cảm ơn bạn đã nhắn. Bạn có thể hỏi kiến thức (SPF/PA, cách bôi, ban đêm…) "
                + "hoặc cho mình biết loại da (dầu, khô, mụn, nhạy cảm…) để gợi ý sản phẩm phù hợp.");
        return ai;
    }

    /** Câu hỏi theo địa điểm / “đang ở …” / “dùng kem chống nắng nào”. */
    private boolean isVietnamLocationOrRecommendAsk(String nd) {
        if (containsAny(nd,
                "gia lâm", "gia lam", "hà nội", "ha noi", "đà nẵng", "da nang",
                "tp.hcm", "sài gòn", "sai gon", "hcm", "ngoài trời", "ngoai troi",
                "hoàng hoa thám", "hoang hoa tham", "cầu giấy", "cau giay",
                "thanh xuân", "thanh xuan", "đống đa", "dong da", "ba đình", "ba dinh",
                "uv cao", "nắng gắt", "nang gat")) {
            return true;
        }
        boolean askWhich = containsAny(nd, "dùng gì", "dung gi", "dùng nào", "dung nao",
                "loại nào", "loai nao", "nên dùng", "nen dung", "gợi ý", "goi y");
        boolean hasPlaceCue = containsAny(nd, "đang ở", "dang o", "mình ở", "minh o", "tôi ở", "toi o",
                "ở ", " o ", "tại ", "tai ");
        boolean sunscreen = containsAny(nd, "chống nắng", "chong nang", "kcn", "kem chống");
        return askWhich && (hasPlaceCue || sunscreen);
    }

    private ProductCatalogItem findBestForVietnamClimate(List<ProductCatalogItem> catalog) {
        return catalog.stream()
                .filter(c -> isHighSpf(c.spf()) && isHighPa(c.pa()))
                .sorted((a, b) -> Boolean.compare(
                        Boolean.TRUE.equals(b.khangNuoc()),
                        Boolean.TRUE.equals(a.khangNuoc())))
                .findFirst()
                .or(() -> catalog.stream().filter(c -> isHighSpf(c.spf())).findFirst())
                .orElse(null);
    }

    private ProductCatalogItem findBySkinOrBenefit(
            List<ProductCatalogItem> catalog,
            List<String> skinKeys,
            List<String> benefitKeys) {
        for (ProductCatalogItem item : catalog) {
            String skin = normalize(item.loaiDa());
            String benefits = normalize(item.congDung() + " " + item.thanhPhan() + " " + item.ten());
            boolean skinOk = skinKeys.isEmpty() || skinKeys.stream().anyMatch(skin::contains);
            boolean benefitOk = benefitKeys.isEmpty() || benefitKeys.stream().anyMatch(benefits::contains);
            if (skinOk && benefitOk) {
                return item;
            }
        }
        for (ProductCatalogItem item : catalog) {
            String skin = normalize(item.loaiDa());
            if (!skinKeys.isEmpty() && skinKeys.stream().anyMatch(skin::contains)) {
                return item;
            }
        }
        for (ProductCatalogItem item : catalog) {
            String benefits = normalize(item.congDung());
            if (!benefitKeys.isEmpty() && benefitKeys.stream().anyMatch(benefits::contains)) {
                return item;
            }
        }
        return null;
    }

    private boolean isHighSpf(String spf) {
        if (spf == null) return false;
        String s = spf.toUpperCase(Locale.ROOT).replace("SPF", "").trim();
        try {
            return Integer.parseInt(s.replace("+", "").replaceAll("[^0-9]", "")) >= 50;
        } catch (Exception e) {
            return s.contains("50");
        }
    }

    private boolean isHighPa(String pa) {
        if (pa == null) return false;
        String s = pa.toUpperCase(Locale.ROOT).replace("PA", "").trim();
        return s.contains("++++") || s.equals("++++");
    }

    private boolean containsAny(String text, String... keys) {
        if (text == null) return false;
        for (String k : keys) {
            if (text.contains(k)) return true;
        }
        return false;
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT);
    }

    private List<ProductCatalogItem> loadActiveCatalog() {
        CachedCatalog cached = catalogCache.get();
        long now = Instant.now().toEpochMilli();
        if (cached != null && now - cached.loadedAtMs() < CATALOG_TTL_MS) {
            return cached.items();
        }

        List<SanPhamResponse> all = sanPhamService.getAll().stream()
                .filter(sp -> Boolean.TRUE.equals(sp.getTrangThai()))
                .filter(sp -> sp.getTongTon() != null && sp.getTongTon() > 0)
                .toList();

        Map<Integer, Set<String>> congDungMap = new HashMap<>();
        for (SanPhamCongDung row : sanPhamCongDungRepository.findAllWithCongDung()) {
            if (row.getSanPham() == null || row.getCongDung() == null) continue;
            congDungMap.computeIfAbsent(row.getSanPham().getId(), k -> new LinkedHashSet<>())
                    .add(row.getCongDung().getTen());
        }

        Map<Integer, Set<String>> loaiDaMap = new HashMap<>();
        for (SanPhamLoaiDa row : sanPhamLoaiDaRepository.findAllWithLoaiDa()) {
            if (row.getSanPham() == null || row.getLoaiDa() == null) continue;
            loaiDaMap.computeIfAbsent(row.getSanPham().getId(), k -> new LinkedHashSet<>())
                    .add(row.getLoaiDa().getTen());
        }

        Map<Integer, Set<String>> thanhPhanMap = new HashMap<>();
        for (SanPhamThanhPhan row : sanPhamThanhPhanRepository.findAllWithThanhPhan()) {
            if (row.getSanPham() == null || row.getThanhPhan() == null) continue;
            thanhPhanMap.computeIfAbsent(row.getSanPham().getId(), k -> new LinkedHashSet<>())
                    .add(row.getThanhPhan().getTen());
        }

        Map<Integer, String> dungTichMap = new HashMap<>();
        for (ChiTietSanPhamRepository.DungTichAgg agg : chiTietSanPhamRepository.aggregateDungTich()) {
            dungTichMap.put(agg.getSpId(), formatDungTich(agg.getDungTichMin(), agg.getDungTichMax()));
        }

        List<ProductCatalogItem> items = new ArrayList<>();
        for (SanPhamResponse sp : all) {
            items.add(new ProductCatalogItem(
                    sp.getId(),
                    sp,
                    sp.getTen(),
                    nullToDash(sp.getTenThuongHieu()),
                    nullToDash(sp.getTenDangSanPham()),
                    nullToDash(sp.getChiSoSpf()),
                    nullToDash(sp.getChiSoPa()),
                    formatLoaiChongNang(sp.getLoaiChongNang()),
                    Boolean.TRUE.equals(sp.getKhangNuoc()),
                    joinNames(congDungMap.get(sp.getId())),
                    joinNames(loaiDaMap.get(sp.getId())),
                    joinNames(thanhPhanMap.get(sp.getId())),
                    dungTichMap.getOrDefault(sp.getId(), "—"),
                    formatGia(sp.getGiaMin())
            ));
        }

        catalogCache.set(new CachedCatalog(items, now));
        return items;
    }

    private Map<Integer, SanPhamResponse> catalogById() {
        return loadActiveCatalog().stream()
                .collect(Collectors.toMap(ProductCatalogItem::id, ProductCatalogItem::response, (a, b) -> a));
    }

    private String joinNames(Set<String> names) {
        if (names == null || names.isEmpty()) return "—";
        return String.join(", ", names);
    }

    private String nullToDash(String s) {
        return s == null || s.isBlank() ? "—" : s.trim();
    }

    private String formatLoaiChongNang(String raw) {
        if (raw == null || raw.isBlank()) return "—";
        String v = raw.trim().toLowerCase(Locale.ROOT);
        return switch (v) {
            case "vat_ly", "vật lý", "vat ly" -> "Vật lý";
            case "hoa_hoc", "hóa học", "hoa hoc" -> "Hóa học";
            case "lai", "hybrid" -> "Lai";
            default -> raw;
        };
    }

    private String formatDungTich(BigDecimal min, BigDecimal max) {
        if (min == null && max == null) return "—";
        if (min != null && (max == null || min.compareTo(max) == 0)) {
            return strip(min) + "ml";
        }
        return strip(min) + "-" + strip(max) + "ml";
    }

    private String strip(BigDecimal v) {
        if (v == null) return "?";
        return v.stripTrailingZeros().toPlainString();
    }

    private String formatGia(BigDecimal gia) {
        if (gia == null) return "—";
        return VND.format(gia.setScale(0, RoundingMode.HALF_UP)) + "đ";
    }
    private ChatResponseDto mapToDto(TinNhanChatAi entity, Map<Integer, SanPhamResponse> byId) {
        java.util.List<SanPhamResponse> listSpRes = new java.util.ArrayList<>();
        
        // Hỗ trợ cột cũ (nếu có)
        if (entity.getSanPhamGoiY() != null) {
            SanPhamResponse spRes = byId.get(entity.getSanPhamGoiY().getId());
            if (spRes != null) listSpRes.add(spRes);
        }

        // Hỗ trợ cột mới (nhiều ID)
        if (entity.getDanhSachSpGoiY() != null && !entity.getDanhSachSpGoiY().isEmpty()) {
            String[] ids = entity.getDanhSachSpGoiY().split(",");
            for (String idStr : ids) {
                try {
                    Integer id = Integer.parseInt(idStr.trim());
                    SanPhamResponse res = byId.get(id);
                    if (res != null && listSpRes.stream().noneMatch(s -> s.getId().equals(id))) {
                        listSpRes.add(res);
                    }
                } catch (Exception ignored) {}
            }
        }

        return ChatResponseDto.builder()
                .idTinNhan(entity.getId())
                .idPhien(entity.getPhienChatAi().getId())
                .nguoiGui(entity.getNguoiGui())
                .noiDung(entity.getNoiDung())
                .sanPhamGoiY(listSpRes.isEmpty() ? null : listSpRes.get(0)) // Giữ tương thích cũ
                .danhSachSanPhamGoiY(listSpRes) // Trả về danh sách đầy đủ
                .thoiGian(entity.getThoiGian() != null ? entity.getThoiGian() : LocalDateTime.now())
                .build();
    }

    private record CachedCatalog(List<ProductCatalogItem> items, long loadedAtMs) {}

    private record ProductCatalogItem(
            Integer id,
            SanPhamResponse response,
            String ten,
            String thuongHieu,
            String dang,
            String spf,
            String pa,
            String loaiChongNang,
            Boolean khangNuoc,
            String congDung,
            String loaiDa,
            String thanhPhan,
            String dungTich,
            String gia
    ) {
        String promptLine() {
            return "- [PRODUCT_ID: " + id + "] " + ten
                    + " | " + thuongHieu
                    + " | " + dang
                    + " | " + (spf.startsWith("SPF") || "—".equals(spf) ? spf : "SPF" + spf)
                    + " | " + (pa.startsWith("PA") || "—".equals(pa) ? pa : "PA" + pa)
                    + " | " + loaiChongNang
                    + " | " + (Boolean.TRUE.equals(khangNuoc) ? "Kháng nước" : "Không kháng nước")
                    + " | Công dụng: " + congDung
                    + " | Da: " + loaiDa
                    + " | TP: " + thanhPhan
                    + " | " + dungTich
                    + " | " + gia;
        }
    }
}
