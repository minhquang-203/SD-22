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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    @Autowired private DanhMucRepository danhMucRepository;
    @Autowired private ThuongHieuRepository thuongHieuRepository;
    @Autowired private DangSanPhamRepository dangSanPhamRepository;
    @Autowired private CongDungRepository congDungRepository;
    @Autowired private ThanhPhanRepository thanhPhanRepository;
    @Autowired private MauSacRepository mauSacRepository;

    // --- Danh mục ---
    public List<DanhMucResponse> getAllDanhMuc() {
        return danhMucRepository.findAll().stream().map(DanhMucResponse::new).toList();
    }

    public DanhMucResponse detailDanhMuc(Integer id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
        return new DanhMucResponse(dm);
    }

    public void addDanhMuc(DanhMucRequest request) {
        DanhMuc dm = MapperUtil.map(request, DanhMuc.class);
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
        Boolean trangThai = dm.getTrangThai();
        MapperUtil.mapToExisting(request, dm);
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
        return thuongHieuRepository.findAll().stream().map(ThuongHieuResponse::new).toList();
    }

    public ThuongHieuResponse detailThuongHieu(Integer id) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        return new ThuongHieuResponse(th);
    }

    public void addThuongHieu(ThuongHieuRequest request) {
        ThuongHieu th = MapperUtil.map(request, ThuongHieu.class);
        th.setMa(MaGenerator.nextCode("TH", thuongHieuRepository.findAll().stream().map(ThuongHieu::getMa).toList()));
        th.setTrangThai(true);
        thuongHieuRepository.save(th);
    }

    public void updateThuongHieu(Integer id, ThuongHieuRequest request) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        ensureMaUnique(thuongHieuRepository.existsByMaAndIdNot(request.getMa(), id), "Mã thương hiệu");
        Boolean trangThai = th.getTrangThai();
        MapperUtil.mapToExisting(request, th);
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
        return dangSanPhamRepository.findAll().stream().map(DangSanPhamResponse::new).toList();
    }

    public DangSanPhamResponse detailDangSanPham(Integer id) {
        DangSanPham d = dangSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND"));
        return new DangSanPhamResponse(d);
    }

    public void addDangSanPham(DangSanPhamRequest request) {
        DangSanPham d = MapperUtil.map(request, DangSanPham.class);
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
        d.setTen(request.getTen());
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
        return congDungRepository.findAll().stream().map(CongDungResponse::new).toList();
    }

    public CongDungResponse detailCongDung(Integer id) {
        CongDung c = congDungRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy công dụng", "NOT_FOUND"));
        return new CongDungResponse(c);
    }

    public void addCongDung(CongDungRequest request) {
        CongDung c = MapperUtil.map(request, CongDung.class);
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
        c.setTen(request.getTen());
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
        return thanhPhanRepository.findAll().stream().map(ThanhPhanResponse::new).toList();
    }

    public ThanhPhanResponse detailThanhPhan(Integer id) {
        ThanhPhan t = thanhPhanRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thành phần", "NOT_FOUND"));
        return new ThanhPhanResponse(t);
    }

    public void addThanhPhan(ThanhPhanRequest request) {
        ThanhPhan t = MapperUtil.map(request, ThanhPhan.class);
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
        t.setTen(request.getTen());
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
        return mauSacRepository.findAll().stream().map(MauSacResponse::new).toList();
    }

    public MauSacResponse detailMauSac(Integer id) {
        MauSac m = mauSacRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc", "NOT_FOUND"));
        return new MauSacResponse(m);
    }

    public void addMauSac(MauSacRequest request) {
        MauSac m = MapperUtil.map(request, MauSac.class);
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
        m.setTen(request.getTen());
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
