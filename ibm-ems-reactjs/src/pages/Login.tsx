// // Login.tsx

import { useState } from "react";
import axios from "axios";

const Login = () => {
    const apiUrl = "http://localhost:8080/api/v1/auth/login";
    const [username, setUsername] = useState("hr.admin");
    const [password, setPassword] = useState("Admin@IBM123");
    const [token, setToken] = useState("");
    const [postLogin, setPostLogin] = useState("");

    const login = async () => {
        try {
            const res = await axios.post(apiUrl, { username, password });
            setToken(res.data.token);
            localStorage.setItem("token", res.data.token);
            setPostLogin("Login successful!");

        } catch (err) {
            console.error(err);
            setPostLogin("Login failed!");
        }
    };

    return (
        <>
            <h2>Login</h2>

            <input
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
            />
            <br /><br />

            <input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
            />
            <br /><br />

            <button onClick={login}>Login</button>

            <br /><br />
            <div>{token && token.substring(0, 30) + "..."}</div>
            <p>{postLogin && postLogin}</p>
        </>
    );
};

export default Login;




// // Login.tsx

// const Login = () => {

//     return (
//         <>
//             <h2>Login Component</h2>
//         </>
//     );
// }

// export default Login;
