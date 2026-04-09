//package com.ibm.ems;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.ibm.ems.dto.EmployeeRequest;
//import com.ibm.ems.model.Employee.EmployeeStatus;
//import com.ibm.ems.repository.EmployeeRepository;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@DisplayName("EMS Integration Tests")
//class EmsIntegrationTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired EmployeeRepository employeeRepository;
//
//    private final ObjectMapper objectMapper =
//            new ObjectMapper().registerModule(new JavaTimeModule());
//
//    // ── Employees ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("GET /api/v1/employees — seed data returns at least 10 employees")
//    void getAllEmployees_ReturnsSeedData() throws Exception {
//        mockMvc.perform(get("/api/v1/employees"))
//               .andDo(print())
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))));
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/employees/1 — returns Alice Smith from seed data")
//    void getEmployeeById1_ReturnsAlice() throws Exception {
//        mockMvc.perform(get("/api/v1/employees/1"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$.firstName", is("Alice")))
//               .andExpect(jsonPath("$.email",     is("alice.smith@ibm.com")));
//    }
//
//    @Test
//    @DisplayName("Full lifecycle — create, read, update salary, delete")
//    void employeeLifecycle() throws Exception {
//        // ── Create ────────────────────────────────────────────────────────────
//        EmployeeRequest req = new EmployeeRequest();
//        req.setFirstName("Test");
//        req.setLastName("User");
//        req.setEmail("test.user.lifecycle@ibm.com");
//        req.setSalary(new BigDecimal("50000.00"));
//        req.setJoinDate(LocalDate.now());
//        req.setDepartmentId(1L);
//        req.setStatus(EmployeeStatus.ACTIVE);
//
//        String createResponse = mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//               .andExpect(status().isCreated())
//               .andExpect(jsonPath("$.firstName", is("Test")))
//               .andExpect(jsonPath("$.email", is("test.user.lifecycle@ibm.com")))
//               .andReturn().getResponse().getContentAsString();
//
//        // Extract ID from response
//        Long newId = objectMapper.readTree(createResponse).get("id").asLong();
//
//        // ── Read ──────────────────────────────────────────────────────────────
//        mockMvc.perform(get("/api/v1/employees/" + newId))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$.id", is(newId.intValue())));
//
//        // ── Patch salary ──────────────────────────────────────────────────────
//        mockMvc.perform(patch("/api/v1/employees/" + newId + "/salary")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"salary\": 60000.00}"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$.salary", is(60000.00)));
//
//        // ── Delete ────────────────────────────────────────────────────────────
//        mockMvc.perform(delete("/api/v1/employees/" + newId))
//               .andExpect(status().isNoContent());
//
//        // ── Confirm deleted ───────────────────────────────────────────────────
//        mockMvc.perform(get("/api/v1/employees/" + newId))
//               .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("POST duplicate email — returns 409 Conflict")
//    void createDuplicateEmail_Returns409() throws Exception {
//        EmployeeRequest req = new EmployeeRequest();
//        req.setFirstName("Dupe");
//        req.setLastName("Test");
//        req.setEmail("alice.smith@ibm.com");  // already in seed data
//        req.setSalary(new BigDecimal("45000.00"));
//        req.setJoinDate(LocalDate.now());
//
//        mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//               .andExpect(status().isConflict())
//               .andExpect(jsonPath("$.status", is(409)));
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/employees/search?keyword=alice — returns matching results")
//    void searchByKeyword_ReturnsResults() throws Exception {
//        mockMvc.perform(get("/api/v1/employees/search")
//                        .param("keyword", "alice"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$.content", hasSize(greaterThan(0))))
//               .andExpect(jsonPath("$.content[0].firstName",
//                       containsStringIgnoringCase("alice")));
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/employees/salary-range — returns employees in range")
//    void getBySalaryRange_ReturnsFiltered() throws Exception {
//        mockMvc.perform(get("/api/v1/employees/salary-range")
//                        .param("min", "70000")
//                        .param("max", "90000"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$", hasSize(greaterThan(0))));
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/employees/status/ACTIVE — returns only active employees")
//    void getByStatus_ReturnsActiveOnly() throws Exception {
//        mockMvc.perform(get("/api/v1/employees/status/ACTIVE"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$[*].status", everyItem(is("ACTIVE"))));
//    }
//
//    // ── Departments ───────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("GET /api/v1/departments — returns 5 seeded departments")
//    void getAllDepartments_ReturnsSeedData() throws Exception {
//        mockMvc.perform(get("/api/v1/departments"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/departments/headcount — returns headcount per dept")
//    void getDepartmentHeadcount_ReturnsMap() throws Exception {
//        mockMvc.perform(get("/api/v1/departments/headcount"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$.Engineering", greaterThan(0)));
//    }
//
//    @Test
//    @DisplayName("DELETE dept with employees — returns 400")
//    void deleteDeptWithEmployees_Returns400() throws Exception {
//        // Department 1 (Engineering) has employees in seed data
//        mockMvc.perform(delete("/api/v1/departments/1"))
//               .andExpect(status().isBadRequest());
//    }
//
//    // ── Validation ────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("POST employee with missing required fields — returns 400 with fieldErrors")
//    void createEmployee_MissingFields_Returns400() throws Exception {
//        mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{}"))
//               .andExpect(status().isBadRequest())
//               .andExpect(jsonPath("$.fieldErrors", notNullValue()))
//               .andExpect(jsonPath("$.fieldErrors.firstName", notNullValue()))
//               .andExpect(jsonPath("$.fieldErrors.email",     notNullValue()))
//               .andExpect(jsonPath("$.fieldErrors.salary",    notNullValue()));
//    }
//
//    @Test
//    @DisplayName("GET non-existent employee — returns 404 with error body")
//    void getNonExistentEmployee_Returns404() throws Exception {
//        mockMvc.perform(get("/api/v1/employees/9999"))
//               .andExpect(status().isNotFound())
//               .andExpect(jsonPath("$.status",  is(404)))
//               .andExpect(jsonPath("$.error",   is("Not Found")))
//               .andExpect(jsonPath("$.message", notNullValue()));
//    }
//}
