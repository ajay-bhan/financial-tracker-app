package com.financialtracker.backend.dashboard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void getDashboardSummaryShouldReturnDashboardData() throws Exception {

        DashboardResponse response = new DashboardResponse();

        response.setTotalBalance(new BigDecimal("5750.00"));
        response.setTotalIncome(new BigDecimal("2000.00"));
        response.setTotalExpenses(new BigDecimal("750.00"));
        response.setNetCashFlow(new BigDecimal("1250.00"));
        response.setSavingsGoalProgress(new BigDecimal("71.18"));
        response.setBudgetUsagePercentage(new BigDecimal("65.00"));

        when(dashboardService.getDashboardSummary())
                .thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance").value(5750.00))
                .andExpect(jsonPath("$.totalIncome").value(2000.00))
                .andExpect(jsonPath("$.totalExpenses").value(750.00))
                .andExpect(jsonPath("$.netCashFlow").value(1250.00))
                .andExpect(jsonPath("$.savingsGoalProgress").value(71.18))
                .andExpect(jsonPath("$.budgetUsagePercentage").value(65.00));
    }
}
