import { useState } from "react";
import Child from "./Child";
const Parent = () => {
    const parentData = "Sonu";
    const [childDataInParent, setChildDataInParent] = useState("");
    const getDataFromChild = (data: string) => {
        console.log("parent function called.");
        setChildDataInParent(data);
    };
    return (
        <>
            <h2>Parent Component</h2>
            <p>Parent data in parent component: {parentData}</p>
            <p>Child data in parent component: {childDataInParent}</p>
            <Child dataFromParent={parentData} sendDataToParent={getDataFromChild} />
        </>
    );
};
export default Parent;