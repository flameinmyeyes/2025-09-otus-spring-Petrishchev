import api from './api';
import { Union } from '../types';

export const unionService = {
  createUnion: async (data: { name: string; description?: string }): Promise<Union> => {
    const response = await api.post('/unions/createUnion', data);
    return response.data;
  },

  getUserUnions: async (): Promise<Union[]> => {
    try {
      const response = await api.get('/unions/myUnions');
      return response.data || [];
    } catch (error) {
      console.error('Failed to get unions:', error);
      return [];
    }
  },

  getUnionById: async (id: number): Promise<Union> => {
    const response = await api.get(`/unions/getUnionInfo/${id}`);
    return response.data;
  },

  addMember: async (unionId: number, userId: number): Promise<void> => {
    await api.post(`/unions/${unionId}/addUserToUnion/${userId}`);
  },

  removeMember: async (unionId: number, userId: number): Promise<void> => {
    await api.delete(`/unions/${unionId}/removeUserFromUnion/${userId}`);
  },

  updateUnion: async (id: number, data: { name?: string; description?: string }): Promise<Union> => {
    const response = await api.put(`/unions/updateUnionInfo/${id}`, data);
    return response.data;
  },

  deleteUnion: async (id: number): Promise<void> => {
    await api.delete(`/unions/deleteUnion/${id}`);
  },
};