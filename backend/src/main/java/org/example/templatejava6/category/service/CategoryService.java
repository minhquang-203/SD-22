package org.example.templatejava6.category.service;

import org.example.templatejava6.category.entity.*;
import org.example.templatejava6.category.model.request.*;
import org.example.templatejava6.category.model.response.*;
import org.example.templatejava6.category.repository.*;
import org.example.templatejava6.common.exception.ApiException;
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
        return danhMucRepository.findByTrangThaiTrue().stream().map(DanhMucResponse::new).toList();
    }

    public DanhMucResponse detailDanhMuc(Integer id) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
        return new DanhMucResponse(dm);
    }

    public void addDanhMuc(DanhMucRequest request) {
        if (danhMucRepository.existsByMa(request.getMa())) {
            throw new ApiException("Mã danh mục đã tồn tại", "DUPLICATE");
        }
        DanhMuc dm = MapperUtil.map(request, DanhMuc.class);
        dm.setTrangThai(true);
        danhMucRepository.save(dm);
    }

    public void updateDanhMuc(Integer id, DanhMucRequest request) {
        DanhMuc dm = danhMucRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy danh mục", "NOT_FOUND"));
        if (danhMucRepository.existsByMaAndIdNot(request.getMa(), id)) {
            throw new ApiException("Mã danh mục đã tồn tại", "DUPLICATE");
        }
        MapperUtil.mapToExisting(request, dm);
        dm.setId(id);
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
        return thuongHieuRepository.findByTrangThaiTrue().stream().map(ThuongHieuResponse::new).toList();
    }

    public ThuongHieuResponse detailThuongHieu(Integer id) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        return new ThuongHieuResponse(th);
    }

    public void addThuongHieu(ThuongHieuRequest request) {
        ThuongHieu th = MapperUtil.map(request, ThuongHieu.class);
        th.setTrangThai(true);
        thuongHieuRepository.save(th);
    }

    public void updateThuongHieu(Integer id, ThuongHieuRequest request) {
        ThuongHieu th = thuongHieuRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thương hiệu", "NOT_FOUND"));
        MapperUtil.mapToExisting(request, th);
        th.setId(id);
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
        dangSanPhamRepository.save(MapperUtil.map(request, DangSanPham.class));
    }

    public void updateDangSanPham(Integer id, DangSanPhamRequest request) {
        DangSanPham d = dangSanPhamRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND"));
        MapperUtil.mapToExisting(request, d);
        d.setId(id);
        dangSanPhamRepository.save(d);
    }

    public void deleteDangSanPham(Integer id) {
        if (!dangSanPhamRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy dạng sản phẩm", "NOT_FOUND");
        }
        dangSanPhamRepository.deleteById(id);
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
        congDungRepository.save(MapperUtil.map(request, CongDung.class));
    }

    public void updateCongDung(Integer id, CongDungRequest request) {
        CongDung c = congDungRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy công dụng", "NOT_FOUND"));
        MapperUtil.mapToExisting(request, c);
        c.setId(id);
        congDungRepository.save(c);
    }

    public void deleteCongDung(Integer id) {
        if (!congDungRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy công dụng", "NOT_FOUND");
        }
        congDungRepository.deleteById(id);
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
        thanhPhanRepository.save(MapperUtil.map(request, ThanhPhan.class));
    }

    public void updateThanhPhan(Integer id, ThanhPhanRequest request) {
        ThanhPhan t = thanhPhanRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy thành phần", "NOT_FOUND"));
        MapperUtil.mapToExisting(request, t);
        t.setId(id);
        thanhPhanRepository.save(t);
    }

    public void deleteThanhPhan(Integer id) {
        if (!thanhPhanRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy thành phần", "NOT_FOUND");
        }
        thanhPhanRepository.deleteById(id);
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
        mauSacRepository.save(MapperUtil.map(request, MauSac.class));
    }

    public void updateMauSac(Integer id, MauSacRequest request) {
        MauSac m = mauSacRepository.findById(id)
                .orElseThrow(() -> new ApiException("Không tìm thấy màu sắc", "NOT_FOUND"));
        MapperUtil.mapToExisting(request, m);
        m.setId(id);
        mauSacRepository.save(m);
    }

    public void deleteMauSac(Integer id) {
        if (!mauSacRepository.existsById(id)) {
            throw new ApiException("Không tìm thấy màu sắc", "NOT_FOUND");
        }
        mauSacRepository.deleteById(id);
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
}
