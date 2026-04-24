// Employee.tsx

import axios from "axios";
import { useEffect, useState } from "react";

const Employee = () => {

    const apiUrl = "https://jsonplaceholder.typicode.com/users/2";
    const [employee, setEmployee] = useState({});

    useEffect(() => {
        axios.get(apiUrl)
            .then((resp) => {
                console.log(resp.data);
                setEmployee(resp.data);
                console.log(employee);
            })
            .catch((err) => { console.error(err) });
    }, []);

    return (
        <>
            <h2>Employee Component</h2>
            {/* <p> Employee data: {employee?.name} </p> */}
        </>
    );
}

export default Employee;