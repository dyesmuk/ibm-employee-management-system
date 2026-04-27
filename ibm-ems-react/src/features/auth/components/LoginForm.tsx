import { useState } from 'react';

interface LoginFormProps {
  onSubmit: (username: string, password: string) => Promise<void>;
  isLoading?: boolean;
  error?: string;
}

export const LoginForm = ({ onSubmit, isLoading = false, error }: LoginFormProps) => {
  const [username, setUsername] = useState('hr.admin');
  const [password, setPassword] = useState('Admin@IBM123');

  const handleSubmit = async (e: any) => {
    e.preventDefault();
    await onSubmit(username, password);
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Employee Management System</h2>

      <div>
        <label htmlFor="username">Username</label>
        <input
          id="username"
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          placeholder="Enter username"
          disabled={isLoading}
          required
        />
      </div>

      <div>
        <label htmlFor="password">Password</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Enter password"
          disabled={isLoading}
          required
        />
      </div>

      {error && <div>{error}</div>}

      <button type="submit" disabled={isLoading}>
        {isLoading ? 'Logging in...' : 'Login'}
      </button>

      <div>
        <p>Demo Credentials:</p>
        <small>Admin: hr.admin / Admin@IBM123</small>
        <small>User: emp.user / User@IBM123</small>
      </div>
    </form>
  );
};