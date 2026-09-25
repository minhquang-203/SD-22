package org.example.templatejava6.category.service;

import org.example.templatejava6.category.entity.*;
import org.example.templatejava6.category.model.request.*;
import org.example.templatejava6.category.model.response.*;
import org.example.templatejava6.category.repository.*;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.model.response.MaTiepTheoResponse;
import org.example.templatejava6.common.util.MaGenerator;
import org.example.templatejava6.common.util.MapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    /** Entity thuộc tính không có ngayTao — id IDENTITY tăng dần = mới hơn. */
    private static final Sort ATTR_NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    @Autowired private DanhMucRepository danhMucRepository;
    @Autowired private ThuongHieuRepository thuongHieuRepository;
    @Autowired private DangSanPhamRepository dangSanPhamRepository;
    @Autowired private CongDungRepository congDungRepository;
    @Autowired private ThanhPhanRepository thanhPhanRepository;
    @Autowired private MauSacRepository mauSacRepository;

    // --- Danh mục ---
    public List<DanhMucResponse> getAllDanhMuc() {
        return danhMucRepository.findAll(ATTR_NEWEST_FIRST).stream().map(DanhMucResponse::new).toList();
    }

    public DanhMucResponse detailDanhMuc(Integer id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
        return new DanhMucResponse(dm);
    }

    public void addDanhMuc(DanhMucRequest request) {
        ensureTenUnique(danhMucRepository.existsByTenIgnoreCase(trimTen(request.getTen())), "Tên danh mục");
        DanhMuc dm = MapperUtil.map(request, DanhMuc.class);
        dm.setTen(trimTen(request.getTen()));
        dm.setMa(MaGenerator.nextCode("DM", danhMucRepository.findAll().stream().map(DanhMuc::getMa).toList()));
        dm.setTrangThai(true);
        danhMucRepository.save(dm);
    }

    public void updateDanhMuc(Integer id, DanhMucRequest request) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
        if (danhMucRepository.existsByMaAndIdNot(request.getMa(), id)) {
            throw new ApiException("Mã danh mục đã tồn tại", "DUPLICATE");
        }
        ensureTenUnique(danhMucRepository.existsByTenIgnoreCaseAndIdNot(trimTen(request.getTen()), id), "Tên danh mục");
        Boolean trangThai = dm.getTrangThai();
        MapperUtil.mapToExisting(request, dm);
        dm.setTen(trimTen(request.getTen()));
        dm.setId(id);
        dm.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : trangThai);
        danhMucRepository.save(dm);
    }

    public void deleteDanhMuc(Integer id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
        dm.setTrangThai(false);
        danhMucRepository.save(dm);
    }

    // --- Thương hiệu ---
    public List<ThuongHieuResponse> getAllThuongHieu() {
        return thuongHieuRepository.findAll(ATTR_NEWEST_FIRST).stream().map(ThuongHieuResponse::new).toList();
    }

    public ThuongHieuResponse detailThuongHieu(Integer id) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        return new ThuongHieuResponse(th);
    }

    public void addThuongHieu(ThuongHieuRequest request) {
        ensureTenUnique(thuongHieuRepository.existsByTenIgnoreCase(trimTen(request.getTen())), "Tên thương hiệu");
        ThuongHieu th = MapperUtil.map(request, ThuongHieu.class);
        th.setTen(trimTen(request.getTen()));
        th.setMa(MaGenerator.nextCode("TH", thuongHieuRepository.findAll().stream().map(ThuongHieu::getMa).toList()));
        th.setTrangThai(true);
        thuongHieuRepository.save(th);
    }

    public void updateThuongHieu(Integer id, ThuongHieuRequest request) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        ensureMaUnique(thuongHieuRepository.existsByMaAndIdNot(request.getMa(), id), "Mã thương hiệu");
        ensureTenUnique(thuongHieuRepository.existsByTenIgnoreCaseAndIdNot(trimTen(request.getTen()), id), "Tên thương hiệu");
        Boolean trangThai = th.getTrangThai();
        MapperUtil.mapToExisting(request, th);
        th.setTen(trimTen(request.getTen()));
        th.setId(id);
        th.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : trangThai);
        thuongHieuRepository.save(th);
    }

    public void deleteThuongHieu(Integer id) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        th.setTrangThai(false);
        thuongHieuRepository.save(th);
    }

    public ThuongHieu getThuongHieuOrThrow(Integer id) {
        return thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
    }

    // --- Dạng sản phẩm ---
    public List<DangSanPhamResponse> getAllDangSanPham() {
        return dangSanPhamRepository.findAll(ATTR_NEWEST_FIRST).stream().map(DangSanPhamResponse::new).toList();
    }

    public DangSanPhamResponse detailDangSanPham(Integer id) {
        DangSanPham d = dangSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND"));
        return new DangSanPhamResponse(d);
    }

    public void addDangSanPham(DangSanPhamRequest request) {
        ensureTenUnique(dangSanPhamRepository.existsByTenIgnoreCase(trimTen(request.getTen())), "Tên dạng sản phẩm");
        DangSanPham d = MapperUtil.map(request, DangSanPham.class);
        d.setTen(trimTen(request.getTen()));
        d.setMa(MaGenerator.nextCode("DSP", dangSanPhamRepository.findAll().stream().map(DangSanPham::getMa).toList()));
        dangSanPhamRepository.save(d);
    }

    public void updateDangSanPham(Integer id, DangSanPhamRequest request) {
        DangSanPham d = dangSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND"));
        if (request.getMa() != null) {
            ensureMaUnique(dangSanPhamRepository.existsByMaAndIdNot(request.getMa(), id), "Mã dạng sản phẩm");
            d.setMa(request.getMa());
        }
        ensureTenUnique(dangSanPhamRepository.existsByTenIgnoreCaseAndIdNot(trimTen(request.getTen()), id), "Tên dạng sản phẩm");
        d.setTen(trimTen(request.getTen()));
        d.setMoTa(request.getMoTa());
        d.setId(id);
        dangSanPhamRepository.save(d);
    }

    public void deleteDangSanPham(Integer id) {
        DangSanPham d = dangSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND"));
        dangSanPhamRepository.delete(d);
    }

    public DangSanPham getDangSanPhamOrThrow(Integer id) {
        return dangSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND"));
    }

    // --- Công dụng ---
    public List<CongDungResponse> getAllCongDung() {
        return congDungRepository.findAll(ATTR_NEWEST_FIRST).stream().map(CongDungResponse::new).toList();
    }

    public CongDungResponse detailCongDung(Integer id) {
        CongDung c = congDungRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy công dụng", "NOT_FOUND"));
        return new CongDungResponse(c);
    }

    public void addCongDung(CongDungRequest request) {
        ensureTenUnique(congDungRepository.existsByTenIgnoreCase(trimTen(request.getTen())), "Tên công dụng");
        CongDung c = MapperUtil.map(request, CongDung.class);
        c.setTen(trimTen(request.getTen()));
        c.setMa(MaGenerator.nextCode("CD", congDungRepository.findAll().stream().map(CongDung::getMa).toList()));
        congDungRepository.save(c);
    }

    public void updateCongDung(Integer id, CongDungRequest request) {
        CongDung c = congDungRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy công dụng", "NOT_FOUND"));
        if (request.getMa() != null) {
            ensureMaUnique(congDungRepository.existsByMaAndIdNot(request.getMa(), id), "Mã công dụng");
            c.setMa(request.getMa());
        }
        ensureTenUnique(congDungRepository.existsByTenIgnoreCaseAndIdNot(trimTen(request.getTen()), id), "Tên công dụng");
        c.setTen(trimTen(request.getTen()));
        c.setMoTa(request.getMoTa());
        c.setId(id);
        congDungRepository.save(c);
    }

    public void deleteCongDung(Integer id) {
        CongDung c = congDungRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy công dụng", "NOT_FOUND"));
        congDungRepository.delete(c);
    }

    public CongDung getCongDungOrThrow(Integer id) {
        return congDungRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy công dụng", "NOT_FOUND"));
    }

    // --- Thành phần ---
    public List<ThanhPhanResponse> getAllThanhPhan() {
        return thanhPhanRepository.findAll(ATTR_NEWEST_FIRST).stream().map(ThanhPhanResponse::new).toList();
    }

    public ThanhPhanResponse detailThanhPhan(Integer id) {
        ThanhPhan t = thanhPhanRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thành phần", "NOT_FOUND"));
        return new ThanhPhanResponse(t);
    }

    public void addThanhPhan(ThanhPhanRequest request) {
        ensureTenUnique(thanhPhanRepository.existsByTenIgnoreCase(trimTen(request.getTen())), "Tên thành phần");
        ThanhPhan t = MapperUtil.map(request, ThanhPhan.class);
        t.setTen(trimTen(request.getTen()));
        t.setMa(MaGenerator.nextCode("TP", thanhPhanRepository.findAll().stream().map(ThanhPhan::getMa).toList()));
        thanhPhanRepository.save(t);
    }

    public void updateThanhPhan(Integer id, ThanhPhanRequest request) {
        ThanhPhan t = thanhPhanRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thành phần", "NOT_FOUND"));
        if (request.getMa() != null) {
            ensureMaUnique(thanhPhanRepository.existsByMaAndIdNot(request.getMa(), id), "Mã thành phần");
            t.setMa(request.getMa());
        }
        ensureTenUnique(thanhPhanRepository.existsByTenIgnoreCaseAndIdNot(trimTen(request.getTen()), id), "Tên thành phần");
        t.setTen(trimTen(request.getTen()));
        t.setLoai(request.getLoai());
        t.setMoTa(request.getMoTa());
        t.setId(id);
        thanhPhanRepository.save(t);
    }

    public void deleteThanhPhan(Integer id) {
        ThanhPhan t = thanhPhanRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thành phần", "NOT_FOUND"));
        thanhPhanRepository.delete(t);
    }

    public ThanhPhan getThanhPhanOrThrow(Integer id) {
        return thanhPhanRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thành phần", "NOT_FOUND"));
    }

    // --- Màu sắc ---
    public List<MauSacResponse> getAllMauSac() {
        return mauSacRepository.findAll(ATTR_NEWEST_FIRST).stream().map(MauSacResponse::new).toList();
    }

    public MauSacResponse detailMauSac(Integer id) {
        MauSac m = mauSacRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc", "NOT_FOUND"));
        return new MauSacResponse(m);
    }

    public void addMauSac(MauSacRequest request) {
        ensureTenUnique(mauSacRepository.existsByTenIgnoreCase(trimTen(request.getTen())), "Tên màu sắc");
        MauSac m = MapperUtil.map(request, MauSac.class);
        m.setTen(trimTen(request.getTen()));
        m.setMa(MaGenerator.nextCode("MS", mauSacRepository.findAll().stream().map(MauSac::getMa).toList()));
        mauSacRepository.save(m);
    }

    public void updateMauSac(Integer id, MauSacRequest request) {
        MauSac m = mauSacRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc", "NOT_FOUND"));
        if (request.getMa() != null) {
            ensureMaUnique(mauSacRepository.existsByMaAndIdNot(request.getMa(), id), "Mã màu sắc");
            m.setMa(request.getMa());
        }
        ensureTenUnique(mauSacRepository.existsByTenIgnoreCaseAndIdNot(trimTen(request.getTen()), id), "Tên màu sắc");
        m.setTen(trimTen(request.getTen()));
        m.setMaHex(request.getMaHex());
        m.setId(id);
        mauSacRepository.save(m);
    }

    public void deleteMauSac(Integer id) {
        MauSac m = mauSacRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc", "NOT_FOUND"));
        mauSacRepository.delete(m);
    }

    public MauSac getMauSacOrNull(Integer id) {
        if (id == null) return null;
        return mauSacRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc", "NOT_FOUND"));
    }

    public DanhMuc getDanhMucOrThrow(Integer id) {
        return danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
    }

    private void ensureMaUnique(boolean exists, String label) {
        if (exists) {
            throw new ApiException(label + " đã tồn tại", "DUPLICATE");
        }
    }

    private void ensureTenUnique(boolean exists, String label) {
        if (exists) {
            throw new ApiException(label + " đã tồn tại", "DUPLICATE");
        }
    }

    private static String trimTen(String ten) {
        if (ten == null || ten.isBlank()) {
            throw new ApiException("Tên không được để trống", "VALIDATION_ERROR");
        }
        return ten.trim();
    }

    public MaTiepTheoResponse previewMaDanhMuc() {
        return new MaTiepTheoResponse(nextMa("DM", danhMucRepository.findAll().stream().map(DanhMuc::getMa).toList()));
    }

    public MaTiepTheoResponse previewMaThuongHieu() {
        return new MaTiepTheoResponse(nextMa("TH", thuongHieuRepository.findAll().stream().map(ThuongHieu::getMa).toList()));
    }

    public MaTiepTheoResponse previewMaDangSanPham() {
        return new MaTiepTheoResponse(nextMa("DSP", dangSanPhamRepository.findAll().stream().map(DangSanPham::getMa).toList()));
    }

    public MaTiepTheoResponse previewMaCongDung() {
        return new MaTiepTheoResponse(nextMa("CD", congDungRepository.findAll().stream().map(CongDung::getMa).toList()));
    }

    public MaTiepTheoResponse previewMaThanhPhan() {
        return new MaTiepTheoResponse(nextMa("TP", thanhPhanRepository.findAll().stream().map(ThanhPhan::getMa).toList()));
    }

    public MaTiepTheoResponse previewMaMauSac() {
        return new MaTiepTheoResponse(nextMa("MS", mauSacRepository.findAll().stream().map(MauSac::getMa).toList()));
    }

    private String nextMa(String prefix, List<String> existing) {
        return MaGenerator.nextCode(prefix, existing);
    }
}
