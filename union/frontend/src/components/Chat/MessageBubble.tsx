import React from 'react';
import { Box, Paper, Typography, Avatar } from '@mui/material';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import { Message } from '../../types';

interface MessageBubbleProps {
  message: Message;
  isOwn: boolean;
  onContextMenu?: (event: React.MouseEvent, message: Message) => void;
}

export const MessageBubble: React.FC<MessageBubbleProps> = ({ message, isOwn, onContextMenu }) => {
  if (!message.sender) {
    return null;
  }

  const handleContextMenu = (event: React.MouseEvent) => {
    event.preventDefault();
    if (onContextMenu) {
      onContextMenu(event, message);
    }
  };

  const formatTime = (timestamp?: string) => {
    if (!timestamp) return '';
    try {
      const date = new Date(timestamp);
      if (isNaN(date.getTime())) return '';
      return format(date, 'HH:mm', { locale: ru });
    } catch (error) {
      console.error('Invalid timestamp:', timestamp);
      return '';
    }
  };

  return (
    <Box
      sx={{
        display: 'flex',
        justifyContent: isOwn ? 'flex-end' : 'flex-start',
        mb: 2,
      }}
      onContextMenu={handleContextMenu}
    >
      {!isOwn && (
        <Avatar sx={{ mr: 1, bgcolor: 'primary.main' }}>
          {message.sender.fullName?.[0] || message.sender.phoneNumber?.[1] || 'U'}
        </Avatar>
      )}
      <Box sx={{ maxWidth: '70%' }}>
        <Paper
          elevation={1}
          sx={{
            p: 1,
            bgcolor: isOwn ? 'primary.main' : 'grey.100',
            color: isOwn ? 'white' : 'text.primary',
          }}
        >
          {!isOwn && (
            <Typography variant="caption" display="block" color={isOwn ? 'white' : 'text.secondary'}>
              {message.sender.fullName || message.sender.phoneNumber}
            </Typography>
          )}
          <Typography variant="body1">{message.content}</Typography>
          <Typography variant="caption" display="block" textAlign="right" sx={{ mt: 0.5 }}>
            {formatTime(message.timestamp)}
          </Typography>
        </Paper>
      </Box>
    </Box>
  );
};