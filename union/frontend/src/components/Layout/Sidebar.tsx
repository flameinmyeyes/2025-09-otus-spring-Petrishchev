import React, { useEffect, useState } from 'react';
import {
  Box,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Typography,
  Button,
  Collapse,
} from '@mui/material';
import {
  Chat,
  Group,
  Poll,
  Event,
  People,
  Add,
  ExpandLess,
  ExpandMore,
} from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { unionService } from '../../services/union.service';
import { Union } from '../../types';

export const Sidebar: React.FC = () => {
  const [unions, setUnions] = useState<Union[]>([]);
  const [unionsOpen, setUnionsOpen] = useState(true);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    loadUnions();
  }, []);

  const loadUnions = async () => {
    try {
      const data = await unionService.getUserUnions();
      setUnions(data);
    } catch (error) {
      console.error('Failed to load unions:', error);
    }
  };

  const menuItems = [
    { text: 'Чаты', icon: <Chat />, path: '/chats' },
    { text: 'Голосования', icon: <Poll />, path: '/polls' },
    { text: 'События', icon: <Event />, path: '/events' },
    { text: 'Друзья', icon: <People />, path: '/friends' },
  ];

  return (
    <Box sx={{ overflow: 'auto', height: '100%' }}>
      <Box sx={{ p: 2 }}>
        <Button
          fullWidth
          variant="contained"
          startIcon={<Add />}
          onClick={() => navigate('/unions/create')}
        >
          Создать профсоюз
        </Button>
      </Box>

      <Divider />

      <List>
        {menuItems.map((item) => (
          <ListItem key={item.text} disablePadding>
            <ListItemButton
              selected={location.pathname === item.path}
              onClick={() => navigate(item.path)}
            >
              <ListItemIcon>{item.icon}</ListItemIcon>
              <ListItemText primary={item.text} />
            </ListItemButton>
          </ListItem>
        ))}
      </List>

      <Divider />

      <ListItemButton onClick={() => setUnionsOpen(!unionsOpen)}>
        <ListItemIcon>
          <Group />
        </ListItemIcon>
        <ListItemText primary="Мои профсоюзы" />
        {unionsOpen ? <ExpandLess /> : <ExpandMore />}
      </ListItemButton>

      <Collapse in={unionsOpen} timeout="auto" unmountOnExit>
        <List component="div" disablePadding>
          {unions.length === 0 ? (
            <ListItem sx={{ pl: 4 }}>
              <ListItemText
                primary="Нет профсоюзов"
                secondary="Создайте первый профсоюз"
                primaryTypographyProps={{ variant: 'body2' }}
                secondaryTypographyProps={{ variant: 'caption' }}
              />
            </ListItem>
          ) : (
            unions.map((union) => (
              <ListItem key={union.id} disablePadding>
                <ListItemButton
                  sx={{ pl: 4 }}
                  onClick={() => navigate(`/chats/${union.id}`)}
                  selected={location.pathname === `/chats/${union.id}`}
                >
                  <ListItemIcon>
                    <Group fontSize="small" />
                  </ListItemIcon>
                  <ListItemText
                    primary={union.name}
                    primaryTypographyProps={{ variant: 'body2' }}
                  />
                </ListItemButton>
              </ListItem>
            ))
          )}
        </List>
      </Collapse>
    </Box>
  );
};