// CompB.tsx

import { useContext } from "react";
import { DataContext } from "../context/DataContext";

const CompB = () => {
    const { message } = useContext(DataContext);

    return (
        <>
        <h2>CompB</h2>
        <p>CompB Component: {message}</p>
        </>
    );
}

export default CompB;