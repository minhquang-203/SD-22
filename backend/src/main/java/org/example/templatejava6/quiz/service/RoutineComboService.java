package org.example.templatejava6.quiz.service;

import org.example.templatejava6.common.entity.LoaiDa;
import org.example.templatejava6.common.repository.LoaiDaRepository;
import org.example.templatejava6.product.entity.SanPham;
import org.example.templatejava6.product.repository.SanPhamRepository;
import org.example.templatejava6.quiz.entity.RoutineCombo;
import org.example.templatejava6.quiz.entity.RoutineComboChiTiet;
import org.example.templatejava6.quiz.model.request.RoutineComboRequest;
import org.example.templatejava6.quiz.model.response.RoutineComboResponse;
import org.example.templatejava6.quiz.repository.RoutineComboRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoutineComboService {

    @Autowired
    private RoutineComboRepository routineComboRepository;

    @Autowired
    private LoaiDaRepository loaiDaRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    public List<RoutineComboResponse> getAllRoutines() {
        return routineComboRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<RoutineComboResponse> getRoutinesByLoaiDa(Integer loaiDaId) {
        return routineComboRepository.findByLoaiDa_IdAndTrangThaiTrue(loaiDaId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoutineComboResponse createRoutine(RoutineComboRequest request) {
        RoutineCombo routine = new RoutineCombo();
        routine.setTen(request.getTen());
        routine.setMoTa(request.getMoTa());
        routine.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        routine.setThuTu(0);

        if (request.getIdLoaiDa() != null) {
            LoaiDa loaiDa = loaiDaRepository.findById(request.getIdLoaiDa())
                    .orElseThrow(() -> new RuntimeException("Loai da not found"));
            routine.setLoaiDa(loaiDa);
        }

        List<RoutineComboChiTiet> chiTiets = new ArrayList<>();
        if (request.getChiTiets() != null) {
            for (RoutineComboRequest.ChiTietRequest ctReq : request.getChiTiets()) {
                RoutineComboChiTiet ct = new RoutineComboChiTiet();
                ct.setRoutine(routine);
                ct.setThuTu(ctReq.getThuTu() != null ? ctReq.getThuTu() : 0);
                ct.setGhiChu(ctReq.getGhiChu());
                
                SanPham sp = sanPhamRepository.findById(ctReq.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("San pham not found"));
                ct.setSanPham(sp);
                chiTiets.add(ct);
            }
        }
        routine.setChiTiets(chiTiets);
        
        RoutineCombo saved = routineComboRepository.save(routine);
        return mapToResponse(saved);
    }

    @Transactional
    public RoutineComboResponse updateRoutine(Integer id, RoutineComboRequest request) {
        RoutineCombo routine = routineComboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routine not found"));
                
        routine.setTen(request.getTen());
        routine.setMoTa(request.getMoTa());
        if (request.getTrangThai() != null) {
            routine.setTrangThai(request.getTrangThai());
        }

        if (request.getIdLoaiDa() != null) {
            LoaiDa loaiDa = loaiDaRepository.findById(request.getIdLoaiDa())
                    .orElseThrow(() -> new RuntimeException("Loai da not found"));
            routine.setLoaiDa(loaiDa);
        } else {
            routine.setLoaiDa(null);
        }

        routine.getChiTiets().clear();
        if (request.getChiTiets() != null) {
            for (RoutineComboRequest.ChiTietRequest ctReq : request.getChiTiets()) {
                RoutineComboChiTiet ct = new RoutineComboChiTiet();
                ct.setRoutine(routine);
                ct.setThuTu(ctReq.getThuTu() != null ? ctReq.getThuTu() : 0);
                ct.setGhiChu(ctReq.getGhiChu());
                
                SanPham sp = sanPhamRepository.findById(ctReq.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("San pham not found"));
                ct.setSanPham(sp);
                routine.getChiTiets().add(ct);
            }
        }
        
        RoutineCombo updated = routineComboRepository.save(routine);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteRoutine(Integer id) {
        routineComboRepository.deleteById(id);
    }

    private RoutineComboResponse mapToResponse(RoutineCombo routine) {
        RoutineComboResponse response = new RoutineComboResponse();
        response.setId(routine.getId());
        response.setTen(routine.getTen());
        response.setMoTa(routine.getMoTa());
        response.setTrangThai(routine.getTrangThai());
        response.setThuTu(routine.getThuTu());
        
        if (routine.getLoaiDa() != null) {
            response.setIdLoaiDa(routine.getLoaiDa().getId());
            response.setTenLoaiDa(routine.getLoaiDa().getTen());
        }

        if (routine.getChiTiets() != null) {
            List<RoutineComboResponse.ChiTietResponse> ctResponses = routine.getChiTiets().stream().map(ct -> {
                RoutineComboResponse.ChiTietResponse ctResp = new RoutineComboResponse.ChiTietResponse();
                ctResp.setId(ct.getId());
                if (ct.getSanPham() != null) {
                    ctResp.setIdSanPham(ct.getSanPham().getId());
                    ctResp.setTenSanPham(ct.getSanPham().getTen());
                    ctResp.setAnhChinhUrl(null); // Frontend will handle
                }
                ctResp.setGhiChu(ct.getGhiChu());
                ctResp.setThuTu(ct.getThuTu());
                return ctResp;
            }).collect(Collectors.toList());
            response.setChiTiets(ctResponses);
        }
        
        return response;
    }
}
