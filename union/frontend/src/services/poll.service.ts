import api from './api';
import { PollCreateDto, PollDeleteResponseDto, PollResponseDto, PollUpdateDto } from '../types';

export const pollService = {
  createPoll: async (unionId: number, data: PollCreateDto): Promise<PollResponseDto> => {
    const response = await api.post(`/polls/createPoll/${unionId}`, data);
    return response.data;
  },

  updatePoll: async (pollId: number, data: PollUpdateDto): Promise<PollResponseDto> => {
    const response = await api.put(`/polls/updatePoll/${pollId}`, data);
    return response.data;
  },

  getPollsByUnion: async (unionId: number): Promise<PollResponseDto[]> => {
    const response = await api.get(`/polls/listPoll/${unionId}`);
    return response.data;
  },

  vote: async (pollId: number, optionId: number): Promise<void> => {
    await api.post(`/polls/vote/${pollId}/${optionId}`);
  },

  getPollResults: async (pollId: number): Promise<PollResponseDto> => {
    const response = await api.get(`/polls/voteResults/${pollId}`);
    return response.data;
  },

  deletePoll: async (pollId: number): Promise<PollDeleteResponseDto> => {
    const response = await api.delete(`/polls/deletePoll/${pollId}`);
    return response.data;
  },
};
