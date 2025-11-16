package org.ancora_casini.PW_1_10.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public enum Crop {

    WHEAT("Wheat",180, 6000.0, 0.25, new SeasonInfo(9, 7)),
    CORN("Corn",120, 10000.0, 0.22, new SeasonInfo(4, 9)),
    RICE("Rice", 150, 7000.0, 0.45, new SeasonInfo(4, 10)),
    SOY("Soy",100, 3000.0, 0.38, new SeasonInfo(5, 9)),
    SUNFLOWER("Sunflower",90, 2500.0, 0.42, new SeasonInfo(4, 8));

    private final String name;
    private final int growthPeriod;
    private final double averageYield;
    private final double marketPrice;
    private final SeasonInfo seasonInfo;

    public static List<Crop> getAllCrops() {
        return List.of(Crop.values());
    }

    public record SeasonInfo(int plantingMonth, int harvestMonth) {

        public boolean isGrowingSeason(int month) {
            if (plantingMonth <= harvestMonth) {
                return month >= plantingMonth && month <= harvestMonth;
            } else {
                return month >= plantingMonth || month <= harvestMonth;
            }
        }
    }
}
