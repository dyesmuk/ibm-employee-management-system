// App.tsx

import AppRoutes from "./routes/AppRoutes";

const App = () => {
  return (
    <div>
      <>
        <AppRoutes />
      </>
    </ div>
  )
}
export default App;


// // App.tsx

// // // using routing 
// import AppRoutes from "./old-stuff/routes/AppRoutes";
// import Navbar from "./pages/Navbar";

// const App = () => {
//   return (
//     <div>
//       <>
//         <Navbar />
//         <AppRoutes />
//       </>
//     </ div>
//   )
// }
// export default App;


// // // // data sharing bw components using context 
// // // =========================================== 

// // // // // App.tsx
// // import { DataContext } from "./DataContext";
// // import CompA from "./CompA";
// // import CompB from "./CompB";
// // import { useState } from "react";
// // const App = () => {

// //   const [message, setMessage] = useState("Sonu");

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <>
// //         <h2>App Component</h2>
// //         <p>App Component: {message}</p>
// //         <DataContext.Provider value={{ message, setMessage }}>
// //           <CompA />
// //           <CompB />
// //         </DataContext.Provider>
// //       </>
// //     </ div>
// //   )
// // }
// // export default App;



// // // data sharing bw parent and child using props 
// // // ============================================
// // import Parent from "./Parent";

// // const App = () => {

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <>
// //         <h2>App Component</h2>
// //         <Parent />
// //       </>
// //     </ div>
// //   )
// // }
// // export default App;



// // import MyButton from "./MyButton";
// // const App = () => {
// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <>
// //         <MyButton />
// //         <br />
// //         <MyButton />
// //       </>
// //     </ div>
// //   )
// // }
// // export default App;



// // // count will be the same on jsx  and console log  
// // import { useEffect, useState } from "react";

// // const App = () => {
// //   const [count, setCount] = useState(0);
// //   // syntax - 
// //   // useEffect(() => {}, []);
// //   useEffect(() => {
// //     console.log(`count increased: ${count}`);
// //   }, [count]);

// //   const increaseCount = () => {
// //     setCount(count + 1);
// //   };

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>{count}</p>
// //       <button onClick={(increaseCount)}>Click to increase count</button>
// //     </ div>
// //   )
// // }

// // export default App;


// // // console.log has stale value!
// // import { useState } from "react";

// // const App = () => {

// //   const [count, setCount] = useState(0);

// //   const increaseCount = () => {
// //     setCount(count + 1);
// //     console.log(`count increased: ${count}`); // 0 1
// //   };

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>{count}</p>
// //       <button onClick={(increaseCount)}>Click to increase count</button>
// //     </ div>
// //   )
// // }

// // export default App;



// // import { useState } from "react";

// // const App = () => {

// //   const [count, setCount] = useState(0);
// //   // const [employee, setEmployee] = useState({});
// //   // const [phones, setPhones] = useState([]);
// //   // const [myName , setMyName] = useState("");
// //   // const [isIndian , setIsIndian] = useState(false);

// //   const increaseCount = () => {
// //     setCount(count + 1);
// //     console.log(`count increased: ${count}`);
// //   };

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>{count}</p>
// //       <button onClick={(increaseCount)}>Click to increase count</button>
// //     </ div>
// //   )
// // }

// // export default App;

// // // this code does not work! 
// // const App = () => {

// //   let count = 0;

// //   const increaseCount = () => {
// //     count++;
// //     console.log("count increased.")
// //   };

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>{count}</p>
// //       <button onClick={(increaseCount)}>Click to increase count</button>
// //     </ div>
// //   )
// // }

// // export default App;

// // // // Components and JSX in JavaScript 

// // const App = () => {

// //   const myStyle = { color: "blue ", border: "solid" };
// //   const salary = 10.50;

// //   return (
// //     <div>
// //       <h1 style={{ color: "blue ", border: "solid" }}>IBM React App</h1>
// //       <h1 style={myStyle}>IBM React App</h1>
// //       <hr />
// //       <>
// //         <p>My salary: {salary} </p>
// //         <p>My salary: {10 + 20} </p>
// //         <p>Welcome to IBM ReactJS Application.</p>
// //         <p>This demo code is for IBM Bengaluru trainees 26 march 2026.</p>
// //       </>
// //     </ div>
// //   )
// // }

// // export default App;


// // // // Class component 

// // import React from "react";

// // class App extends React.Component {

// //   render = () => {
// //     return (
// //       <div>
// //         <h1>IBM React App</h1>
// //         <hr />
// //         <p>Welcome to IBM ReactJS Application.</p>
// //       </ div>
// //     );
// //   };
// // }
// // export default App;

// // // Modern JS function component 

// // const App = () => {

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>Welcome to IBM ReactJS Application.</p>
// //     </ div>
// //   )
// // }

// // export default App;


// // // Old JS function component 

// // function App() {

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>Welcome to IBM ReactJS Application.</p>
// //     </ div>
// //   )
// // }

// // export default App;


// // // // Minimal code 

// // function App() {

// //   return (
// //     <div>
// //       <h1>IBM React App</h1>
// //       <hr />
// //       <p>Welcome to IBM ReactJS Application.</p>
// //     </ div>
// //   )
// // }

// // export default App;

// // // Original Code 
// // import { useState } from 'react'
// // import reactLogo from './assets/react.svg'
// // import viteLogo from './assets/vite.svg'
// // import heroImg from './assets/hero.png'
// // import './App.css'

// // function App() {
// //   const [count, setCount] = useState(0)

// //   return (
// //     <>
// //       <section id="center">
// //         <div className="hero">
// //           <img src={heroImg} className="base" width="170" height="179" alt="" />
// //           <img src={reactLogo} className="framework" alt="React logo" />
// //           <img src={viteLogo} className="vite" alt="Vite logo" />
// //         </div>
// //         <div>
// //           <h1>Get started</h1>
// //           <p>
// //             Edit <code>src/App.tsx</code> and save to test <code>HMR</code>
// //           </p>
// //         </div>
// //         <button
// //           className="counter"
// //           onClick={() => setCount((count) => count + 1)}
// //         >
// //           Count is {count}
// //         </button>
// //       </section>

// //       <div className="ticks"></div>

// //       <section id="next-steps">
// //         <div id="docs">
// //           <svg className="icon" role="presentation" aria-hidden="true">
// //             <use href="/icons.svg#documentation-icon"></use>
// //           </svg>
// //           <h2>Documentation</h2>
// //           <p>Your questions, answered</p>
// //           <ul>
// //             <li>
// //               <a href="https://vite.dev/" target="_blank">
// //                 <img className="logo" src={viteLogo} alt="" />
// //                 Explore Vite
// //               </a>
// //             </li>
// //             <li>
// //               <a href="https://react.dev/" target="_blank">
// //                 <img className="button-icon" src={reactLogo} alt="" />
// //                 Learn more
// //               </a>
// //             </li>
// //           </ul>
// //         </div>
// //         <div id="social">
// //           <svg className="icon" role="presentation" aria-hidden="true">
// //             <use href="/icons.svg#social-icon"></use>
// //           </svg>
// //           <h2>Connect with us</h2>
// //           <p>Join the Vite community</p>
// //           <ul>
// //             <li>
// //               <a href="https://github.com/vitejs/vite" target="_blank">
// //                 <svg
// //                   className="button-icon"
// //                   role="presentation"
// //                   aria-hidden="true"
// //                 >
// //                   <use href="/icons.svg#github-icon"></use>
// //                 </svg>
// //                 GitHub
// //               </a>
// //             </li>
// //             <li>
// //               <a href="https://chat.vite.dev/" target="_blank">
// //                 <svg
// //                   className="button-icon"
// //                   role="presentation"
// //                   aria-hidden="true"
// //                 >
// //                   <use href="/icons.svg#discord-icon"></use>
// //                 </svg>
// //                 Discord
// //               </a>
// //             </li>
// //             <li>
// //               <a href="https://x.com/vite_js" target="_blank">
// //                 <svg
// //                   className="button-icon"
// //                   role="presentation"
// //                   aria-hidden="true"
// //                 >
// //                   <use href="/icons.svg#x-icon"></use>
// //                 </svg>
// //                 X.com
// //               </a>
// //             </li>
// //             <li>
// //               <a href="https://bsky.app/profile/vite.dev" target="_blank">
// //                 <svg
// //                   className="button-icon"
// //                   role="presentation"
// //                   aria-hidden="true"
// //                 >
// //                   <use href="/icons.svg#bluesky-icon"></use>
// //                 </svg>
// //                 Bluesky
// //               </a>
// //             </li>
// //           </ul>
// //         </div>
// //       </section>

// //       <div className="ticks"></div>
// //       <section id="spacer"></section>
// //     </>
// //   )
// // }

// // export default App


