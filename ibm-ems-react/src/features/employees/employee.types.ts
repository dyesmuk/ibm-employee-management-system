export interface Employee {
    id: string;
    firstName: string;
    lastName: string;
    fullName: string;
    email: string;
    phone: string;
    departmentId: string;
    departmentName: string;
    roleId: string;
    roleName: string;
    status: string;
}

export interface EmployeePageResponse {
    content: Employee[];
    totalElements: number;
    totalPages: number;
    number: number; // current page
}