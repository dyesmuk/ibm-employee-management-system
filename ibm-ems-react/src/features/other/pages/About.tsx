// About.tsx

import { useState } from "react";

const About = () => {

    const [input, setInput] = useState({ username: "", password: "" });
    const [output, setOutput] = useState({ username: "", password: "" });
    const [error, setError] = useState("");

    const inputValidator = () => {
        console.log("inputValidator");
        if (usernameValidator(input.username) && passwordValidator(input.password))
            return true;
        else
            return false;
    };

    const usernameValidator = (user: string) => {
        // validations - required, min, max
        if (user.length > 4 && user.length < 20) {
            console.log("username validated");
            return true;
        }
        else
            return false;
    };
    const passwordValidator = (pass) => {
        // validations - required, min, max, one each - upper, lower, number, special char 
        if (pass.length > 4 && pass.length < 20) {
            console.log("password validated");
            return true;
        }
        else
            return false;

    };

    const handleInput = (evt) => {
        console.log(evt.target);
        const { name, value } = evt.target;
        setInput({ ...input, [name]: value });
    };

    const handleSubmit = (evt) => {
        evt.preventDefault();
        if (inputValidator()) {
            setOutput(input); // call rest apis 
            console.log("Form submitted:", input);
            setInput({ username: "", password: "" });
        }
        else {
            setError("Invalid inputs");
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
                    <input type="text" name="username"
                        value={input.username} onChange={handleInput} required autoFocus />
                    <br />
                    <input type="password" name="password"
                        value={input.password} onChange={handleInput} required />
                    <br />
                    <input type="submit" value="Submit" />
                </form>
            </>
            <p> {(error) && error} </p>
            <p>Input: - {input.username}  - {input.password}</p>
            <p>Ouput: - {output.username} - {output.password}</p>
            {/* <p>Validations for username: min length, max length, no special chars</p>
            <p>Validations for password: min length, max length, at least one upper, one lower, one special chars</p> */}
        </>
    );
}

export default About;






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


