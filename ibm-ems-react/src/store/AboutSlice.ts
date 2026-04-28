// AboutSlice.ts
import { createSlice } from '@reduxjs/toolkit';

const initialState = {
    username: '',
    password: ''
};

const aboutSlice = createSlice({
    name: 'about',
    initialState,
    reducers: {
        saveData: (state, action) => {
            console.log(state);
            state.username = action.payload.username;
            state.password = action.payload.password;
        },
        updateData: (state, action) => {
            console.log(state);
            state.username = action.payload.username;
            state.password = action.payload.password;
        },
        deleteData: (state, action) => {
            console.log(state);
            state.username = action.payload.username;
            state.password = action.payload.password;
        }
    }
});

export { initialState };
export const { saveData } = aboutSlice.actions;
export default aboutSlice.reducer;
