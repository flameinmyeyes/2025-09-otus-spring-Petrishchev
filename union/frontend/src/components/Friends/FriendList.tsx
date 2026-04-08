import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  TextField,
  DialogActions,
  Box,
  CircularProgress,
  IconButton,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { userService } from '../../services/user.service';
import { User } from '../../types';
import { useSnackbar } from 'notistack';
import { useNavigate } from 'react-router-dom';

interface FriendListProps {
  onFriendAdded?: () => void;
}

export const FriendList: React.FC<FriendListProps> = ({ onFriendAdded }) => {
  const [friends, setFriends] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [addDialogOpen, setAddDialogOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<User[]>([]);
  const [searching, setSearching] = useState(false);
  const { enqueueSnackbar } = useSnackbar();
  const navigate = useNavigate();

  useEffect(() => {
    loadFriends();
  }, []);

  const loadFriends = async () => {
    try {
      const data = await userService.getFriends();
      setFriends(data);
    } catch (error) {
      console.error('Failed to load friends:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;

    setSearching(true);
    try {
      const results = await userService.searchUsers(searchQuery);
      const filteredResults = results.filter(
        user => !friends.some(friend => friend.id === user.id)
      );
      setSearchResults(filteredResults);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setSearching(false);
    }
  };

  const handleAddFriend = async (friendId: number) => {
    try {
      await userService.addFriend(friendId);
      enqueueSnackbar('Друг добавлен!', { variant: 'success' });
      await loadFriends();
      setAddDialogOpen(false);
      setSearchQuery('');
      setSearchResults([]);
      // Уведомляем родительский компонент об обновлении
      if (onFriendAdded) onFriendAdded();
    } catch (error: any) {
      enqueueSnackbar(error.response?.data || 'Ошибка добавления', { variant: 'error' });
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="md">
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 2 }}>
        <IconButton onClick={() => navigate('/chats')}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" sx={{ flex: 1 }}>
          Мои друзья
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setAddDialogOpen(true)}
        >
          Добавить друга
        </Button>
      </Box>

      <Paper elevation={3}>
        <List>
          {friends.length === 0 ? (
            <ListItem>
              <ListItemText primary="У вас пока нет друзей" />
            </ListItem>
          ) : (
            friends.map((friend) => (
              <ListItem key={friend.id}>
                <ListItemAvatar>
                  <Avatar>
                    {friend.fullName?.[0] || friend.phoneNumber[1]}
                  </Avatar>
                </ListItemAvatar>
                <ListItemText
                  primary={friend.fullName || friend.phoneNumber}
                  secondary={friend.status || 'Онлайн'}
                />
              </ListItem>
            ))
          )}
        </List>
      </Paper>

      <Dialog open={addDialogOpen} onClose={() => setAddDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Добавить друга</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
            <TextField
              fullWidth
              placeholder="Введите номер телефона или имя"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            />
            <Button
              variant="contained"
              onClick={handleSearch}
              disabled={searching}
            >
              Поиск
            </Button>
          </Box>

          {searching && (
            <Box display="flex" justifyContent="center" sx={{ mt: 2 }}>
              <CircularProgress />
            </Box>
          )}

          {searchResults.length > 0 && (
            <List sx={{ mt: 2 }}>
              {searchResults.map((user) => (
                <ListItem
                  key={user.id}
                  secondaryAction={
                    <Button
                      startIcon={<PersonAddIcon />}
                      onClick={() => handleAddFriend(user.id)}
                      size="small"
                    >
                      Добавить
                    </Button>
                  }
                >
                  <ListItemAvatar>
                    <Avatar>
                      {user.fullName?.[0] || user.phoneNumber[1]}
                    </Avatar>
                  </ListItemAvatar>
                  <ListItemText
                    primary={user.fullName || user.phoneNumber}
                    secondary={user.phoneNumber}
                  />
                </ListItem>
              ))}
            </List>
          )}

          {searchResults.length === 0 && searchQuery && !searching && (
            <Typography sx={{ mt: 2, textAlign: 'center' }} color="text.secondary">
              Пользователи не найдены
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddDialogOpen(false)}>Закрыть</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};