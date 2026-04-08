import React, { useEffect, useState } from 'react';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemAvatar,
  Avatar,
  ListItemText,
  Typography,
  Divider,
  CircularProgress,
  Badge,
  TextField,
  InputAdornment,
  IconButton,
  Menu,
  MenuItem,
  ListItemIcon,
} from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import SearchIcon from '@mui/icons-material/Search';
import GroupIcon from '@mui/icons-material/Group';
import PersonIcon from '@mui/icons-material/Person';
import AddIcon from '@mui/icons-material/Add';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import { unionService } from '../../services/union.service';
import { userService } from '../../services/user.service';
import { Union, User } from '../../types';

interface ChatItem {
  id: number;
  type: 'union' | 'friend';
  name: string;
  avatar: string;
  subtitle: string;
  membersCount?: number;
  status?: string;
  phoneNumber?: string;
}

interface ChatListProps {
  onChatSelect?: () => void;
  refreshTrigger?: number;
}

export const ChatList: React.FC<ChatListProps> = ({ onChatSelect, refreshTrigger }) => {
  const [unions, setUnions] = useState<Union[]>([]);
  const [friends, setFriends] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    loadData();
  }, [refreshTrigger]);

  const loadData = async () => {
    setLoading(true);
    try {
      const [unionsData, friendsData] = await Promise.all([
        unionService.getUserUnions(),
        userService.getFriends()
      ]);
      setUnions(unionsData || []);
      setFriends(friendsData || []);
    } catch (error) {
      console.error('Failed to load data:', error);
      setUnions([]);
      setFriends([]);
    } finally {
      setLoading(false);
    }
  };

  // Объединяем профсоюзы и друзей в один список с проверкой на существование
  const chatItems: ChatItem[] = [
    ...(unions || []).map(union => ({
      id: union.id,
      type: 'union' as const,
      name: union.name,
      avatar: 'group',
      subtitle: `${union.members?.length || 0} участников`,
      membersCount: union.members?.length || 0,
    })),
    ...(friends || []).map(friend => ({
      id: friend.id,
      type: 'friend' as const,
      name: friend.fullName || friend.phoneNumber,
      avatar: 'person',
      subtitle: friend.status || 'Онлайн',
      phoneNumber: friend.phoneNumber,
      status: friend.status,
    }))
  ];

  // Фильтрация по поиску
  const filteredItems = chatItems.filter(item =>
    item.name?.toLowerCase().includes(searchQuery.toLowerCase()) || false
  );

  const handleItemClick = (item: ChatItem) => {
    if (item.type === 'union') {
      navigate(`/chats/union/${item.id}`);
    } else {
      navigate(`/chats/private/${item.id}`);
    }
    if (onChatSelect) onChatSelect();
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleCreateUnion = () => {
    navigate('/unions/create');
    handleMenuClose();
    if (onChatSelect) onChatSelect();
  };

  const handleAddFriend = () => {
    navigate('/friends');
    handleMenuClose();
    if (onChatSelect) onChatSelect();
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="100%">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
      {/* Поиск и кнопка добавления */}
      <Box sx={{ p: 2, display: 'flex', gap: 1, alignItems: 'center' }}>
        <TextField
          fullWidth
          size="small"
          placeholder="Поиск чатов и друзей..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          sx={{ flex: 1 }}
          InputProps={{
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
          }}
        />
        <IconButton
          onClick={handleMenuOpen}
          sx={{
            bgcolor: 'primary.main',
            color: 'white',
            '&:hover': {
              bgcolor: 'primary.dark',
            },
            width: 40,
            height: 40,
          }}
        >
          <AddIcon />
        </IconButton>
      </Box>

      <Divider />

      {/* Объединенный список чатов и друзей */}
      <List sx={{ flex: 1, overflow: 'auto' }}>
        {filteredItems.length === 0 ? (
          <Box sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              {searchQuery ? 'Ничего не найдено' : 'Нет чатов и друзей'}
            </Typography>
            {!searchQuery && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 1, display: 'block' }}>
                Нажмите [+] чтобы создать профсоюз или добавить друга
              </Typography>
            )}
          </Box>
        ) : (
          filteredItems.map((item) => {
            const isSelected =
              (item.type === 'union' && location.pathname === `/chats/union/${item.id}`) ||
              (item.type === 'friend' && location.pathname === `/chats/private/${item.id}`);

            return (
              <ListItem key={`${item.type}-${item.id}`} disablePadding>
                <ListItemButton
                  selected={isSelected}
                  onClick={() => handleItemClick(item)}
                  sx={{
                    borderRadius: 1,
                    mx: 1,
                    mb: 0.5,
                    '&.Mui-selected': {
                      bgcolor: 'primary.light',
                      '&:hover': { bgcolor: 'primary.light' },
                    },
                  }}
                >
                  <ListItemAvatar>
                    <Badge
                      color="success"
                      variant="dot"
                      overlap="circular"
                      anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
                    >
                      <Avatar sx={{ bgcolor: item.type === 'union' ? 'primary.main' : 'secondary.main' }}>
                        {item.type === 'union' ? <GroupIcon /> : <PersonIcon />}
                      </Avatar>
                    </Badge>
                  </ListItemAvatar>
                  <ListItemText
                    primary={item.name}
                    secondary={
                      <Typography variant="caption" color="text.secondary">
                        {item.subtitle}
                      </Typography>
                    }
                    primaryTypographyProps={{
                      fontWeight: isSelected ? 600 : 400,
                    }}
                  />
                </ListItemButton>
              </ListItem>
            );
          })
        )}
      </List>

      {/* Меню выбора действия */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleMenuClose}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <MenuItem onClick={handleCreateUnion}>
          <ListItemIcon>
            <GroupIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Создать профсоюз</ListItemText>
        </MenuItem>
        <MenuItem onClick={handleAddFriend}>
          <ListItemIcon>
            <PersonAddIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Добавить друга</ListItemText>
        </MenuItem>
      </Menu>
    </Box>
  );
};