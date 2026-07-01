package org.example.templatejava6.Uv.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "uv_configs")
public class UvConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uv_high_threshold")
    private double uvHighThreshold = 6.0;

    @Column(name = "uv_extreme_threshold")
    private double uvExtremeThreshold = 8.0;

    @Column(name = "bonus_points_high")
    private int bonusPointsHigh = 20;

    @Column(name = "bonus_points_extreme")
    private int bonusPointsExtreme = 50;

    @Column(name = "enable_realtime_alert")
    private boolean enableRealtimeAlert = true;

    // --- Bổ sung Getters và Setters ở dưới đây ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public double getUvHighThreshold() { return uvHighThreshold; }
    public void setUvHighThreshold(double uvHighThreshold) { this.uvHighThreshold = uvHighThreshold; }

    public double getUvExtremeThreshold() { return uvExtremeThreshold; }
    public void setUvExtremeThreshold(double uvExtremeThreshold) { this.uvExtremeThreshold = uvExtremeThreshold; }

    public int getBonusPointsHigh() { return bonusPointsHigh; }
    public void setBonusPointsHigh(int bonusPointsHigh) { this.bonusPointsHigh = bonusPointsHigh; }

    public int getBonusPointsExtreme() { return bonusPointsExtreme; }
    public void setBonusPointsExtreme(int bonusPointsExtreme) { this.bonusPointsExtreme = bonusPointsExtreme; }

    public boolean isEnableRealtimeAlert() { return enableRealtimeAlert; }
    public void setEnableRealtimeAlert(boolean enableRealtimeAlert) { this.enableRealtimeAlert = enableRealtimeAlert; }
}
