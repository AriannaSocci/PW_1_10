package org.ancora_casini.PW_1_10.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public enum Region {

    LOMBARDIA(18.0),
    EMILIA_ROMAGNA(20.0),
    VENETO(19.0),
    PIEMONTE(17.0),
    SICILIA(24.0),
    LAZIO(21.0),
    CAMPANIA(22.0),
    TOSCANA(22.0);

    private final double averageTemperature;


    public static List<Region> getAllRegions() {
        return List.of(Region.values());
    }
}
