// Home.tsx
import { useSelector } from 'react-redux';

const Home = () => {

    const data = useSelector((state: { about: { username: "", password: "" } }) => state.about);

    return (
        <>
            <h2>Home Component</h2>
            <p>{data.username} {data.password}</p>
        </>
    );
}

export default Home;

