
import { describe, it, expect } from 'vitest';

const BASE_URL = 'http://localhost:8080/api/v1';

async function login(username: string, password: string) {
  return fetch(`${BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ username, password })
  });
}

describe('Auth Integration', () => {

  it('should login with correct credentials', async () => {
    const res = await login('hr.admin', 'Admin@IBM123');
    const body = await res.json();
    expect(res.status).toBe(200);
    expect(body.token).toBeDefined();
  });

  it('should fail with bad credentials', async () => {
    const res = await login('hr.admin', 'wrongPassword');
    expect(res.status).toBe(401);
  });

});