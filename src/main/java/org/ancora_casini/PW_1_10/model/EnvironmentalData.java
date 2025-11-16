package org.ancora_casini.PW_1_10.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "environmental_data")
@Getter
@Setter
public final class EnvironmentalData extends BaseData {

    @Column(nullable = false)
    private Double temperature; // Celsius

    @Column(nullable = false)
    private Double humidity; // Percentage

    @Column(nullable = false)
    private Double precipitation; // mm

    @Column(nullable = false)
    private Double soilMoisture; // Percentage

    @Column(nullable = false)
    private Double windSpeed; // km/h
}
