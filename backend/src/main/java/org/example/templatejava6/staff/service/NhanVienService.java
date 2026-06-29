package org.example.templatejava6.staff.service;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.model.VaiTro;
import org.example.templatejava6.common.repository.VaiTroRepository;
import org.example.templatejava6.common.security.SecurityUtils;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.order.repository.NhanVienRepository;
import org.example.templatejava6.staff.model.request.NhanVienCreateRequest;
import org.example.templatejava6.staff.model.request.NhanVienDatLaiMatKhauRequest;
import org.example.templatejava6.staff.model.request.NhanVienTrangThaiRequest;
import org.example.templatejava6.staff.model.request.NhanVienUpdateRequest;
import org.example.templatejava6.staff.model.response.DatLaiMatKhauResponse;
import org.example.templatejava6.staff.model.response.NhanVienResponse;
import org.example.templatejava6.staff.util.VaiTroRank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private VaiTroRepository vaiTroRepository;

    @Transactional(readOnly = true)
    public List<NhanVienResponse> danhSachHoatDong() {
        return nhanVienRepository.findActiveWithVaiTro().stream()
                .map(NhanVienResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NhanVienResponse> danhSachQuanLy() {
        return nhanVienRepository.findAllWithVaiTro().stream()
                .map(NhanVienResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public NhanVienResponse chiTiet(Integer id) {
        NhanVien actor = currentActor();
        NhanVien target = resolveNhanVien(id);
        VaiTroRank.assertCanManage(actor, target);
        return new NhanVienResponse(target);
    }

    @Transactional
    public NhanVienResponse taoMoi(NhanVienCreateRequest request) {
        NhanVien actor = currentActor();
        VaiTroRank.assertCanAssignRole(actor, request.getMaVaiTro());

        String email = normalizeEmail(request.getEmail());
        String sdt = normalizePhone(request.getSoDienThoai());
        validateUniqueContact(email, sdt, null);

        NhanVien nv = new NhanVien();
        nv.setMaNhanVien(sinhMaNhanVien());
        nv.setHoTen(request.getHoTen().trim());
        nv.setEmail(email);
        nv.setSoDienThoai(sdt);
        nv.setMatKhau(request.getMatKhau());
        nv.setVaiTro(resolveVaiTro(request.getMaVaiTro()));
        nv.setGioiTinh(request.getGioiTinh());
        nv.setNgayVaoLam(request.getNgayVaoLam());
        nv.setTrangThai(true);

        return new NhanVienResponse(nhanVienRepository.save(nv));
    }

    @Transactional
    public NhanVienResponse capNhat(Integer id, NhanVienUpdateRequest request) {
        NhanVien actor = currentActor();
        NhanVien nv = resolveNhanVien(id);
        VaiTroRank.assertCanManage(actor, nv);
        VaiTroRank.assertCanAssignRole(actor, request.getMaVaiTro());

        String email = normalizeEmail(request.getEmail());
        String sdt = normalizePhone(request.getSoDienThoai());
        validateUniqueContact(email, sdt, id);

        nv.setHoTen(request.getHoTen().trim());
        nv.setEmail(email);
        nv.setSoDienThoai(sdt);
        nv.setVaiTro(resolveVaiTro(request.getMaVaiTro()));
        nv.setGioiTinh(request.getGioiTinh());
        nv.setNgayVaoLam(request.getNgayVaoLam());

        return new NhanVienResponse(nhanVienRepository.save(nv));
    }

    @Transactional
    public NhanVienResponse doiTrangThai(Integer id, NhanVienTrangThaiRequest request) {
        NhanVien actor = currentActor();
        NhanVien nv = resolveNhanVien(id);
        VaiTroRank.assertCanManage(actor, nv);
        nv.setTrangThai(request.getTrangThai());
        return new NhanVienResponse(nhanVienRepository.save(nv));
    }

    @Transactional
    public DatLaiMatKhauResponse datLaiMatKhau(Integer id, NhanVienDatLaiMatKhauRequest request) {
        NhanVien actor = currentActor();
        NhanVien nv = resolveNhanVien(id);
        VaiTroRank.assertCanManage(actor, nv);

        String raw;
        String matKhauTam = null;

        if (Boolean.TRUE.equals(request.getSinhTuDong())) {
            raw = sinhMatKhauTam();
            matKhauTam = raw;
        } else {
            raw = request.getMatKhauMoi();
            if (raw == null || raw.isBlank()) {
                throw new ApiException("Mật khẩu mới là bắt buộc", "VALIDATION_ERROR");
            }
            if (raw.length() < 6) {
                throw new ApiException("Mật khẩu phải từ 6 đến 100 ký tự", "VALIDATION_ERROR");
            }
        }

        nv.setMatKhau(raw);
        nhanVienRepository.save(nv);

        String message = "Đã đặt lại mật khẩu cho " + nv.getHoTen();
        return new DatLaiMatKhauResponse(message, matKhauTam);
    }

    private NhanVien currentActor() {
        return resolveNhanVien(SecurityUtils.currentNhanVienId());
    }

    private String sinhMatKhauTam() {
        final String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder(10);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private NhanVien resolveNhanVien(Integer id) {
        return nhanVienRepository.findByIdWithVaiTro(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));
    }

    private VaiTro resolveVaiTro(String maVaiTro) {
        return vaiTroRepository.findByMaVaiTro(maVaiTro)
                .orElseThrow(() -> new ApiException("Vai trò không hợp lệ", "VALIDATION_ERROR"));
    }

    private void validateUniqueContact(String email, String sdt, Integer excludeId) {
        if (excludeId == null) {
            if (nhanVienRepository.existsByEmailIgnoreCase(email)) {
                throw new ApiException("Email đã được sử dụng", "DUPLICATE");
            }
            if (nhanVienRepository.existsBySoDienThoai(sdt)) {
                throw new ApiException("Số điện thoại đã được sử dụng", "DUPLICATE");
            }
            return;
        }

        if (nhanVienRepository.existsByEmailIgnoreCaseAndIdNot(email, excludeId)) {
            throw new ApiException("Email đã được sử dụng", "DUPLICATE");
        }
        if (nhanVienRepository.existsBySoDienThoaiAndIdNot(sdt, excludeId)) {
            throw new ApiException("Số điện thoại đã được sử dụng", "DUPLICATE");
        }
    }

    private String sinhMaNhanVien() {
        List<String> existing = nhanVienRepository.findAllMaNhanVien();
        return MaGenerator.nextCode("NV", existing);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ApiException("Email là bắt buộc", "VALIDATION_ERROR");
        }
        return email.trim().toLowerCase();
    }

    private String normalizePhone(String sdt) {
        if (sdt == null || sdt.isBlank()) {
            throw new ApiException("Số điện thoại là bắt buộc", "VALIDATION_ERROR");
        }
        return sdt.trim();
    }
}
