const Child = (props: any) => {
    const childData = "Monu";
    const sendData = () => {
        console.log("child function called.");
        props.sendDataToParent(childData);
    };
    return (
        <>
            <h2>Child Component</h2>
            <p>Parent data in child component: {props.dataFromParent}</p>
            <p>Child data in child component: {childData}</p>
            <button onClick={sendData}>Send child data to parent</button>

        </>
    );
};
export default Child;