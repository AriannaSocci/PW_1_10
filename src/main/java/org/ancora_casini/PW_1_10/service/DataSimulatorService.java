package org.ancora_casini.PW_1_10.service;

import lombok.AllArgsConstructor;
import org.ancora_casini.PW_1_10.model.*;
import org.ancora_casini.PW_1_10.repository.EnvironmentalDataRepository;
import org.ancora_casini.PW_1_10.repository.ProductionDataRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@AllArgsConstructor
public class DataSimulatorService {

    private final EnvironmentalDataRepository environmentalDataRepository;

    private final ProductionDataRepository productionDataRepository;

    private final Random random = new Random();

    private final List<Region> regions = Region.getAllRegions();
    private final List<Crop> crops = Crop.getAllCrops();

    // Previous data state for continuity
    private final Map<String, EnvironmentalState> lastEnvironmentalState = new HashMap<>();
    private final Map<String, ProductionState> lastProductionState = new HashMap<>();

    // Classes to store previous state
    private static class EnvironmentalState {
        double temperature, humidity, precipitation, soilMoisture, windSpeed;

        EnvironmentalState(double temp, double hum, double precipitation, double soil, double wind) {
            this.temperature = temp;
            this.humidity = hum;
            this.precipitation = precipitation;
            this.soilMoisture = soil;
            this.windSpeed = wind;
        }
    }

    private static class ProductionState {
        double yieldPerHectare, efficiency;
        BigDecimal marketPrice;

        ProductionState(double yield, double eff, BigDecimal price) {
            this.yieldPerHectare = yield;
            this.efficiency = eff;
            this.marketPrice = price;
        }
    }

    /**
     * Generates environmental data with seasonal variations and regional influences
     *
     * @param start    start time for data generation
     * @param end      end time for data generation
     * @param interval  customizable time interval between data points
     */
    public void generateEnvironmentalData(OffsetDateTime start, OffsetDateTime end, Interval interval) {

        clearEnvironmentData();

        List<EnvironmentalData> allData = new java.util.ArrayList<>();

        // For each region and crop type
        for (Region region : regions) {
            OffsetDateTime currentTime = start;
            String key = region.name();

            // Initialize state if not exists
            if (!lastEnvironmentalState.containsKey(key)) {
                initializeEnvironmentalState(key, region);
            }

            EnvironmentalState state = lastEnvironmentalState.get(key);

            // Generate continuous time series
            while (currentTime.isBefore(end)) {
                EnvironmentalData data = new EnvironmentalData();

                // Timestamp with customizable interval
                currentTime = currentTime.plus(interval.step(), interval.timeUnit().getChronoUnit());
                data.setTimestamp(currentTime);

                // Calculate base seasonal temperature for this specific time
                double seasonalTemp = getSeasonalTemperature(currentTime, region);

                // Blend toward seasonal target more aggressively
                state.temperature = state.temperature * 0.85 + seasonalTemp * 0.15;

                // Humidity: random walk with soft boundaries
                state.humidity += random.nextGaussian();
                if (state.humidity < 30.0) state.humidity += 3.0;
                if (state.humidity > 80.0) state.humidity -= 3.0;
                state.humidity = Math.max(20.0, Math.min(95.0, state.humidity));

                // Precipitation: seasonal chance with gradual decay
                double seasonalPrecipitationChance = getSeasonalPrecipitationChance(currentTime);
                if (random.nextDouble() < seasonalPrecipitationChance * 0.08) {
                    state.precipitation += random.nextDouble() * 5.0;
                }
                state.precipitation *= 0.92; // Gradual decay
                state.precipitation = Math.max(0.0, Math.min(50.0, state.precipitation));

                // Wind speed: seasonal base with random variation
                double seasonalWind = getSeasonalWindSpeed(currentTime);
                state.windSpeed = state.windSpeed * 0.7 + seasonalWind * 0.3 + random.nextGaussian() * 1.5;
                state.windSpeed = Math.max(0.0, Math.min(40.0, state.windSpeed));

                // Apply variations at the end (daily cycle, regional, weather events)
                applySeasonalVariations(state, currentTime, region);

                // Soil moisture: correlated with precipitation and humidity with more variation
                // Allow soil to gain moisture from rain and lose it from evaporation
                double moistureGainFromRain = state.precipitation * 1.5;
                double moistureLossFromEvaporation = (100.0 - state.humidity) * 0.1;
                double targetSoilMoisture = state.humidity * 0.5 + moistureGainFromRain - moistureLossFromEvaporation;

                // Gradual transition with some random variation
                state.soilMoisture = state.soilMoisture * 0.80 + targetSoilMoisture * 0.15 + random.nextGaussian() * 3.0;

                // Allow lower minimum (10%) for dry periods, especially in summer
                state.soilMoisture = Math.max(10.0, Math.min(100.0, state.soilMoisture));

                // Set all data values
                data.setTemperature(round(state.temperature));
                data.setHumidity(round(state.humidity));
                data.setPrecipitation(round(state.precipitation));
                data.setSoilMoisture(round(state.soilMoisture));
                data.setWindSpeed(round(state.windSpeed));

                data.setRegion(region);

                allData.add(data);
            }

            // Update stored state
            lastEnvironmentalState.put(key, state);

        }

        // Bulk save all data at once
        environmentalDataRepository.saveAll(allData);
    }

    /**
     * Generates production data with seasonal variations and crop-specific influences
     *
     * @param start    start time for data generation
     * @param end      end time for data generation
     * @param interval customizable time interval between data points
     */
    public void generateProductionData(OffsetDateTime start, OffsetDateTime end, Interval interval) {

        clearProductionData();

        List<ProductionData> allData = new java.util.ArrayList<>();

        // For each region and crop type
        for (Region region : regions) {
            for (Crop crop : crops) {
                OffsetDateTime currentTime = start;
                String key = region + "_" + crop.getName();

                // Initialize state if not exists
                if (!lastProductionState.containsKey(key)) {
                    initializeProductionState(key, crop);
                }

                ProductionState state = lastProductionState.get(key);

                // Generate continuous time series
                while (currentTime.isBefore(end)) {
                    currentTime = currentTime.plus(interval.step(), interval.timeUnit().getChronoUnit());

                    // Check if it's growing season for this crop
                    if (!crop.getSeasonInfo().isGrowingSeason(currentTime.getMonthValue())) {
                        continue; // Skip if not growing season
                    }

                    ProductionData data = new ProductionData();
                    data.setTimestamp(currentTime);
                    data.setRegion(region);
                    data.setCrop(crop);

                    // Growth days: varies based on season
                    int baseGrowthDays = crop.getGrowthPeriod();
                    int seasonalAdjustment = getSeasonalGrowthAdjustment(currentTime);
                    data.setGrowthDays(baseGrowthDays + seasonalAdjustment + random.nextInt(10) - 5);

                    // Yield per hectare: influenced by season with smooth transitions
                    double seasonalYieldMultiplier = getSeasonalYieldMultiplier(currentTime, crop);
                    double targetYield = crop.getAverageYield() * seasonalYieldMultiplier;
                    state.yieldPerHectare = state.yieldPerHectare * 0.95 + targetYield * 0.05 + random.nextGaussian() * 10.0;
                    state.yieldPerHectare = Math.max(crop.getAverageYield() * 0.5, Math.min(crop.getAverageYield() * 1.5, state.yieldPerHectare));
                    data.setYieldPerHectare(round(state.yieldPerHectare));

                    // Harvested quantity
                    double hectares = normalDistribution(50.0, 20.0, 10.0, 200.0);
                    data.setHarvestQuantity(round(data.getYieldPerHectare() * hectares));

                    // Market price with seasonal variations
                    double seasonalPriceMultiplier = getSeasonalPriceMultiplier(currentTime, crop);
                    double targetPrice = crop.getMarketPrice() * seasonalPriceMultiplier;
                    double currentPrice = state.marketPrice.doubleValue();
                    double priceChange = (targetPrice - currentPrice) * 0.05 + random.nextGaussian() * 0.01;
                    double newPrice = currentPrice + priceChange;
                    newPrice = Math.max(crop.getMarketPrice() * 0.6, Math.min(crop.getMarketPrice() * 1.4, newPrice));
                    state.marketPrice = BigDecimal.valueOf(newPrice).setScale(2, RoundingMode.HALF_UP);
                    data.setMarketPrice(state.marketPrice);

                    // Economic calculations
                    BigDecimal revenue = state.marketPrice.multiply(BigDecimal.valueOf(data.getHarvestQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);
                    data.setRevenue(revenue);

                    double costRatio = normalDistribution(0.7, 0.05, 0.6, 0.8);
                    data.setProductionCost(revenue.multiply(BigDecimal.valueOf(costRatio))
                            .setScale(2, RoundingMode.HALF_UP));

                    // Efficiency based on seasonal yield with smooth transitions
                    double targetEfficiency = Math.min(100.0, (data.getYieldPerHectare() / (targetYield * 1.1)) * 100);
                    state.efficiency = state.efficiency * 0.9 + targetEfficiency * 0.1;
                    state.efficiency = Math.max(50.0, Math.min(100.0, state.efficiency));
                    data.setEfficiency(round(state.efficiency));

                    allData.add(data);
                }

                // Update stored state
                lastProductionState.put(key, state);
            }
        }

        // Bulk save all data at once
        productionDataRepository.saveAll(allData);
    }


    private void clearEnvironmentData() {
        lastEnvironmentalState.clear();
        environmentalDataRepository.deleteAll();
    }

    private void clearProductionData() {
        lastProductionState.clear();
        productionDataRepository.deleteAll();
    }

    // Methods for seasonal variations - with realistic bounds
    private void applySeasonalVariations(EnvironmentalState state, OffsetDateTime timestamp, Region region) {
        int month = timestamp.getMonthValue();
        int hour = timestamp.getHour();

        // Daily temperature cycle: ±3°C variation (more realistic)
        double sinned = Math.sin(2 * Math.PI * (hour - 6) / 24.0);
        double dailyTempCycle = 3.0 * sinned;
        state.temperature += dailyTempCycle;

        // Daily humidity cycle: inverse of temperature (±5% instead of ±8%)
        double dailyHumidityCycle = -5.0 * sinned;
        state.humidity += dailyHumidityCycle;

        // Regional climate adjustments
        switch (region) {
            case LOMBARDIA, PIEMONTE -> {
                if (month >= 11 || month <= 2) {
                    state.temperature -= 1.0; // Colder in winter
                    state.soilMoisture += 5.0; // More moisture in winter
                }
                if (month >= 10 && month <= 11) {
                    state.humidity += 5.0; // Autumn fog (reduced from 8.0)
                    state.soilMoisture += 8.0; // Wet autumn
                }
            }
            case TOSCANA, EMILIA_ROMAGNA, VENETO -> {
                if (month >= 6 && month <= 8) {
                    state.humidity -= 5.0; // Drier summer
                    state.soilMoisture -= 10.0; // Dry soil in summer
                }
            }
            case SICILIA, CAMPANIA, LAZIO -> {
                if (month >= 6 && month <= 8) {
                    state.temperature += 1.5; // Warmer in summer
                    state.humidity -= 10.0; // Much drier summer (increased from -8.0)
                    state.soilMoisture -= 15.0; // Very dry soil in southern summer
                }
            }
        }

        // Rare weather events (low probability, moderate impact)
        if (random.nextDouble() < 0.03) {
            if (month >= 6 && month <= 8 && hour >= 14 && hour <= 18) {
                // Summer afternoon thunderstorm
                state.precipitation += random.nextDouble() * 8.0;
                state.windSpeed += random.nextDouble() * 8.0;
                state.temperature -= random.nextDouble() * 3.0;
            } else if (month >= 11 || month <= 2) {
                // Winter precipitation
                state.precipitation += random.nextDouble() * 5.0;
                state.windSpeed += random.nextDouble() * 6.0;
            }
        }

        // Soft boundaries - only clamp to extreme values
        if (state.temperature < -10.0) state.temperature = -10.0 + random.nextDouble() * 2.0;
        if (state.temperature > 50.0) state.temperature = 50.0 - random.nextDouble() * 2.0;
        if (state.humidity < 15.0) state.humidity = 15.0 + random.nextDouble() * 5.0;
        if (state.humidity > 100.0) state.humidity = 100.0;
    }



    // Utility methods for state initialization
    private void initializeEnvironmentalState(String key, Region region) {

        lastEnvironmentalState.put(key, new EnvironmentalState(
                region.getAverageTemperature() + random.nextGaussian() * 3, // base temperature ± 3°C
                50.0 + random.nextGaussian() * 10, // base humidity 50% ± 10%
                0.0, // start with no precipitation
                45.0 + random.nextGaussian() * 20, // base soil moisture 45% ± 20% (wider range: 25-65%)
                10.0 + random.nextGaussian() * 5.0 // base wind speed 10km/h ± 5
        ));
    }

    private void initializeProductionState(String key, Crop crop) {
        lastProductionState.put(key, new ProductionState(
                crop.getAverageYield() + random.nextGaussian() * crop.getAverageYield() * 0.1,
                75.0 + random.nextGaussian() * 10, // base efficiency 75% ± 10%
                BigDecimal.valueOf(crop.getMarketPrice() + random.nextGaussian() * crop.getMarketPrice() * 0.05)
                        .setScale(2, RoundingMode.HALF_UP)
        ));
    }



    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Existing methods for compatibility
    private double normalDistribution(double mean, double stdDev, double min, double max) {
        double value;
        do {
            value = random.nextGaussian() * stdDev + mean;
        } while (value < min || value > max);
        return round(value);
    }

    /**
     * Calculates the average seasonal temperature for a region based on the time of year
     */
    private double getSeasonalTemperature(OffsetDateTime timestamp, Region region) {
        int dayOfYear = timestamp.getDayOfYear();
        double baseTemp = region.getAverageTemperature();

        // Annual sinusoidal oscillation (minimum in January, maximum in July)
        // dayOfYear 1 = January 1st, ~200 = mid-July
        // Use 10°C amplitude for realistic seasonal swing (20°C total range)
        double seasonalVariation = 10.0 * Math.sin(2 * Math.PI * (dayOfYear - 15) / 365.0);

        return baseTemp + seasonalVariation;
    }

    /**
     * Calculates the probability of precipitation based on the season
     */
    private double getSeasonalPrecipitationChance(OffsetDateTime timestamp) {
        int month = timestamp.getMonthValue();

        // Spring and autumn: more rain
        if (month >= 3 && month <= 5 || month >= 9 && month <= 11) {
            return 0.25; // 25% probability
        }
        // Summer: dry
        else if (month >= 6 && month <= 8) {
            return 0.10; // 10% probability
        }
        // Winter: moderate precipitation
        else {
            return 0.18; // 18% probability
        }
    }

    /**
     * Calculates the seasonal wind speed
     */
    private double getSeasonalWindSpeed(OffsetDateTime timestamp) {
        int month = timestamp.getMonthValue();

        // Spring: stronger winds
        if (month >= 3 && month <= 5) {
            return 12.0 + random.nextDouble() * 4.0;
        }
        // Summer: light winds
        else if (month >= 6 && month <= 8) {
            return 6.0 + random.nextDouble() * 3.0;
        }
        // Autumn: moderate winds
        else if (month >= 9 && month <= 11) {
            return 10.0 + random.nextDouble() * 4.0;
        }
        // Winter: strong winds
        else {
            return 13.0 + random.nextDouble() * 5.0;
        }
    }

    /**
     * Calculates the adjustment to growth days based on the season
     */
    private int getSeasonalGrowthAdjustment(OffsetDateTime timestamp) {
        int month = timestamp.getMonthValue();

        // Optimal conditions in late spring/early summer
        if (month >= 5 && month <= 7) {
            return -5; // Faster growth (fewer days needed)
        }
        // Good conditions in spring
        else if (month == 4 || month >= 8 && month <= 9) {
            return 0; // Normal growth
        }
        // Suboptimal conditions (start/end of season)
        else {
            return 5; // Slower growth (more days needed)
        }
    }

    /**
     * Calculates the yield multiplier based on the season and crop
     */
    private double getSeasonalYieldMultiplier(OffsetDateTime timestamp, Crop crop) {
        int month = timestamp.getMonthValue();
        var seasonInfo = crop.getSeasonInfo();

        // If not growing season, very low yield
        if (!seasonInfo.isGrowingSeason(month)) {
            return 0.1;
        }

        // Optimal months for yield (peak of season)
        int optimalMonth = (seasonInfo.plantingMonth() + seasonInfo.harvestMonth()) / 2;
        if (optimalMonth > 12) optimalMonth -= 12;

        // Calculate distance from optimal month
        int distance = Math.abs(month - optimalMonth);
        if (distance > 6) distance = 12 - distance; // Handle annual cycle

        // Maximum yield in optimal month, decreases with distance
        if (distance == 0) {
            return 1.2; // +20% in optimal period
        } else if (distance <= 1) {
            return 1.1; // +10% near optimal period
        } else if (distance == 2) {
            return 1.0; // Normal yield
        } else if (distance == 3) {
            return 0.85; // -15% far from optimal period
        } else {
            return 0.7; // -30% very far from optimal period
        }
    }

    /**
     * Calculates the price multiplier based on the season and crop
     */
    private double getSeasonalPriceMultiplier(OffsetDateTime timestamp, Crop crop) {
        int month = timestamp.getMonthValue();
        var seasonInfo = crop.getSeasonInfo();

        int harvestMonth = seasonInfo.harvestMonth();

        // During harvest: lower prices (abundance)
        if (month == harvestMonth || month == (harvestMonth % 12) + 1) {
            return 0.80; // -20% during harvest
        }
        // 1-2 months after harvest: low prices
        else if (month == (harvestMonth + 2) % 12 || month == (harvestMonth + 3) % 12) {
            return 0.90; // -10%
        }
        // Far from harvest: higher prices (scarcity)
        else if (month == (harvestMonth + 6) % 12 || month == (harvestMonth + 7) % 12) {
            return 1.25; // +25% at peak scarcity
        }
        // Intermediate period: high prices
        else if (month == (harvestMonth + 4) % 12 || month == (harvestMonth + 5) % 12 ||
                month == (harvestMonth + 8) % 12 || month == (harvestMonth + 9) % 12) {
            return 1.15; // +15%
        }
        // Other periods: normal prices
        else {
            return 1.0;
        }
    }
}
