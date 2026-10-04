package com.financialtracker.backend.savingsgoal;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SavingsGoalController.class)
class SavingsGoalControllerTest {

    @MockitoBean
    private SavingsGoalService savingsGoalService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }
    @Test
    void contributeToSavingsGoalShouldRejectZeroAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "amount": 0,
                                "accountId": 5
                            }
                            """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void contributeToSavingsGoalShouldRejectNegativeAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "amount": -100,
                                "accountId": 5
                            }
                            """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void contributeToSavingsGoalShouldRejectMissingAccountId() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "amount": 100
                            }
                            """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void contributeToSavingsGoalShouldRejectMissingAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "accountId": 5
                            }
                            """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSavingsGoalShouldReturnNotFoundWhenGoalDoesNotExist() throws Exception {

        Mockito.when(savingsGoalService.getSavingsGoalById(999L))
                .thenThrow(new SavingsGoalNotFoundException(999L));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/savings-goals/999")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void contributeToSavingsGoalShouldReturnSuccess() throws Exception {

        SavingsGoalResponse response = new SavingsGoalResponse();

        response.setId(1L);
        response.setName("Emergency Fund");
        response.setTargetAmount(new BigDecimal("12000"));
        response.setCurrentAmount(new BigDecimal("4100"));
        response.setRemainingAmount(new BigDecimal("7900"));
        response.setProgressPercentage(new BigDecimal("34.17"));
        response.setRequiredMonthlySavings(new BigDecimal("493.75"));
        response.setTargetDate(LocalDate.of(2027, 12, 31));
        response.setActive(true);

        Mockito.when(
                savingsGoalService.contributeToSavingsGoal(
                        Mockito.eq(1L),
                        Mockito.any(SavingsContributionRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "amount": 100,
                            "accountId": 5
                        }
                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Emergency Fund"));
    }

    @Test
    void contributeToSavingsGoalShouldReturnBadRequestWhenContributionExceedsTarget()
            throws Exception {

        Mockito.when(
                savingsGoalService.contributeToSavingsGoal(
                        Mockito.eq(1L),
                        Mockito.any(SavingsContributionRequest.class)
                )
        ).thenThrow(
                new InvalidSavingsGoalException(
                        "Contribution would exceed the target amount"
                )
        );

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "amount": 500,
                                "accountId": 5
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Contribution would exceed the target amount"));
    }

    @Test
    void contributeToSavingsGoalShouldReturnBadRequestWhenAccountBalanceIsInsufficient()
            throws Exception {

        Mockito.when(
                savingsGoalService.contributeToSavingsGoal(
                        Mockito.eq(1L),
                        Mockito.any(SavingsContributionRequest.class)
                )
        ).thenThrow(
                new InsufficientAccountBalanceException(
                        "Insufficient account balance for this contribution"
                )
        );

        mockMvc.perform(
                        post("/api/savings-goals/1/contribute")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "amount": 500,
                                "accountId": 5
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Insufficient account balance for this contribution"));
    }

    @Test
    void createSavingsGoalShouldReturnSuccess() throws Exception {

        SavingsGoalResponse response = new SavingsGoalResponse();

        response.setId(3L);
        response.setName("Vacation Fund");
        response.setTargetAmount(new BigDecimal("5000"));
        response.setCurrentAmount(new BigDecimal("1000"));
        response.setRemainingAmount(new BigDecimal("4000"));
        response.setProgressPercentage(new BigDecimal("20.00"));
        response.setRequiredMonthlySavings(new BigDecimal("333.34"));
        response.setTargetDate(LocalDate.of(2027, 12, 31));
        response.setActive(true);

        Mockito.when(
                savingsGoalService.createSavingsGoal(
                        Mockito.any(SavingsGoalRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": 5000,
                                "currentAmount": 1000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Vacation Fund"))
                .andExpect(jsonPath("$.targetAmount").value(5000))
                .andExpect(jsonPath("$.currentAmount").value(1000))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createSavingsGoalShouldReturnBadRequestWhenCurrentAmountExceedsTarget()
            throws Exception {

        Mockito.when(
                savingsGoalService.createSavingsGoal(
                        Mockito.any(SavingsGoalRequest.class)
                )
        ).thenThrow(
                new InvalidSavingsGoalException(
                        "Current amount cannot be greater than target amount"
                )
        );

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Invalid Goal",
                                "targetAmount": 1000,
                                "currentAmount": 1500,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Current amount cannot be greater than target amount"));
    }

    @Test
    void createSavingsGoalShouldRejectMissingName() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "targetAmount": 5000,
                                "currentAmount": 1000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createSavingsGoalShouldRejectMissingTargetAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "currentAmount": 1000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createSavingsGoalShouldRejectMissingCurrentAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": 5000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
    @Test
    void createSavingsGoalShouldRejectMissingTargetDate() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": 5000,
                                "currentAmount": 1000
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createSavingsGoalShouldRejectZeroTargetAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": 0,
                                "currentAmount": 0,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createSavingsGoalShouldRejectNegativeTargetAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": -5000,
                                "currentAmount": 1000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createSavingsGoalShouldRejectZeroCurrentAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": 5000,
                                "currentAmount": 0,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createSavingsGoalShouldRejectNegativeCurrentAmount() throws Exception {

        mockMvc.perform(
                        post("/api/savings-goals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Vacation Fund",
                                "targetAmount": 5000,
                                "currentAmount": -100,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
    @Test
    void getSavingsGoalsShouldReturnSuccess() throws Exception {

        SavingsGoalResponse goal1 = new SavingsGoalResponse();
        goal1.setId(1L);
        goal1.setName("Emergency Fund");
        goal1.setTargetAmount(new BigDecimal("12000"));
        goal1.setCurrentAmount(new BigDecimal("4100"));
        goal1.setRemainingAmount(new BigDecimal("7900"));
        goal1.setProgressPercentage(new BigDecimal("34.17"));
        goal1.setRequiredMonthlySavings(new BigDecimal("493.75"));
        goal1.setTargetDate(LocalDate.of(2027, 12, 31));
        goal1.setActive(true);

        SavingsGoalResponse goal2 = new SavingsGoalResponse();
        goal2.setId(2L);
        goal2.setName("Vacation Fund");
        goal2.setTargetAmount(new BigDecimal("5000"));
        goal2.setCurrentAmount(new BigDecimal("1000"));
        goal2.setRemainingAmount(new BigDecimal("4000"));
        goal2.setProgressPercentage(new BigDecimal("20.00"));
        goal2.setRequiredMonthlySavings(new BigDecimal("250.00"));
        goal2.setTargetDate(LocalDate.of(2027, 12, 31));
        goal2.setActive(true);

        Mockito.when(savingsGoalService.getAllSavingsGoals())
                .thenReturn(java.util.List.of(goal1, goal2));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/savings-goals")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Emergency Fund"))
                .andExpect(jsonPath("$[0].targetAmount").value(12000))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Vacation Fund"));
    }
    @Test
    void getSavingsGoalByIdShouldReturnSuccess() throws Exception {

        SavingsGoalResponse response = new SavingsGoalResponse();

        response.setId(1L);
        response.setName("Emergency Fund");
        response.setTargetAmount(new BigDecimal("12000"));
        response.setCurrentAmount(new BigDecimal("4100"));
        response.setRemainingAmount(new BigDecimal("7900"));
        response.setProgressPercentage(new BigDecimal("34.17"));
        response.setRequiredMonthlySavings(new BigDecimal("493.75"));
        response.setTargetDate(LocalDate.of(2027, 12, 31));
        response.setActive(true);

        Mockito.when(savingsGoalService.getSavingsGoalById(1L))
                .thenReturn(response);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/savings-goals/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(12000))
                .andExpect(jsonPath("$.currentAmount").value(4100))
                .andExpect(jsonPath("$.remainingAmount").value(7900))
                .andExpect(jsonPath("$.progressPercentage").value(34.17))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updateSavingsGoalShouldReturnSuccess() throws Exception {

        SavingsGoalResponse response = new SavingsGoalResponse();

        response.setId(1L);
        response.setName("Updated Emergency Fund");
        response.setTargetAmount(new BigDecimal("15000"));
        response.setCurrentAmount(new BigDecimal("5000"));
        response.setRemainingAmount(new BigDecimal("10000"));
        response.setProgressPercentage(new BigDecimal("33.33"));
        response.setRequiredMonthlySavings(new BigDecimal("625.00"));
        response.setTargetDate(LocalDate.of(2027, 12, 31));
        response.setActive(true);

        Mockito.when(
                savingsGoalService.updateSavingsGoal(
                        Mockito.eq(1L),
                        Mockito.any(SavingsGoalRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/savings-goals/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Updated Emergency Fund",
                                "targetAmount": 15000,
                                "currentAmount": 5000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(15000))
                .andExpect(jsonPath("$.currentAmount").value(5000))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void updateSavingsGoalShouldReturnNotFoundWhenGoalDoesNotExist()
            throws Exception {

        Mockito.when(
                savingsGoalService.updateSavingsGoal(
                        Mockito.eq(999L),
                        Mockito.any(SavingsGoalRequest.class)
                )
        ).thenThrow(new SavingsGoalNotFoundException(999L));

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/savings-goals/999")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                                "name": "Updated Goal",
                                "targetAmount": 5000,
                                "currentAmount": 1000,
                                "targetDate": "2027-12-31"
                            }
                            """)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Savings goal with id 999 not found"));
    }
    @Test
    void deactivateSavingsGoalShouldReturnSuccess() throws Exception {

        SavingsGoalResponse response = new SavingsGoalResponse();

        response.setId(1L);
        response.setName("Emergency Fund");
        response.setTargetAmount(new BigDecimal("12000"));
        response.setCurrentAmount(new BigDecimal("4100"));
        response.setRemainingAmount(new BigDecimal("7900"));
        response.setProgressPercentage(new BigDecimal("34.17"));
        response.setRequiredMonthlySavings(new BigDecimal("493.75"));
        response.setTargetDate(LocalDate.of(2027, 12, 31));
        response.setActive(false);

        Mockito.when(savingsGoalService.deactivateSavingsGoal(1L))
                .thenReturn(response);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .patch("/api/savings-goals/1/deactivate")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Emergency Fund"))
                .andExpect(jsonPath("$.active").value(false));
    }
}
