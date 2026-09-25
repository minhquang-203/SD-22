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

    public static boolean isSelf(NhanVien actor, NhanVien target) {
        return actor != null && target != null
                && actor.getId() != null
                && actor.getId().equals(target.getId());
    }

    /**
     * Chỉ thao tác lên tài khoản có cấp thấp hơn.
     * Tự khóa / tự đổi vai trò / đặt lại MK cho người khác cùng cấp vẫn bị chặn.
     */
    public static void assertCanManage(NhanVien actor, NhanVien target) {
        if (isSelf(actor, target)) {
            throw forbiddenSelf();
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

    /** Tự sửa: không được đổi vai trò sang mã khác hiện tại. */
    public static void assertSelfKeepsRole(NhanVien self, String requestedMaVaiTro) {
        String current = maVaiTro(self.getVaiTro());
        if (requestedMaVaiTro != null && current != null && !requestedMaVaiTro.equals(current)) {
            throw forbiddenSelf();
        }
    }

    public static String maVaiTro(VaiTro vaiTro) {
        return vaiTro != null ? vaiTro.getMaVaiTro() : null;
    }

    private static ApiException forbidden() {
        return new ApiException("Không đủ quyền", "FORBIDDEN");
    }

    private static ApiException forbiddenSelf() {
        return new ApiException(
                "Không thể tự thay đổi vai trò/trạng thái của chính mình",
                "FORBIDDEN");
    }
}
