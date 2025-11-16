package org.ancora_casini.PW_1_10.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "production_data")
@Getter
@Setter
public final class ProductionData extends BaseData {

    @Column(nullable = false)
    private Crop crop;

    @Column(nullable = false)
    private Double harvestQuantity; // kg

    @Column(nullable = false)
    private Integer growthDays;

    @Column(nullable = false)
    private Double yieldPerHectare; // kg/ha

    @Column(nullable = false)
    private BigDecimal productionCost; // Euro

    @Column(nullable = false)
    private BigDecimal marketPrice; // Euro per kg

    @Column(nullable = false)
    private BigDecimal revenue; // Euro

    @Column(nullable = false)
    private Double efficiency; // Percentage (0-100)
}
