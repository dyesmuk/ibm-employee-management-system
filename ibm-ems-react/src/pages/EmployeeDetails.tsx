import axios from "axios";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const EmployeeDetails = () => {

    const { id } = useParams();
    const [employee, setEmployee] = useState<any>(null);
    const [error, setError] = useState("");
    const apiUrl = "http://localhost:8080/api/v1/employees";

    useEffect(() => {

        const fetchEmployee = async () => {
            try {
                const token = localStorage.getItem("token");

                const res = await axios.get(
                    `${apiUrl}/${id}`,
                    {
                        headers: {
                            Authorization: "Bearer " + token
                        }
                    }
                );

                setEmployee(res.data);

            } catch (err) {
                console.error(err);
                setError("Failed to fetch employee");
            }
        };

        if (id) {
            fetchEmployee();
        }

    }, [id]);

    if (!employee && !error) {
        return <p>Loading...</p>;
    }

    if (error) {
        return <p>{error}</p>;
    }

    return (
        <div>
            <h2>Employee Details</h2>

            <p><b>ID:</b> {employee.id}</p>
            <p><b>Name:</b> {employee.fullName}</p>
            <p><b>Email:</b> {employee.email}</p>
            <p><b>Phone:</b> {employee.phone}</p>

            <p><b>Department:</b> {employee.departmentName}</p>
            <p><b>Role:</b> {employee.roleName}</p>

            <p><b>Status:</b> {employee.status}</p>
            <p><b>Hire Date:</b> {employee.hireDate}</p>

            {employee.salary && (
                <p><b>Salary:</b> ₹ {employee.salary}</p>
            )}
        </div>
    );
};

export default EmployeeDetails;



// // EmployeeDetails.tsx
// // EmployeeDetails.tsx

// import axios from "axios";
// import { useEffect, useState } from "react";
// import { useParams } from "react-router-dom";

// const EmployeeDetails = () => {

//     const { id } = useParams();
//     const [employee, setEmployee] = useState<any>({});

//     useEffect(() => {
        
//         if (!id) return;

//         const apiUrl = `https://jsonplaceholder.typicode.com/users/${id}`;

//         axios.get(apiUrl)
//             .then((resp) => {
//                 console.log(resp.data);
//                 setEmployee(resp.data);
//             })
//             .catch((err) => console.error(err));

//     }, [id]);

//     return (
//         <>
//             <h2>Employee Details</h2>
//             <p><b>ID:</b> {employee?.id}</p>
//             <p><b>Name:</b> {employee?.name}</p>
//             <p><b>Email:</b> {employee?.email}</p>
//             <p><b>Phone:</b> {employee?.phone}</p>
//         </>
//     );
// };

// export default EmployeeDetails;
