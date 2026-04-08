import api from './api';
import { Message } from '../types';

export const messageService = {
  getUnionMessages: async (unionId: number): Promise<Message[]> => {
    try {
      const response = await api.get(`/messages/listMessageByUnion/${unionId}`);
      return response.data || [];
    } catch (error) {
      console.error('Failed to get union messages:', error);
      return [];
    }
  },

  getPrivateMessages: async (friendId: number): Promise<Message[]> => {
    try {
      const response = await api.get(`/messages/private/list/${friendId}`);
      return response.data || [];
    } catch (error) {
      console.error('Failed to get private messages:', error);
      return [];
    }
  },

  sendMessage: async (data: { unionId?: number; receiverId?: number; content: string }): Promise<Message> => {
    const response = await api.post('/messages/send', data);
    return response.data;
  },

  deleteMessage: async (messageId: number): Promise<void> => {
    await api.delete(`/messages/delete/${messageId}`);
  },

  editMessage: async (messageId: number, content: string): Promise<Message> => {
    const response = await api.put(`/messages/edit/${messageId}`, { content });
    return response.data;
  },
};