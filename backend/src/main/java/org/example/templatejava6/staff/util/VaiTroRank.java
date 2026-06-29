package org.example.templatejava6.staff.util;

import org.example.templatejava6.common.entity.NhanVien;
import org.example.templatejava6.common.exception.ApiException;
import org.example.templatejava6.common.model.VaiTro;

public final class VaiTroRank {

    private VaiTroRank() {
    }

    public static int rankOf(String maVaiTro) {
        if (maVaiTro == null) {
            return 0;
        }
        return switch (maVaiTro) {
            case "CHU" -> 3;
            case "QUAN_LY" -> 2;
            case "NHAN_VIEN" -> 1;
            default -> 0;
        };
    }

    public static int rankOf(NhanVien nv) {
        if (nv == null || nv.getVaiTro() == null) {
            return 0;
        }
        return rankOf(nv.getVaiTro().getMaVaiTro());
    }

    /** Chỉ thao tác lên tài khoản có cấp thấp hơn; không tự thao tác chính mình. */
    public static void assertCanManage(NhanVien actor, NhanVien target) {
        if (actor.getId().equals(target.getId())) {
            throw forbidden();
        }
        if (rankOf(target) >= rankOf(actor)) {
            throw forbidden();
        }
    }

    /** Khi thêm/đổi vai trò: chỉ gán cấp thấp hơn người thực hiện; không ai tạo CHU. */
    public static void assertCanAssignRole(NhanVien actor, String maVaiTro) {
        if ("CHU".equals(maVaiTro)) {
            throw forbidden();
        }
        int assignRank = rankOf(maVaiTro);
        if (assignRank <= 0 || assignRank >= rankOf(actor)) {
            throw forbidden();
        }
    }

    public static String maVaiTro(VaiTro vaiTro) {
        return vaiTro != null ? vaiTro.getMaVaiTro() : null;
    }

    private static ApiException forbidden() {
        return new ApiException("Không đủ quyền", "FORBIDDEN");
    }
}
