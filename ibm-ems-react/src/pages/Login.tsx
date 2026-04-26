// // Login.tsx

import { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Login = () => {
    const apiUrl = "http://localhost:8080/api/v1/auth/login";
    const [username, setUsername] = useState("hr.admin");
    const [password, setPassword] = useState("Admin@IBM123");
    const [token, setToken] = useState("");
    const [postLogin, setPostLogin] = useState("");

    const navigate = useNavigate();

    const handleLogin = async () => {

        try {
            const res = await axios.post(apiUrl, { username, password });
            setToken(res.data.token);
            localStorage.setItem("token", res.data.token);
            setPostLogin("Login successful!");
            console.log(token);
            setTimeout(() => {
                navigate("/home");
            }, 2000);

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

            <button onClick={handleLogin}>Login</button>

            <br /><br />
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
