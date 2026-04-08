import api from './api';
import { Event, EventCreateDto, EventUpdateDto } from '../types';

export const eventService = {
  createEvent: async (unionId: number, data: EventCreateDto): Promise<Event> => {
    const response = await api.post(`/events/createEvent/${unionId}`, data);
    return response.data;
  },

  updateEvent: async (eventId: number, data: EventUpdateDto): Promise<Event> => {
    const response = await api.put(`/events/updateEvent/${eventId}`, data);
    return response.data;
  },

  deleteEvent: async (eventId: number): Promise<void> => {
    await api.delete(`/events/deleteEvent/${eventId}`);
  },

  getEventsByUnion: async (unionId: number): Promise<Event[]> => {
    const response = await api.get(`/events/listEvents/${unionId}`);
    return response.data;
  },
};
