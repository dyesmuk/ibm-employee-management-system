
import { Link, useNavigate } from "react-router-dom";
import logo from "../assets/ibm-logo.png";

const Navbar = () => {

    const navigate = useNavigate();

    const isLoggedIn = !!localStorage.getItem("token");

    const handleLogout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        navigate("/login");
    };

    return (
        <nav>

            <Link to="/home">
                <img src={logo} height="20px" alt="Home" />
            </Link>

            <div>
                <Link to="/about">About</Link>

                {!isLoggedIn && (
                    <>
                        <Link to="/register">Register</Link>
                        <Link to="/login">Login</Link>
                        {/* added new route  */}
                        <Link to="/login2">Login2</Link>
                    </>
                )}

                {isLoggedIn && (<>
                    <Link to="/employeelist">Employee List</Link>
                    <button onClick={handleLogout}>
                        Logout
                    </button>
                </>
                )}
            </div>

        </nav>
    );
};

export default Navbar;

// import { Link } from "react-router-dom";
// import logo from "../assets/ibm-logo-transparent.png";

// const Navbar = () => {

//     const navStyle = {
//         display: "flex",
//         justifyContent: "space-between",
//         alignItems: "center",
//         padding: "10px 20px"
//     };

//     const linkStyle = {
//         textDecoration: "none",
//         color: "inherit",
//         marginLeft: "15px"
//     };

//     const rightMenuStyle = {
//         display: "flex",
//         alignItems: "center"
//     };

//     return (
//         <nav style={navStyle}>

//             <Link to="/home">
//                 <img src={logo} height="20px" alt="Home" />
//             </Link>

//             <div style={rightMenuStyle}>
//                 <Link to="/about" style={linkStyle}>About</Link>
//                 <Link to="/employeelist" style={linkStyle}>Employee List</Link>
//                 <Link to="/register" style={linkStyle}>Register</Link>
//                 <Link to="/login" style={linkStyle}>Login</Link>
//             </div>

//         </nav>
//     );
// };

// export default Navbar;