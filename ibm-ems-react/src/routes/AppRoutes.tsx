import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from '../features/auth/context/AuthContext';
import PrivateRoute from './PrivateRoute';
import LoginPage from '../features/auth/pages/LoginPage';
import EmployeeDetailPage from '../features/employees/pages/EmployeeDetailPage';
import EmployeeFormPage from '../features/employees/pages/EmployeeFormPage';
import EmployeeSearchPage from '../features/employees/pages/EmployeeSearchPage';
import EmployeesPage from '../features/employees/pages/EmployeesPage';
import Navbar from '../shared/layout/Navbar';
import About from '../features/other/pages/About';
import Home from '../features/other/pages/Home';
import Page404 from '../features/other/pages/Page404';

const AppRoutes = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Navbar />
        <Routes>
          <Route path="/home" element={<Home />} />
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="/about" element={<About />} />
          <Route path="/employees" element={<EmployeesPage />} />
          <Route path="/employees/new" element={<EmployeeFormPage />} />
          <Route path="/employees/:id" element={<EmployeeDetailPage />} />
          <Route path="/employees/:id/edit" element={<EmployeeFormPage />} />
          <Route path="/employees/search" element={<EmployeeSearchPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/*" element={<Page404 />} />

        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default AppRoutes;




