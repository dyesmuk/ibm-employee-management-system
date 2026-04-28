// Store.tsx
import { configureStore } from '@reduxjs/toolkit';
import aboutReducer from './AboutSlice';
import employeeReducer from './EmployeeSlice';

// const str = configureStore({
//     reducer : {

//     }
// });

export const store = configureStore({
    reducer: {
        about: aboutReducer,
        employee: employeeReducer
        // more reducers here 
    }
});

export type RootState = ReturnType<typeof store.getState>;

export type AppDispatch = typeof store.dispatch;
