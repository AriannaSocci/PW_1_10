package org.ancora_casini.PW_1_10.controller;

import lombok.AllArgsConstructor;
import org.ancora_casini.PW_1_10.model.Interval;
import org.ancora_casini.PW_1_10.model.TimeUnit;
import org.ancora_casini.PW_1_10.service.DashboardService;
import org.ancora_casini.PW_1_10.service.DataSimulatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
@AllArgsConstructor
public class DashboardController {


    private final DashboardService dashboardService;

    private final DataSimulatorService dataSimulatorService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(required = false) Optional<OffsetDateTime> start,
                            @RequestParam(required = false) Optional<OffsetDateTime> end) {
        var now = OffsetDateTime.now();
        if (start.isPresent() && end.isPresent() && start.get().isAfter(end.get())) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        if (start.isPresent() && end.isPresent() && end.get().isAfter(now)) {
            throw new IllegalArgumentException("Dates must be before current date.");
        }

        Map<String, Object> dashboardData = dashboardService.getDashboardData(
                start.orElse(OffsetDateTime.now().minusDays(7)),
                end.orElse(OffsetDateTime.now())
        );
        model.addAttribute("dashboardData", dashboardData);
        return "dashboard";
    }

    @GetMapping("/")
    public String showSimulationForm() {
        return "simulate";
    }

    @PostMapping("/simulate-data")
    public String simulateData(@RequestParam("dataType") String dataType,
                               @RequestParam("start") OffsetDateTime start,
                               @RequestParam("end") OffsetDateTime end,
                               @RequestParam("intervalValue") int intervalValue,
                               @RequestParam("intervalUnit") TimeUnit intervalUnit) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date must be before end date.");
        }
        if (dataType.equals("environmental") || dataType.equals("both")) {
            dataSimulatorService.generateEnvironmentalData(
                    start,
                    end,
                    new Interval(intervalValue, intervalUnit)
            );
        }
        if (dataType.equals("production") || dataType.equals("both")) {
            dataSimulatorService.generateProductionData(
                    start,
                    end,
                    new Interval(intervalValue, intervalUnit)
            );
        }
        return "redirect:/dashboard";
    }
}
