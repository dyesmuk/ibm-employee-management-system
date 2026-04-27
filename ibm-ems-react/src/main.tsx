import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

// import { StrictMode } from 'react'
// import { createRoot } from 'react-dom/client'
// import './index.css'
// import App from './App.tsx'
// import { BrowserRouter } from 'react-router-dom';
// import { AuthProvider } from './providers/AuthProvider.tsx';

// createRoot(document.getElementById("root")!).render(
//   <BrowserRouter>
//     <StrictMode>
//       <AuthProvider>
//         <App />
//       </AuthProvider>
//     </StrictMode>
//   </BrowserRouter>
// );

// // import { StrictMode } from 'react'
// // import { createRoot } from 'react-dom/client'
// // import './index.css'
// // import App from './App.tsx'

// // createRoot(document.getElementById('root')!).render(
// //   <StrictMode>
// //     <App />
// //   </StrictMode>,
// // )
