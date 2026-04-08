import React, { useEffect, useState } from 'react';
import { Box, Typography, CircularProgress } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import { ChatRoom } from './ChatRoom';
import { unionService } from '../../services/union.service';
import { userService } from '../../services/user.service';

export const ChatView: React.FC = () => {
  const { unionId, friendId } = useParams<{ unionId?: string; friendId?: string }>();
  const [chatInfo, setChatInfo] = useState<{ id: number; name: string; type: 'union' | 'friend' } | null>(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    if (unionId) {
      loadUnionChat();
    } else if (friendId) {
      loadPrivateChat();
    } else {
      setLoading(false);
    }
  }, [unionId, friendId]);

  const loadUnionChat = async () => {
    if (!unionId) return;
    try {
      const data = await unionService.getUnionById(parseInt(unionId));
      setChatInfo({
        id: data.id,
        name: data.name,
        type: 'union'
      });
    } catch (error) {
      console.error('Failed to load union:', error);
      navigate('/chats');
    } finally {
      setLoading(false);
    }
  };

  const loadPrivateChat = async () => {
    if (!friendId) return;
    try {
      const data = await userService.getUserById(parseInt(friendId));
      setChatInfo({
        id: data.id,
        name: data.fullName || data.phoneNumber,
        type: 'friend'
      });
    } catch (error) {
      console.error('Failed to load friend:', error);
      navigate('/chats');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <CircularProgress />
      </Box>
    );
  }

  if (!chatInfo) {
    return (
      <Box
        display="flex"
        flexDirection="column"
        justifyContent="center"
        alignItems="center"
        height="100%"
        sx={{ bgcolor: 'background.paper' }}
      >
        <Typography variant="h5" color="text.secondary" gutterBottom>
          Добро пожаловать в Мессенджер Union
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Выберите чат из списка слева или создайте новый профсоюз
        </Typography>
      </Box>
    );
  }

  return (
    <ChatRoom
      chatId={chatInfo.id}
      chatName={chatInfo.name}
      chatType={chatInfo.type}
    />
  );
};