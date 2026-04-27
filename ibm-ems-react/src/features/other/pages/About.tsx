// About.tsx

import { useState } from "react";

const About = () => {
    const [input, setInput] = useState({ username: "", password: "" });
    const [output, setOutput] = useState({ username: "", password: "" });

    const handleInput = (evt) => {
        const { name, value } = evt.target;
        setInput({ ...input, [name]: value });
    };

    const handleSubmit = (evt) => {
        evt.preventDefault();
        setOutput(input);
        console.log("Form submitted:", input);
    };

    return (
        <>
            <h2>About Component</h2>
            <p>Forms in ReactJS</p>
            <hr />
            <>
                <form onSubmit={handleSubmit}>
                    <input type="text" value={input.username} 
                    onChange={handleInput} autoFocus />
                    <br />
                    <input type="password" value={input.password}
                        onChange={handleInput} />
                    <br />
                    <input type="submit" value="Submit" />
                </form>
            </>
            <p>Input: - {input.username}  - {input.password}</p>
            <p>Ouput: - {output.username} - {output.password}</p>
        </>
    );
}

export default About;