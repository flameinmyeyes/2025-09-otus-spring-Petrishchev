import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  CircularProgress,
  IconButton,
  Avatar,
  Dialog,
  DialogTitle,
  DialogContent,
  TextField,
  DialogActions,
  Button,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import MoreVertIcon from '@mui/icons-material/MoreVert';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import SettingsIcon from '@mui/icons-material/Settings';
import EventIcon from '@mui/icons-material/Event';
import PollIcon from '@mui/icons-material/Poll';
import { useNavigate } from 'react-router-dom';
import { MessageList } from './MessageList';
import { MessageInput } from './MessageInput';
import { MessageContextMenu } from './MessageContextMenu';
import { AddMemberDialog } from './AddMemberDialog';
import { UnionSettings } from '../Unions/UnionSettings';
import { EventListDialog } from '../Events/EventListDialog';
import { PollListDialog } from '../Polls/PollListDialog';
import { useWebSocket } from '../../hooks/useWebSocket';
import { messageService } from '../../services/message.service';
import { pollService } from '../../services/poll.service';
import { Message, Union, Poll } from '../../types';
import { useAuth } from '../../hooks/useAuth';

interface ChatRoomProps {
  chatId: number;
  chatName: string;
  chatType: 'union' | 'friend';
}

export const ChatRoom: React.FC<ChatRoomProps> = ({ chatId, chatName, chatType }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(true);
  const [union, setUnion] = useState<Union | null>(null);
  const [editMessage, setEditMessage] = useState<Message | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editContent, setEditContent] = useState('');
  const [contextMenu, setContextMenu] = useState<{
    anchorEl: HTMLElement | null;
    message: Message | null;
  }>({ anchorEl: null, message: null });
  const [addMemberOpen, setAddMemberOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [eventsDialogOpen, setEventsDialogOpen] = useState(false);
  const [pollsDialogOpen, setPollsDialogOpen] = useState(false);
  const [menuAnchorEl, setMenuAnchorEl] = useState<null | HTMLElement>(null);
  const [pollMessages, setPollMessages] = useState<Poll[]>([]);
  const { messages: liveMessages, sendMessage, connected } = useWebSocket(chatId, chatType);
  const { user } = useAuth();
  const navigate = useNavigate();
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const loadPollsAsMessages = async () => {
    if (chatType !== 'union') return;

    try {
      const polls = await pollService.getPollsByUnion(chatId);
      setPollMessages(polls);
    } catch (error) {
      console.error('Failed to load polls:', error);
    }
  };

  useEffect(() => {
    loadMessages();
    if (chatType === 'union') {
      loadUnion();
      loadPollsAsMessages();
    } else {
      setUnion(null);
      setPollMessages([]);
    }
  }, [chatId, chatType]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, pollMessages, liveMessages]);

  const loadUnion = async () => {
    try {
      const { unionService } = await import('../../services/union.service');
      const data = await unionService.getUnionById(chatId);
      setUnion(data);
    } catch (error) {
      console.error('Failed to load union:', error);
    }
  };

  const loadMessages = async () => {
    setLoading(true);
    try {
      let history: Message[];
      if (chatType === 'union') {
        history = await messageService.getUnionMessages(chatId);
      } else {
        history = await messageService.getPrivateMessages(chatId);
      }
      setMessages(history);
    } catch (error) {
      console.error('Failed to load messages:', error);
      setMessages([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSendMessage = async (content: string) => {
    if (!content.trim()) return;

    const messageDto = chatType === 'union'
      ? { unionId: chatId, content }
      : { receiverId: chatId, content };

    try {
      if (connected) {
        sendMessage(messageDto);
      } else {
        const saved = await messageService.sendMessage(messageDto);
        setMessages((prev) => {
          if (prev.some((message) => message.id === saved.id)) {
            return prev;
          }
          return [...prev, saved];
        });
      }
    } catch (error) {
      console.error('Failed to send message:', error);
    }
  };

  const handleEditMessage = async () => {
    if (!editMessage || !editContent.trim()) return;

    try {
      const updated = await messageService.editMessage(editMessage.id, editContent);
      setMessages(prev => prev.map(m => m.id === updated.id ? updated : m));
      setEditDialogOpen(false);
      setEditMessage(null);
    } catch (error) {
      console.error('Failed to edit message:', error);
    }
  };

  const handleDeleteMessage = async (messageId: number) => {
    try {
      await messageService.deleteMessage(messageId);
      setMessages(prev => prev.filter(m => m.id !== messageId));
    } catch (error) {
      console.error('Failed to delete message:', error);
    }
  };

  const handleContextMenu = (event: React.MouseEvent, message: Message) => {
    event.preventDefault();
    setContextMenu({
      anchorEl: event.currentTarget as HTMLElement,
      message,
    });
  };

  const handleEditClick = (message: Message) => {
    setEditMessage(message);
    setEditContent(message.content);
    setEditDialogOpen(true);
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setMenuAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setMenuAnchorEl(null);
  };

  const handleOpenEvents = () => {
    setEventsDialogOpen(true);
    handleMenuClose();
  };

  const handleOpenPolls = () => {
    setPollsDialogOpen(true);
    handleMenuClose();
  };

  const allMessages = useMemo(() => {
    const merged = new Map<number, Message | Poll>();

    [...messages, ...liveMessages].forEach((message) => {
      merged.set(message.id, message);
    });

    pollMessages.forEach((poll) => {
      merged.set(poll.id, poll);
    });

    return Array.from(merged.values()).sort((a, b) => {
      const timeA = 'timestamp' in a ? new Date(a.timestamp) : new Date(a.createdAt);
      const timeB = 'timestamp' in b ? new Date(b.timestamp) : new Date(b.createdAt);
      return timeA.getTime() - timeB.getTime();
    });
  }, [messages, liveMessages, pollMessages]);

  const isCreator = union?.creator?.id === user?.id;

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', bgcolor: 'background.paper' }}>
      <Paper
        elevation={1}
        sx={{
          p: 2,
          borderRadius: 0,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          flexShrink: 0,
        }}
      >
        <Box display="flex" alignItems="center" gap={2}>
          <IconButton onClick={() => navigate('/chats')} sx={{ display: { md: 'none' } }}>
            <ArrowBackIcon />
          </IconButton>
          <Avatar sx={{ bgcolor: chatType === 'union' ? 'primary.main' : 'secondary.main' }}>
            {chatName[0].toUpperCase()}
          </Avatar>
          <Box>
            <Typography variant="h6">{chatName}</Typography>
            <Typography variant="caption" color={connected ? 'success.main' : 'error.main'}>
              {connected ? '● Онлайн' : '● Офлайн'}
            </Typography>
          </Box>
        </Box>
        <Box display="flex" alignItems="center">
          {chatType === 'union' && isCreator && (
            <>
              <IconButton onClick={() => setAddMemberOpen(true)}>
                <PersonAddIcon />
              </IconButton>
              <IconButton onClick={() => setSettingsOpen(true)}>
                <SettingsIcon />
              </IconButton>
            </>
          )}
          <IconButton onClick={handleMenuOpen}>
            <MoreVertIcon />
          </IconButton>
        </Box>
      </Paper>

      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
        <Box sx={{ flex: 1, overflow: 'auto', p: 2 }}>
          <MessageList
            messages={allMessages}
            onContextMenu={handleContextMenu}
            onVoteComplete={loadPollsAsMessages}
          />
          <div ref={messagesEndRef} />
        </Box>

        <MessageInput onSendMessage={handleSendMessage} disabled={false} />
      </Box>

      <MessageContextMenu
        anchorEl={contextMenu.anchorEl}
        message={contextMenu.message}
        isOwn={contextMenu.message?.sender.id === user?.id}
        onClose={() => setContextMenu({ anchorEl: null, message: null })}
        onEdit={handleEditClick}
        onDelete={handleDeleteMessage}
      />

      <Menu
        anchorEl={menuAnchorEl}
        open={Boolean(menuAnchorEl)}
        onClose={handleMenuClose}
      >
        <MenuItem onClick={handleOpenEvents}>
          <ListItemIcon>
            <EventIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>События профсоюза</ListItemText>
        </MenuItem>
        <MenuItem onClick={handleOpenPolls}>
          <ListItemIcon>
            <PollIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Все голосования</ListItemText>
        </MenuItem>
      </Menu>

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)}>
        <DialogTitle>Редактировать сообщение</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            multiline
            value={editContent}
            onChange={(e) => setEditContent(e.target.value)}
            margin="normal"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Отмена</Button>
          <Button onClick={handleEditMessage} variant="contained">Сохранить</Button>
        </DialogActions>
      </Dialog>

      {chatType === 'union' && union && (
        <AddMemberDialog
          open={addMemberOpen}
          unionId={chatId}
          onClose={() => setAddMemberOpen(false)}
          onSuccess={loadUnion}
        />
      )}

      {chatType === 'union' && union && (
        <UnionSettings
          open={settingsOpen}
          union={union}
          onClose={() => setSettingsOpen(false)}
          onUpdate={() => {
            loadUnion();
            window.location.reload();
          }}
        />
      )}

      {chatType === 'union' && union && (
        <EventListDialog
          open={eventsDialogOpen}
          unionId={chatId}
          unionName={union.name}
          onClose={() => setEventsDialogOpen(false)}
        />
      )}

      {chatType === 'union' && union && (
        <PollListDialog
          open={pollsDialogOpen}
          unionId={chatId}
          unionName={union.name}
          onClose={() => setPollsDialogOpen(false)}
        />
      )}
    </Box>
  );
};
