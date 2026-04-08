import api from './api';
import { User } from '../types';

export const userService = {
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get('/users/userInfo');
    return response.data;
  },

  getUserById: async (id: number): Promise<User> => {
    const response = await api.get(`/users/getUser/${id}`);
    return response.data;
  },

  addFriend: async (friendId: number): Promise<void> => {
    await api.post(`/users/addFriend/${friendId}`);
  },

  getFriends: async (): Promise<User[]> => {
    try {
      const response = await api.get('/users/listFriends');
      return response.data || [];
    } catch (error) {
      console.error('Failed to get friends:', error);
      return [];
    }
  },

  searchUsers: async (query: string): Promise<User[]> => {
    try {
      const response = await api.get('/users/searchUsers', { params: { query } });
      return response.data || [];
    } catch (error) {
      console.error('Failed to search users:', error);
      return [];
    }
  },

  updateProfile: async (data: { fullName?: string; status?: string }): Promise<User> => {
    const response = await api.put('/users/updateUserInfo', data);
    return response.data;
  },
};