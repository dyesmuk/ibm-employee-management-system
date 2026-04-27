import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginApi } from "../api/authApi";

const Login = () => {

    // real project fields 
    // const [username, setUsername] = useState("");
    // const [password, setPassword] = useState("");

    // demo project fields with hardcoded credentials 
    const [username, setUsername] = useState("hr.admin");
    const [password, setPassword] = useState("Admin@IBM123");
    const [message, setMessage] = useState("");

    const navigate = useNavigate();

    const handleLogin = async () => {
        console.log('handleLogin');
        try {
            const res = await loginApi({ username, password });

            const token = res.data.token;

            localStorage.setItem("token", token);
            setMessage("Login successful!");
            console.log("Logged in with ", token);

            setTimeout(() => {
                navigate("/home");
            }, 1000);
        } catch (err) {
            console.error(err);
            setMessage("Login failed!");
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
            <p>{message}</p>
        </>
    );
};

export default Login;