// AppRoutes.tsx 

import { Routes, Route, Navigate } from "react-router-dom";
import Home from "../pages/Home";
import About from "../pages/About";
// import Employee from "../pages/Employee";
import Page404 from "../pages/Page404";
import Register from "../pages/Register";
import Login from "../pages/Login";
import EmployeeList from "../pages/EmployeeList";
import EmployeeDetails from "../pages/EmployeeDetails";
import AddEmployee from "../pages/AddEmployee";

const AppRoutes = () => {

  return (
    <>
      <Routes>
        <Route path="/" element={<Navigate to="/home" replace />} />
        <Route path="/home" element={<Home />} />
        <Route path="/about" element={<About />} />
        {/* <Route path="/employee" element={<Employee />} /> */}
        <Route path="/employeelist" element={<EmployeeList />} />
        <Route path="/employee/:id" element={<EmployeeDetails />} />
        <Route path="/addemployee" element={<AddEmployee />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/*" element={<Page404 />} />
      </Routes>
    </>
  );
};

export default AppRoutes;
