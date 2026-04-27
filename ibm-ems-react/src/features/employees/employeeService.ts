import { getEmployeesApi } from "../../api/employeeApi";
import type { Employee } from "../employees/employee.types";

export const fetchEmployees = async (
  page = 0,
  size = 10
): Promise<Employee[]> => {
  const res = await getEmployeesApi(page, size);

  // No transformation needed — backend already gives fullName
  return res.data.content;
};