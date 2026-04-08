export interface User {
  id: number;
  phoneNumber: string;
  fullName?: string;
  status?: string;
}

export interface Union {
  id: number;
  name: string;
  description?: string;
  creator: User;
  members: User[];
}

export interface Message {
  id: number;
  content: string;
  sender: User;
  unionId?: number;
  receiverId?: number;
  timestamp: string;
}

export interface Poll {
  id: number;
  question: string;
  createdBy: User;
  createdAt: string;
  expiresAt: string | null;
  status: 'ACTIVE' | 'EXPIRED';
  unionId: number;
  hasUserVoted: boolean;
  options: PollOption[];
}

export interface PollOption {
  id: number;
  text: string;
  voteCount: number;
  votePercentage: number;
  voted: boolean;
}

export interface Vote {
  id: number;
  user: User;
  option: PollOption;
}

export interface Event {
  id: number;
  title: string;
  description?: string;
  eventDate: string | null;
  location?: string;
  union: Union;
  createdBy: User;
}

export interface AuthRequest {
  phoneNumber: string;
  password: string;
}

export interface AuthResponse {
  token: string;
}

export interface MessageDto {
  unionId?: number;
  receiverId?: number;
  content: string;
}

export interface PollCreateDto {
  question: string;
  options: string[];
}

export interface PollUpdateDto {
  question: string;
  options: string[];
}

export interface EventCreateDto {
  title: string;
  description?: string;
  location?: string;
}

export interface EventUpdateDto {
  title?: string;
  description?: string;
  eventDate?: string | null;
  location?: string;
}

export interface PollResponseDto extends Poll {
}

export interface PollDeleteResponseDto {
  message: string;
  pollId: number;
  status: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path?: string;
  validationErrors?: Record<string, string>;
  errorCode?: string;
}

export type ChatMessage = Message | Poll;
