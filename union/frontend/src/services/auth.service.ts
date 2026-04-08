import api from './api';

export const authService = {
  register: async (phoneNumber: string, password: string): Promise<string> => {
    const response = await api.post('/auth/register', { phoneNumber, password });
    return response.data;
  },

  login: async (phoneNumber: string, password: string): Promise<string> => {
    const response = await api.post('/auth/login', { phoneNumber, password });
    const token = response.data.token;
    localStorage.setItem('token', token);
    return token;
  },

  logout: () => {
    localStorage.removeItem('token');
  },

  getToken: (): string | null => {
    return localStorage.getItem('token');
  },
};