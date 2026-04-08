import React from 'react';
import { Box, Typography } from '@mui/material';
import { Message, Poll } from '../../types';
import { useAuth } from '../../hooks/useAuth';
import { PollMessage } from './PollMessage';

interface MessageListProps {
  messages: (Message | Poll)[];
  onContextMenu?: (event: React.MouseEvent, message: Message) => void;
  onVoteComplete?: () => void;
}

export const MessageList: React.FC<MessageListProps> = ({
  messages,
  onContextMenu,
  onVoteComplete
}) => {
  const { user } = useAuth();

  const isOwnMessage = (senderId: number) => {
    return senderId === user?.id;
  };

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const isPoll = (msg: Message | Poll): msg is Poll => {
    return 'question' in msg && 'options' in msg;
  };

  return (
    <Box>
      {messages.map((msg, index) => {
        if (isPoll(msg)) {
          return (
            <PollMessage
              key={`poll-${msg.id}`}
              poll={msg}
              onVoteComplete={onVoteComplete}
            />
          );
        } else {
          const message = msg as Message;
          const isOwn = isOwnMessage(message.sender.id);

          return (
            <Box
              key={message.id}
              sx={{
                display: 'flex',
                justifyContent: isOwn ? 'flex-end' : 'flex-start',
                mb: 1,
              }}
              onContextMenu={(e) => onContextMenu?.(e, message)}
            >
              <Box
                sx={{
                  maxWidth: '70%',
                  bgcolor: isOwn ? 'primary.main' : 'grey.100',
                  color: isOwn ? 'white' : 'text.primary',
                  borderRadius: 2,
                  p: 1,
                  wordWrap: 'break-word',
                }}
              >
                {!isOwn && (
                  <Typography variant="caption" display="block" color="text.secondary">
                    {message.sender.fullName || message.sender.phoneNumber}
                  </Typography>
                )}
                <Typography variant="body2">{message.content}</Typography>
                <Typography variant="caption" display="block" textAlign="right" sx={{ opacity: 0.7 }}>
                  {formatTime(message.timestamp)}
                </Typography>
              </Box>
            </Box>
          );
        }
      })}
    </Box>
  );
};