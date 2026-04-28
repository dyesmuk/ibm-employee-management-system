// About.tsx

import { useRef, useState } from "react";
import { useDispatch, useSelector } from 'react-redux';
import { saveData } from '../../../store/AboutSlice';

const About = () => {

    const dispatch = useDispatch();

    const data = useSelector((state: { about: { username: "", password: "" } }) => state.about);

    const [input, setInput] = useState({ username: "", password: "" });
    const [output, setOutput] = useState({ username: "", password: "" });
    const [error, setError] = useState("");

    const usernameRef = useRef<HTMLInputElement>(null);
    const passwordRef = useRef<HTMLInputElement>(null);
    
    const usernameValidator = (user: string) => {
        if (!user)
            return "Username is required.";
        else if (user.length < 4)
            return "Username must be minimum 4 characters long.";
        else if (user.length > 20)
            return "Username must be maximum 20 characters long.";
        else
            return "";
    };

    const passwordValidator = (pass: string) => {
        if (!pass)
            return "Password is required.";
        else if (pass.length < 4)
            return "Password must be minimum 4 characters long.";
        else if (pass.length > 20)
            return "Password must be maximum 20 characters long.";
        else if (!/(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#*$])/.test(pass))
            return "Password must contain at least one lower, upper, number ans special";
        else
            return "";
    };

    const inputValidator = (data: { username: string; password: string }) => {
        console.log("inputValidator", data);

        const usernameError = usernameValidator(data.username);
        const passwordError = passwordValidator(data.password);

        if (usernameError) {
            setError(usernameError);
            return false;
        }
        if (passwordError) {
            setError(passwordError);
            return false;
        }
        setError("");
        return true;
    };

    // const handleInput = (evt: any) => {
    //     const { name, value } = evt.target;
    //     setInput({ ...input, [name]: value });
    //     if (error) {
    //         setError("");
    //     }
    // };

    const handleSubmit = (evt: any) => {
        evt.preventDefault();

        // // Uncontrolled form - 3 - extract values into component  
        const input = {
            username: usernameRef.current.value,
            password: passwordRef.current.value
        };

        if (inputValidator(input)) {
            setOutput(input);
            console.log("Form submitted:", input);
            setInput({ username: "", password: "" });
            dispatch(saveData(input));

            setError("");
        } else {
            console.error("Invalid inputs!");
        }
    };

    return (
        <>
            <h2>About Component</h2>
            <p>Forms in ReactJS</p>
            <hr />
            <>
                <form onSubmit={handleSubmit}>
                    {/* // // Uncontrolled form - 2 - capture user inputs  */}
                    <input type="text" name="username" ref={usernameRef} autoFocus />
                    {/* <input type="text" name="username" value={input.username} onChange={handleInput} autoFocus /> */}
                    <br />
                    <input type="password" name="password" ref={passwordRef} />
                    {/* <input type="password" name="password" value={input.password} onChange={handleInput} /> */}
                    <br />
                    <input type="submit" value="Submit" />
                </form>
            </>
            <p> {(error) && error} </p>
            <p>Input: - {input.username}  - {input.password}</p>
            <p>Output: - {output.username} - {output.password}</p>
            <h3>Data from Redux Store:</h3>
            <p>Username: {data.username}</p>
            <p>Password: {data.password}</p>
        </>
    );
}

export default About;


// // About.tsx

// import { useState } from "react";

// const About = () => {
//     const [input, setInput] = useState({ username: "", password: "" });
//     const [output, setOutput] = useState({ username: "", password: "" });
//     const [error, setError] = useState("");

//     const usernameValidator = (user: string) => {
//         if (!user)
//             return "Username is required.";
//         else if (user.length < 4)
//             return "Username must be minimum 4 characters long.";
//         else if (user.length > 20)
//             return "Username must be maximum 20 characters long.";
//         else
//             return "";
//     };

//     const passwordValidator = (pass: string) => {
//         if (!pass)
//             return "Password is required.";
//         else if (pass.length < 4)
//             return "Password must be minimum 4 characters long.";
//         else if (pass.length > 20)
//             return "Password must be maximum 20 characters long.";
//         else if (!/(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[@#*$])/.test(pass))
//             return "Password must contain at least one lower, upper, number ans special";
//         else
//             return "";
//     };

//     const inputValidator = (data: { username: string; password: string }) => {
//         console.log("inputValidator", data);

//         const usernameError = usernameValidator(data.username);
//         const passwordError = passwordValidator(data.password);

//         if (usernameError) {
//             setError(usernameError);
//             return false;
//         }
//         if (passwordError) {
//             setError(passwordError);
//             return false;
//         }
//         setError("");
//         return true;
//     };

//     const handleInput = (evt: any) => {
//         const { name, value } = evt.target;
//         setInput({ ...input, [name]: value });
//         if (error) {
//             setError("");
//         }
//     };

//     const handleSubmit = (evt: any) => {
//         evt.preventDefault();

//         if (inputValidator(input)) {
//             setOutput(input);
//             console.log("Form submitted:", input);
//             setInput({ username: "", password: "" });
//             setError("");
//         } else {
//             console.error("Invalid inputs!");
//         }
//     };

//     return (
//         <>
//             <h2>About Component</h2>
//             <p>Forms in ReactJS</p>
//             <hr />
//             <>
//                 <form onSubmit={handleSubmit}>
//                     <input type="text" name="username"
//                         value={input.username} onChange={handleInput} autoFocus />
//                     <br />
//                     <input type="password" name="password"
//                         value={input.password} onChange={handleInput} />
//                     <br />
//                     <input type="submit" value="Submit" />
//                 </form>
//             </>
//             <p> {(error) && error} </p>
//             <p>Input: - {input.username}  - {input.password}</p>
//             <p>Ouput: - {output.username} - {output.password}</p>
//         </>
//     );
// }

// export default About;






// // About.tsx

// import { useState } from "react";

// const About = () => {

//     const [input, setInput] = useState({ username: "", password: "" });
//     const [output, setOutput] = useState({ username: "", password: "" });

//     const handleInput = (evt) => {
//         console.log(evt.target);
//         const { name, value } = evt.target;
//         setInput({ ...input, [name]: value });
//     };

//     const handleSubmit = (evt) => {
//         evt.preventDefault();
//         setOutput(input); // call rest apis
//         console.log("Form submitted:", input);
//         setInput({ username: "", password: "" });
//     };

//     return (
//         <>
//             <h2>About Component</h2>
//             <p>Forms in ReactJS</p>
//             <hr />
//             <>
//                 <form onSubmit={handleSubmit}>
//                     <input type="text" name="username"
//                         value={input.username} onChange={handleInput} required autoFocus />
//                     <br />
//                     <input type="password" name="password"
//                         value={input.password} onChange={handleInput} required />
//                     <br />
//                     <input type="submit" value="Submit" />
//                 </form>
//             </>
//             <p>Validations for username: min length, max length, no special chars</p>
//             <p>Validations for password: min length, max length, at least one upper, one lower, one special chars</p>
//             {/* <p>Conditional rendering</p> */}
//             {/* {(true) && <p>Show</p>} */}
// {/* {(input.username) && <p>Input: - {input.username}</p>} */}
// {/* {(input.username) ? <p>Input:-{input.username}</p> : <p>Ouput:-{output.username}</p>} */}

//             <p>Input: - {input.username}  - {input.password}</p>
//             <p>Ouput: - {output.username} - {output.password}</p>
//         </>
//     );
// }

// export default About;




// // About.tsx

// import { useState } from "react";

// const About = () => {

//     const [input, setInput] = useState({ username: "", password: "" });
//     const [output, setOutput] = useState({ username: "", password: "" });
//     const [error, setError] = useState("");

//     const inputValidator = () => {
//         console.log("inputValidator");
//         if (usernameValidator(input.username) && passwordValidator(input.password))
//             return true;
//         else
//             return false;
//     };

//     const usernameValidator = (user: string) => {
//         // validations - required, min, max
//         if (user.length > 4 && user.length < 20) {
//             console.log("username validated");
//             return true;
//         }
//         else
//             return false;
//     };
//     const passwordValidator = (pass) => {
//         // validations - required, min, max, one each - upper, lower, number, special char
//         if (pass.length > 4 && pass.length < 20) {
//             console.log("password validated");
//             return true;
//         }
//         else
//             return false;

//     };

//     const handleInput = (evt) => {
//         console.log(evt.target);
//         const { name, value } = evt.target;
//         setInput({ ...input, [name]: value });
//     };

//     const handleSubmit = (evt) => {
//         evt.preventDefault();
//         if (inputValidator()) {
//             setOutput(input); // call rest apis
//             console.log("Form submitted:", input);
//             setInput({ username: "", password: "" });
//         }
//         else {
//             setError("Invalid inputs");
//             console.error("Invalid inputs!");
//         }
//     };

//     return (
//         <>
//             <h2>About Component</h2>
//             <p>Forms in ReactJS</p>
//             <hr />
//             <>
//                 <form onSubmit={handleSubmit}>
//                     <input type="text" name="username"
//                         value={input.username} onChange={handleInput} required autoFocus />
//                     <br />
//                     <input type="password" name="password"
//                         value={input.password} onChange={handleInput} required />
//                     <br />
//                     <input type="submit" value="Submit" />
//                 </form>
//             </>
//             <p> {(error) && error} </p>
//             <p>Input: - {input.username}  - {input.password}</p>
//             <p>Ouput: - {output.username} - {output.password}</p>
//             {/* <p>Validations for username: min length, max length, no special chars</p>
//             <p>Validations for password: min length, max length, at least one upper, one lower, one special chars</p> */}
//         </>
//     );
// }

// export default About;






// // // About.tsx

// // import { useState } from "react";

// // const About = () => {

// //     const [input, setInput] = useState({ username: "", password: "" });
// //     const [output, setOutput] = useState({ username: "", password: "" });

// //     const handleInput = (evt) => {
// //         console.log(evt.target);
// //         const { name, value } = evt.target;
// //         setInput({ ...input, [name]: value });
// //     };

// //     const handleSubmit = (evt) => {
// //         evt.preventDefault();
// //         setOutput(input); // call rest apis
// //         console.log("Form submitted:", input);
// //         setInput({ username: "", password: "" });
// //     };

// //     return (
// //         <>
// //             <h2>About Component</h2>
// //             <p>Forms in ReactJS</p>
// //             <hr />
// //             <>
// //                 <form onSubmit={handleSubmit}>
// //                     <input type="text" name="username"
// //                         value={input.username} onChange={handleInput} required autoFocus />
// //                     <br />
// //                     <input type="password" name="password"
// //                         value={input.password} onChange={handleInput} required />
// //                     <br />
// //                     <input type="submit" value="Submit" />
// //                 </form>
// //             </>
// //             <p>Validations for username: min length, max length, no special chars</p>
// //             <p>Validations for password: min length, max length, at least one upper, one lower, one special chars</p>
// //             {/* <p>Conditional rendering</p> */}
// //             {/* {(true) && <p>Show</p>} */}
// // {/* {(input.username) && <p>Input: - {input.username}</p>} */}
// // {/* {(input.username) ? <p>Input:-{input.username}</p> : <p>Ouput:-{output.username}</p>} */}

// //             <p>Input: - {input.username}  - {input.password}</p>
// //             <p>Ouput: - {output.username} - {output.password}</p>
// //         </>
// //     );
// // }

// // export default About;




