import { useState, useEffect } from 'react';
import { authService } from '../services/auth.service';
import { userService } from '../services/user.service';
import { User } from '../types';

export const useAuth = () => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = authService.getToken();
    if (token) {
      loadUser();
    } else {
      setLoading(false);
    }
  }, []);

  const loadUser = async () => {
    try {
      const userData = await userService.getCurrentUser();
      setUser(userData);
    } catch (error) {
      authService.logout();
    } finally {
      setLoading(false);
    }
  };

  const login = async (phoneNumber: string, password: string) => {
    await authService.login(phoneNumber, password);
    await loadUser();
  };

  const register = async (phoneNumber: string, password: string) => {
    await authService.register(phoneNumber, password);
    await login(phoneNumber, password);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const updateUser = (updatedUser: User) => {
    setUser(updatedUser);
  };

  return { user, loading, login, register, logout, updateUser };
};