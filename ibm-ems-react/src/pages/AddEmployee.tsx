import { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const AddEmployee = () => {

    const navigate = useNavigate();

    const [employee, setEmployee] = useState({
        firstName: "",
        lastName: "",
        email: "",
        phone: "",
        salary: "",
        hireDate: "",
        status: "ACTIVE",
        departmentId: "",
        roleId: ""
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        setEmployee({
            ...employee,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            await axios.post(
                "http://localhost:8080/api/v1/employees",
                {
                    ...employee,
                    salary: Number(employee.salary),
                    departmentId: Number(employee.departmentId),
                    roleId: Number(employee.roleId)
                },
                {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`
                    }
                }
            );

            navigate("/employeelist");

        } catch (error) {
            console.error("Error adding employee:", error);
        }
    };

    return (
        <main>
            <form onSubmit={handleSubmit}>

                <h2>Add Employee</h2>

                <label>First Name</label>
                <input name="firstName" value={employee.firstName} onChange={handleChange} required />

                <label>Last Name</label>
                <input name="lastName" value={employee.lastName} onChange={handleChange} required />

                <label>Email</label>
                <input name="email" type="email" value={employee.email} onChange={handleChange} required />

                <label>Phone</label>
                <input name="phone" value={employee.phone} onChange={handleChange} />

                <label>Salary</label>
                <input name="salary" type="number" value={employee.salary} onChange={handleChange} required />

                <label>Hire Date</label>
                <input name="hireDate" type="date" value={employee.hireDate} onChange={handleChange} required />

                <label>Status</label>
                <select name="status" value={employee.status} onChange={handleChange}>
                    <option value="ACTIVE">ACTIVE</option>
                    <option value="INACTIVE">INACTIVE</option>
                    <option value="ON_LEAVE">ON_LEAVE</option>
                    <option value="TERMINATED">TERMINATED</option>
                </select>

                <label>Department ID</label>
                <input name="departmentId" type="number" value={employee.departmentId} onChange={handleChange} required />

                <label>Role ID</label>
                <input name="roleId" type="number" value={employee.roleId} onChange={handleChange} required />

                <button type="submit">Add Employee</button>

            </form>
        </main>
    );
};

export default AddEmployee;