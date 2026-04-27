import axiosClient from "../api/axiosClient";
import type { EmployeePageResponse } from "../features/employees/employee.types";

export const getEmployeesApi = (page = 0, size = 10) => {
    return axiosClient.get<EmployeePageResponse>(
        `/employees?page=${page}&size=${size}`
    );
};