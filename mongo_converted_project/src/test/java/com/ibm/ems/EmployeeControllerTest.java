//package com.ibm.ems;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import com.ibm.ems.dto.EmployeeRequest;
//import com.ibm.ems.dto.EmployeeResponse;
//import com.ibm.ems.exception.GlobalExceptionHandler;
//import com.ibm.ems.exception.ResourceNotFoundException;
//import com.ibm.ems.model.Employee.EmployeeStatus;
//import com.ibm.ems.controller.EmployeeController;
//import com.ibm.ems.service.EmployeeService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.hamcrest.Matchers.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.eq;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(EmployeeController.class)
//@DisplayName("EmployeeController Tests")
//class EmployeeControllerTest {
//
//    @Autowired MockMvc mockMvc;
//    @MockBean  EmployeeService employeeService;
//
//    private ObjectMapper objectMapper;
//    private EmployeeResponse sampleResponse;
//    private EmployeeRequest  sampleRequest;
//
//    @BeforeEach
//    void setUp() {
//        objectMapper = new ObjectMapper()
//                .registerModule(new JavaTimeModule());
//
//        sampleResponse = new EmployeeResponse();
//        // Populate via reflection is messy — use a helper builder pattern or just
//        // rely on the service mock to return pre-built objects directly.
//        // We build a minimal EmployeeRequest that passes validation:
//        sampleRequest = new EmployeeRequest();
//        sampleRequest.setFirstName("Alice");
//        sampleRequest.setLastName("Smith");
//        sampleRequest.setEmail("alice.smith@ibm.com");
//        sampleRequest.setSalary(new BigDecimal("75000.00"));
//        sampleRequest.setJoinDate(LocalDate.of(2022, 1, 15));
//        sampleRequest.setDepartmentId(1L);
//        sampleRequest.setStatus(EmployeeStatus.ACTIVE);
//    }
//
//    // ── GET all ───────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("GET /api/v1/employees — returns 200 with list")
//    void getAll_Returns200() throws Exception {
//        when(employeeService.findAll()).thenReturn(List.of(sampleResponse));
//
//        mockMvc.perform(get("/api/v1/employees")
//                        .accept(MediaType.APPLICATION_JSON))
//               .andDo(print())
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$", hasSize(1)));
//
//        verify(employeeService).findAll();
//    }
//
//    // ── GET by ID ─────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("GET /api/v1/employees/{id} — returns 200 when found")
//    void getById_WhenExists_Returns200() throws Exception {
//        when(employeeService.findById(1L)).thenReturn(sampleResponse);
//
//        mockMvc.perform(get("/api/v1/employees/1"))
//               .andExpect(status().isOk());
//    }
//
//    @Test
//    @DisplayName("GET /api/v1/employees/{id} — returns 404 when not found")
//    void getById_WhenNotFound_Returns404() throws Exception {
//        when(employeeService.findById(999L))
//                .thenThrow(new ResourceNotFoundException("Employee", "id", 999L));
//
//        mockMvc.perform(get("/api/v1/employees/999"))
//               .andExpect(status().isNotFound())
//               .andExpect(jsonPath("$.status", is(404)));
//    }
//
//    // ── POST — create ─────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("POST /api/v1/employees — returns 201 with Location header")
//    void create_ValidRequest_Returns201() throws Exception {
//        when(employeeService.create(any(EmployeeRequest.class)))
//                .thenReturn(sampleResponse);
//
//        mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(sampleRequest)))
//               .andDo(print())
//               .andExpect(status().isCreated());
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/employees — returns 400 when firstName is blank")
//    void create_BlankFirstName_Returns400() throws Exception {
//        sampleRequest.setFirstName("");
//
//        mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(sampleRequest)))
//               .andExpect(status().isBadRequest())
//               .andExpect(jsonPath("$.fieldErrors.firstName", notNullValue()));
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/employees — returns 400 when email is invalid")
//    void create_InvalidEmail_Returns400() throws Exception {
//        sampleRequest.setEmail("not-an-email");
//
//        mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(sampleRequest)))
//               .andExpect(status().isBadRequest())
//               .andExpect(jsonPath("$.fieldErrors.email", notNullValue()));
//    }
//
//    @Test
//    @DisplayName("POST /api/v1/employees — returns 400 when salary is null")
//    void create_NullSalary_Returns400() throws Exception {
//        sampleRequest.setSalary(null);
//
//        mockMvc.perform(post("/api/v1/employees")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(sampleRequest)))
//               .andExpect(status().isBadRequest())
//               .andExpect(jsonPath("$.fieldErrors.salary", notNullValue()));
//    }
//
//    // ── PUT — update ──────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("PUT /api/v1/employees/{id} — returns 200 on success")
//    void update_ValidRequest_Returns200() throws Exception {
//        when(employeeService.update(eq(1L), any(EmployeeRequest.class)))
//                .thenReturn(sampleResponse);
//
//        mockMvc.perform(put("/api/v1/employees/1")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(sampleRequest)))
//               .andExpect(status().isOk());
//    }
//
//    // ── PATCH — salary ────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("PATCH /api/v1/employees/{id}/salary — returns 200")
//    void patchSalary_Returns200() throws Exception {
//        when(employeeService.updateSalary(eq(1L), any(BigDecimal.class)))
//                .thenReturn(sampleResponse);
//
//        mockMvc.perform(patch("/api/v1/employees/1/salary")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content("{\"salary\": 90000.00}"))
//               .andExpect(status().isOk());
//    }
//
//    // ── DELETE ────────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("DELETE /api/v1/employees/{id} — returns 204")
//    void delete_WhenExists_Returns204() throws Exception {
//        doNothing().when(employeeService).delete(1L);
//
//        mockMvc.perform(delete("/api/v1/employees/1"))
//               .andExpect(status().isNoContent());
//
//        verify(employeeService, times(1)).delete(1L);
//    }
//
//    @Test
//    @DisplayName("DELETE /api/v1/employees/{id} — returns 404 when not found")
//    void delete_WhenNotFound_Returns404() throws Exception {
//        doThrow(new ResourceNotFoundException("Employee", "id", 999L))
//                .when(employeeService).delete(999L);
//
//        mockMvc.perform(delete("/api/v1/employees/999"))
//               .andExpect(status().isNotFound());
//    }
//
//    // ── Search ────────────────────────────────────────────────────────────────
//
//    @Test
//    @DisplayName("GET /api/v1/employees/search — returns paginated results")
//    void search_ReturnsPaginatedResults() throws Exception {
//        var page = new PageImpl<>(List.of(sampleResponse),
//                PageRequest.of(0, 10), 1);
//        when(employeeService.search(eq("alice"), any())).thenReturn(page);
//
//        mockMvc.perform(get("/api/v1/employees/search")
//                        .param("keyword", "alice")
//                        .param("page", "0")
//                        .param("size", "10"))
//               .andExpect(status().isOk())
//               .andExpect(jsonPath("$.totalElements", is(1)));
//    }
//}
