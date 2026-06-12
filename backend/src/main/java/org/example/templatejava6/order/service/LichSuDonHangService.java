package org.example.templatejava6.order.service;


import org.example.templatejava6.common.entity.NhanVien;

import org.example.templatejava6.common.exception.ApiException;

import org.example.templatejava6.order.entity.LichSuDonHang;

import org.example.templatejava6.order.model.request.LichSuDonHangCapNhatRequest;

import org.example.templatejava6.order.model.request.LichSuDonHangRequest;

import org.example.templatejava6.order.model.response.LichSuDonHangResponse;

import org.example.templatejava6.order.repository.LichSuDonHangRepository;

import org.example.templatejava6.order.repository.NhanVienRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.util.List;


@Service

public class LichSuDonHangService {


    @Autowired
    private LichSuDonHangRepository lichSuDonHangRepository;

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private NhanVienRepository nhanVienRepository;


    @Transactional(readOnly = true)

    public List<LichSuDonHangResponse> getAll() {

        return lichSuDonHangRepository.findAllByOrderByThoiGianDesc()

                .stream().map(LichSuDonHangResponse::new).toList();

    }


    @Transactional(readOnly = true)

    public List<LichSuDonHangResponse> getByHoaDon(Integer idHoaDon) {

        hoaDonService.getHoaDonOrThrow(idHoaDon);

        return lichSuDonHangRepository.findByIdHoaDon_IdOrderByThoiGianDesc(idHoaDon)

                .stream().map(LichSuDonHangResponse::new).toList();

    }


    @Transactional(readOnly = true)

    public LichSuDonHangResponse detail(Integer id) {

        return new LichSuDonHangResponse(getLichSuDonHangOrThrow(id));

    }


    @Transactional

    public void add(LichSuDonHangRequest request) {

        hoaDonService.chuyenTrangThai(

                request.getIdHoaDon(),

                request.getTrangThai(),

                request.getGhiChu(),

                request.getIdNhanVien());

    }


    @Transactional

    public void update(Integer id, LichSuDonHangCapNhatRequest request) {

        LichSuDonHang ls = getLichSuDonHangOrThrow(id);


        if (request.getGhiChu() != null) {

            ls.setGhiChu(request.getGhiChu());

        }

        if (request.getIdNhanVien() != null) {

            ls.setIdNhanVien(resolveNhanVien(request.getIdNhanVien()));

        }

        ls.setId(id);

        lichSuDonHangRepository.save(ls);

    }


    @Transactional

    public void delete(Integer id) {

        LichSuDonHang ls = getLichSuDonHangOrThrow(id);

        lichSuDonHangRepository.delete(ls);

    }


    public LichSuDonHang getLichSuDonHangOrThrow(Integer id) {

        return lichSuDonHangRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy lịch sử đơn hàng", "NOT_FOUND"));

    }


    private NhanVien resolveNhanVien(Integer id) {

        return nhanVienRepository.findById(id)

                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "NOT_FOUND"));

    }

}

