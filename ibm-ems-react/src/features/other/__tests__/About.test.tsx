import { describe, it, expect } from "vitest";

describe("Demo test", () => {

    it("addition positive test", () => {
        expect(5 + 5).toBe(10);
    });

    it("addition negative test", () => {
        expect(5 + 5).not.toBe(11);
    });

});

// import { render, screen, fireEvent } from "@testing-library/react";
// import About from "../pages/About";
// import { Provider } from "react-redux";
// import { configureStore } from "@reduxjs/toolkit";
// import { describe, test, expect } from "vitest";

// const mockReducer = (state = { username: "", password: "" }, action: any) => {
//     if (action.type === "about/saveData") {
//         return action.payload;
//     }
//     return state;
// };

// const createTestStore = () =>
//     configureStore({
//         reducer: {
//             about: mockReducer
//         }
//     });

// const renderWithRedux = () => {
//     const store = createTestStore();
//     return render(
//         <Provider store={store}>
//             <About />
//         </Provider>
//     );
// };

// describe("About Component", () => {

//     test("renders component", () => {
//         renderWithRedux();
//         expect(screen.getByText("About Component")).toBeInTheDocument();
//     });

//     test("shows error when username is empty", () => {
//         renderWithRedux();

//         fireEvent.click(screen.getByDisplayValue("Submit"));

//         expect(screen.getByText("Username is required.")).toBeInTheDocument();
//     });

//     test("valid form submission works", () => {
//         renderWithRedux();

//         const usernameInput = screen.getByRole("textbox") as HTMLInputElement;
//         const passwordInput = document.querySelector("input[type='password']") as HTMLInputElement;

//         usernameInput.value = "Vaman";
//         passwordInput.value = "Abc@1234";

//         fireEvent.click(screen.getByDisplayValue("Submit"));

//         expect(screen.getByText(/Output:/)).toHaveTextContent("Vaman");
//     });

// });