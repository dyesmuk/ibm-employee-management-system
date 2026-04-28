import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../auth/context/AuthContext';
import { employeeService } from '../employeeService';
import { type Employee } from '../employee.types';

const EmployeesPage = () => {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    loadEmployees();
  }, [page]);

  const loadEmployees = async () => {
    setLoading(true);
    try {
      const response = await employeeService.getAll(page, 10);
      setEmployees(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err: any) {
      setError('Failed to load employees');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (window.confirm('Are you sure you want to delete this employee?')) {
      try {
        await employeeService.delete(id);
        loadEmployees();
      } catch (err) {
        setError('Failed to delete employee');
      }
    }
  };

  if (loading) return <div>Loading employees...</div>;

  return (
    <div>
      <div>
        <h2>Employees</h2>
        {isAdmin && (
          <button onClick={() => navigate('/employees/new')}>
            Add Employee
          </button>
        )}
      </div>

      {error && <div>{error}</div>}

      <div>
        <input
          type="text"
          placeholder="Search by name..."
          onChange={async (e) => {
            if (e.target.value.length > 2) {
              const response = await employeeService.search(e.target.value);
              setEmployees(response.data);
            } else if (e.target.value.length === 0) {
              loadEmployees();
            }
          }}
        />
      </div>

      <div>
        <table border={1}>
          <thead>
            <tr>
              <th>Name</th>
            </tr>
          </thead>
          <tbody>
            {employees.map((emp) => (
              <tr key={emp.id}>
                <td>{emp.fullName}</td>
                <td>
                  <button onClick={() => navigate(`/employees/${emp.id}`)}>
                    View
                  </button>
                  {isAdmin && (
                    <>
                      <button onClick={() => navigate(`/employees/${emp.id}/edit`)}>
                        Edit
                      </button>
                      <button onClick={() => handleDelete(emp.id)}>
                        Delete
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div>
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </button>
          <span>Page {page + 1} of {totalPages}</span>
          <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)}>
            Next
          </button>
        </div>
      )}
    </div>
  );
};

export default EmployeesPage;
