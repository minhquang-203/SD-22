package org.example.templatejava6.chat.service;

import org.example.templatejava6.chat.dto.ChatRequestDto;
import org.example.templatejava6.chat.dto.ChatResponseDto;
import org.example.templatejava6.chat.entity.PhienChatAi;
import org.example.templatejava6.chat.entity.TinNhanChatAi;
import org.example.templatejava6.chat.repository.PhienChatAiRepository;
import org.example.templatejava6.chat.repository.TinNhanChatAiRepository;
import org.example.templatejava6.common.util.MapperUtil;
import org.example.templatejava6.common.entity.KhachHang;
import org.example.templatejava6.customer.repository.KhachHangRepository;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.model.response.SanPhamResponse;
import org.example.templatejava6.product.repository.SanPhamCongDungRepository;
import org.example.templatejava6.product.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatAiService {

    @Autowired private PhienChatAiRepository phienChatAiRepository;
    @Autowired private TinNhanChatAiRepository tinNhanChatAiRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private org.example.templatejava6.product.service.SanPhamService sanPhamService;
    @Autowired private SanPhamRepository sanPhamRepository;

    @Value("${app.gemini.api-key:}")
    private String geminiApiKey;

    @Transactional
    public PhienChatAi taoPhienMoi(Integer idKhachHang) {
        PhienChatAi phien = new PhienChatAi();
        if (idKhachHang != null) {
            KhachHang kh = khachHangRepository.findById(idKhachHang).orElse(null);
            phien.setKhachHang(kh);
        }
        phien.setTieuDe("Chat tư vấn ngày " + LocalDateTime.now().toLocalDate().toString());
        phien.setTrangThai("DANG_MO");
        phien.setThoiGianBatDau(LocalDateTime.now());
        return phienChatAiRepository.save(phien);
    }

    public List<PhienChatAi> getPhienCuaKhachHang(Integer idKhachHang) {
        return phienChatAiRepository.findByKhachHangIdOrderByThoiGianBatDauDesc(idKhachHang);
    }

    public List<ChatResponseDto> getLichSuTinNhan(Integer idPhien) {
        return tinNhanChatAiRepository.findByPhienChatAiIdOrderByThoiGianAsc(idPhien)
                .stream()
                .map(this::mapToDto)
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

        // Lưu tin nhắn của Khách
        TinNhanChatAi tinKhach = new TinNhanChatAi();
        tinKhach.setPhienChatAi(phien);
        tinKhach.setNguoiGui("KHACH");
        tinKhach.setNoiDung(request.getNoiDung());
        tinKhach.setThoiGian(LocalDateTime.now());
        tinNhanChatAiRepository.save(tinKhach);

        // Sinh câu trả lời của AI
        TinNhanChatAi tinAi = generateGeminiResponse(request.getNoiDung(), phien);
        tinNhanChatAiRepository.save(tinAi);

        return mapToDto(tinAi);
    }

    private TinNhanChatAi generateGeminiResponse(String noiDung, PhienChatAi phien) {
        TinNhanChatAi ai = new TinNhanChatAi();
        ai.setPhienChatAi(phien);
        ai.setNguoiGui("AI");
        ai.setThoiGian(LocalDateTime.now());

        if (geminiApiKey == null || geminiApiKey.isEmpty() || geminiApiKey.equals("YOUR_API_KEY_HERE")) {
            return generateRuleBasedResponse(noiDung, phien); // Fallback
        }

        try {
            List<SanPhamResponse> tatCaSp = sanPhamService.getAll();

            StringBuilder sb = new StringBuilder();
            for (SanPhamResponse sp : tatCaSp) {
                sb.append("- [PRODUCT_ID: ").append(sp.getId()).append("] ")
                  .append(sp.getTen()).append(" | Giá: ").append(sp.getGiaMin())
                  .append("\n");
            }

            String systemPrompt = "Bạn là trợ lý AI tư vấn bán hàng của cửa hàng mỹ phẩm SUNOVA. " +
                    "Hãy tư vấn thân thiện, ngắn gọn và tự nhiên. Dưới đây là danh sách sản phẩm kem chống nắng của cửa hàng:\n" +
                    sb.toString() +
                    "\nKhi bạn quyết định gợi ý một sản phẩm nào đó cho khách, HÃY GHI KÈM mã ID của sản phẩm đó ở dạng `[PRODUCT_ID: xxx]` vào cuối câu trả lời của bạn. Chỉ được tư vấn các sản phẩm trong danh sách.";

            RestTemplate restTemplate = new RestTemplate();
            
            // Sử dụng Pollinations AI (Miễn phí, không cần API Key)
            String url = "https://text.pollinations.ai/openai";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode rootNode = mapper.createObjectNode();
            
            ArrayNode messagesNode = mapper.createArrayNode();
            
            // System message
            ObjectNode sysMessage = mapper.createObjectNode();
            sysMessage.put("role", "system");
            sysMessage.put("content", systemPrompt);
            messagesNode.add(sysMessage);
            
            // User message
            ObjectNode userMessage = mapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", noiDung);
            messagesNode.add(userMessage);

            rootNode.set("messages", messagesNode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(rootNode), headers);

            String responseStr = restTemplate.postForObject(url, request, String.class);
            JsonNode resNode = mapper.readTree(responseStr);
            
            String aiText = resNode.path("choices").get(0).path("message").path("content").asText();
            
            SanPham spGoiY = null;
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[PRODUCT_ID:\\s*(\\d+)\\]");
            java.util.regex.Matcher matcher = pattern.matcher(aiText);
            if (matcher.find()) {
                String idStr = matcher.group(1);
                Integer idSp = Integer.parseInt(idStr);
                spGoiY = sanPhamRepository.findById(idSp).orElse(null);
                aiText = aiText.replace(matcher.group(0), "").trim();
            }

            ai.setNoiDung(aiText);
            ai.setSanPhamGoiY(spGoiY);

        } catch (Exception e) {
            e.printStackTrace();
            ai.setNoiDung("Xin lỗi, tôi đang gặp sự cố kết nối với máy chủ AI. Vui lòng thử lại sau!");
        }

        return ai;
    }

    private TinNhanChatAi generateRuleBasedResponse(String noiDung, PhienChatAi phien) {
        TinNhanChatAi ai = new TinNhanChatAi();
        ai.setPhienChatAi(phien);
        ai.setNguoiGui("AI");
        ai.setThoiGian(LocalDateTime.now());

        String ndLower = noiDung.toLowerCase();
        List<SanPham> tatCaSp = sanPhamRepository.findAll().stream()
                .filter(sp -> sp.getTrangThai() != null && sp.getTrangThai())
                .toList();

        SanPham spGoiY = null;

        if (ndLower.contains("da dầu") || ndLower.contains("kiềm dầu")) {
            ai.setNoiDung("Đối với làn da dầu, bạn cần một loại kem chống nắng có kết cấu mỏng nhẹ, thấm nhanh và khả năng kiềm dầu tốt để tránh gây bít tắc lỗ chân lông. SUNOVA xin gợi ý sản phẩm này cho bạn nhé!");
            spGoiY = findSanPhamByKeyword(tatCaSp, "kiềm dầu", "mỏng nhẹ");
        } else if (ndLower.contains("da mụn") || ndLower.contains("trị mụn") || ndLower.contains("mẩn đỏ")) {
            ai.setNoiDung("Làn da mụn rất nhạy cảm, bạn nên chọn kem chống nắng chứa các thành phần làm dịu như Centella (Rau má) hoặc Niacinamide, và đặc biệt là không chứa cồn. Đây là sản phẩm hoàn hảo cho làn da của bạn:");
            spGoiY = findSanPhamByKeyword(tatCaSp, "dịu nhẹ", "phục hồi", "mụn");
        } else if (ndLower.contains("da khô") || ndLower.contains("cấp ẩm")) {
            ai.setNoiDung("Với làn da khô, kem chống nắng cần có bổ sung Hyaluronic Acid hoặc Ceramide để duy trì độ ẩm suốt ngày dài, tránh tình trạng mốc nền (cakey). Mời bạn tham khảo sản phẩm này:");
            spGoiY = findSanPhamByKeyword(tatCaSp, "cấp ẩm", "dưỡng ẩm");
        } else if (ndLower.contains("nhạy cảm") || ndLower.contains("kích ứng")) {
            ai.setNoiDung("Da nhạy cảm nên ưu tiên kem chống nắng vật lý (chứa ZinC Oxide, Titanium Dioxide) vì nó lành tính và ít gây kích ứng hơn kem chống nắng hóa học. Hãy thử sản phẩm này nhé:");
            spGoiY = findSanPhamByKeyword(tatCaSp, "vật lý", "nhạy cảm");
        } else if (ndLower.contains("hỗn hợp")) {
            ai.setNoiDung("Da hỗn hợp thường đổ dầu vùng chữ T nhưng lại khô ở hai bên má. Bạn nên chọn loại kem chống nắng dạng sữa (milk) hoặc gel mỏng nhẹ, có khả năng cân bằng ẩm tốt. Thử tham khảo loại này nhé:");
            spGoiY = findSanPhamByKeyword(tatCaSp, "mỏng nhẹ", "gel", "sữa");
        } else if (ndLower.contains("trắng") || ndLower.contains("nâng tone") || ndLower.contains("sáng da")) {
            ai.setNoiDung("Nếu bạn muốn một loại kem chống nắng vừa bảo vệ da vừa có tác dụng nâng tone rạng rỡ thay lớp trang điểm, thì đây là sự lựa chọn tuyệt vời dành cho bạn:");
            spGoiY = findSanPhamByKeyword(tatCaSp, "nâng tone", "trắng", "sáng");
        } else if (ndLower.contains("giá") || ndLower.contains("bao nhiêu")) {
            ai.setNoiDung("Các sản phẩm kem chống nắng của SUNOVA có mức giá cực kỳ học sinh sinh viên, dao động từ 150.000đ đến 450.000đ tùy loại và dung tích. Bạn có muốn tư vấn chi tiết hơn cho loại da nào không?");
        } else if (ndLower.contains("chào") || ndLower.contains("hello")) {
            ai.setNoiDung("Chào bạn! Tôi là Trợ lý AI của SUNOVA. Bạn cần tư vấn kem chống nắng cho loại da nào (da dầu, da khô, da mụn...), hay có thắc mắc gì về sản phẩm không?");
        } else {
            ai.setNoiDung("Cảm ơn bạn đã nhắn tin. Để tôi tư vấn chính xác nhất, bạn có thể chia sẻ thêm về loại da của mình (da dầu, mụn, khô...) hoặc nhu cầu cụ thể được không?");
        }

        if (spGoiY == null && !tatCaSp.isEmpty() && ai.getNoiDung().contains("gợi ý")) {
            spGoiY = tatCaSp.get(0); // Lấy tạm sản phẩm đầu tiên nếu không tìm thấy
        }
        
        ai.setSanPhamGoiY(spGoiY);
        return ai;
    }

    private SanPham findSanPhamByKeyword(List<SanPham> spList, String... keywords) {
        for (SanPham sp : spList) {
            String moTa = sp.getMoTa() != null ? sp.getMoTa().toLowerCase() : "";
            for (String kw : keywords) {
                if (moTa.contains(kw)) {
                    return sp;
                }
            }
        }
        return spList.isEmpty() ? null : spList.get(0);
    }

    private ChatResponseDto mapToDto(TinNhanChatAi entity) {
        SanPhamResponse spRes = null;
        if (entity.getSanPhamGoiY() != null) {
            // Re-fetch using service to get all mapped fields like anhUrl
            List<SanPhamResponse> allSp = sanPhamService.getAll();
            spRes = allSp.stream()
                .filter(s -> s.getId().equals(entity.getSanPhamGoiY().getId()))
                .findFirst()
                .orElse(null);
        }

        return ChatResponseDto.builder()
                .idTinNhan(entity.getId())
                .idPhien(entity.getPhienChatAi().getId())
                .nguoiGui(entity.getNguoiGui())
                .noiDung(entity.getNoiDung())
                .sanPhamGoiY(spRes)
                .thoiGian(entity.getThoiGian() != null ? entity.getThoiGian() : LocalDateTime.now())
                .build();
    }
}
