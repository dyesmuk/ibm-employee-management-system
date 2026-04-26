import { useEffect, useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";

const EmployeeList = () => {

    const [employees, setEmployees] = useState<any[]>([]);
    const [error, setError] = useState("");
    
    const apiUrl = "http://localhost:8080/api/v1/employees";

    useEffect(() => {
        const fetchEmployees = async () => {
            try {
                const token = localStorage.getItem("token");

                const res = await axios.get(
                    apiUrl,
                    {
                        headers: {
                            Authorization: "Bearer " + token
                        }
                    }
                );
                setEmployees(res.data.content);
            } catch (err: any) {
                console.error(err);
                setError("Failed to fetch employees");
            }
        };

        fetchEmployees();
    }, []);

    return (
        <div>
            <h2>Employee List</h2>

            {error && <p>{error}</p>}

            <ul>
                {employees.map((emp) => (
                    <li key={emp.id}>
                        {emp.firstName} {emp.lastName}
                        <Link to={`/employee/${emp.id}`}> {emp.firstName} {emp.lastName} </Link>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default EmployeeList;


// import axios from "axios";
// import { useEffect, useState } from "react";
// import { Link } from "react-router-dom";

// interface Employee {
//     id: number;
//     name: string;
// }
// const EmployeeList = () => {
//     const apiUrl = "https://jsonplaceholder.typicode.com/users";
//     const [employeeList, setEmployeeList] = useState<Employee[]>([]);
//     useEffect(() => {
//         axios.get(apiUrl)
//             .then((resp) => {
//                 console.log(resp.data);
//                 setEmployeeList(resp.data);
//             })
//             .catch((err) => {
//                 console.error(err);
//             });
//     }, []);
//     return (
//         <>
//             <h2>EmployeeList Component</h2>
//             <div>
//                 {employeeList.map((emp) => (
//                     <div key={emp.id}>
//                         {emp.name} <Link to={`/employee/${emp.id}`}>{emp.name}</Link>
//                     </div>
//                 ))}
//             </div>
//         </>
//     );
// }
// export default EmployeeList;




