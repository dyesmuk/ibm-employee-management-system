import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

const Logout = () => {

  const navigate = useNavigate();

  useEffect(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("username");

    navigate("/login");
  }, [navigate]);

  return (
    <main>
      <h2>Logging out...</h2>
    </main>
  );
};

export default Logout;