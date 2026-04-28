// EmployeeSlice.ts

import { createSlice, type PayloadAction } from '@reduxjs/toolkit';
import { type Employee } from '../features/employees/employee.types';


interface EmployeeState {
    employees: Employee[];
    currentEmployee: Employee | null;
    loading: boolean;
    error: string | null;
}

const initialState: EmployeeState = {
    employees: [],
    currentEmployee: null,
    loading: false,
    error: null
};

const employeeSlice = createSlice({
    name: 'employees',
    initialState,
    reducers: {
        setEmployees: (state, action: PayloadAction<Employee[]>) => {
            state.employees = action.payload;
            state.loading = false;
            state.error = null;
        },

        setCurrentEmployee: (state, action: PayloadAction<Employee>) => {
            state.currentEmployee = action.payload;
            state.loading = false;
        },

        addEmployee: (state, action: PayloadAction<Employee>) => {
            state.employees.push(action.payload);
        },

        updateEmployee: (state, action: PayloadAction<Employee>) => {
            const index = state.employees.findIndex(emp => emp.id === action.payload.id);
            if (index !== -1) {
                state.employees[index] = action.payload;
            }
            state.currentEmployee = action.payload;
        },

        removeEmployee: (state, action: PayloadAction<string>) => {
            state.employees = state.employees.filter(emp => emp.id !== action.payload);
            if (state.currentEmployee?.id === action.payload) {
                state.currentEmployee = null;
            }
        },

        setLoading: (state, action: PayloadAction<boolean>) => {
            state.loading = action.payload;
        },

        setError: (state, action: PayloadAction<string | null>) => {
            state.error = action.payload;
            state.loading = false;
        },

        clearEmployees: (state) => {
            state.employees = [];
            state.currentEmployee = null;
            state.error = null;
        }
    }
});

export const {
    setEmployees,
    setCurrentEmployee,
    addEmployee,
    updateEmployee,
    removeEmployee,
    setLoading,
    setError,
    clearEmployees
} = employeeSlice.actions;

export default employeeSlice.reducer;