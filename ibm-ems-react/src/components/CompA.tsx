// CompA.tsx

import { useContext } from "react";
import { DataContext } from "../context/DataContext";

const CompA = () => {

    const { message, setMessage } = useContext(DataContext);

    return (
        <>
            <h2>CompA</h2>
            <p>CompA Component: {message}</p>
            <button onClick={() => setMessage("Monu")}>
                Update Message
            </button>
        </>

    );
}

export default CompA;