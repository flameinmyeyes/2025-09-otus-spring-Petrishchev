import React, { useState, useCallback } from 'react';
import { Box, Drawer, AppBar, Toolbar, Typography, IconButton, useTheme, useMediaQuery } from '@mui/material';
import MenuIcon from '@mui/icons-material/Menu';
import { ChatList } from '../Chat/ChatList';
import { UserMenu } from './UserMenu';
import { Outlet, useLocation } from 'react-router-dom';

const drawerWidth = 320;
const appBarHeight = 64;

export const MainLayout: React.FC = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const refreshChatList = useCallback(() => {
    setRefreshKey(prev => prev + 1);
  }, []);

  return (
    <Box sx={{ display: 'flex', height: '100vh' }}>
      <AppBar
        position="fixed"
        sx={{
          zIndex: (theme) => theme.zIndex.drawer + 1,
          height: appBarHeight,
        }}
      >
        <Toolbar sx={{ height: appBarHeight, minHeight: appBarHeight }}>
          <IconButton
            color="inherit"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Мессенджер Union
          </Typography>
          <UserMenu onProfileUpdate={refreshChatList} />
        </Toolbar>
      </AppBar>

      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant={isMobile ? "temporary" : "permanent"}
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: 'block', sm: 'block' },
            '& .MuiDrawer-paper': {
              width: drawerWidth,
              boxSizing: 'border-box',
              position: isMobile ? 'absolute' : 'relative',
              top: appBarHeight,
              height: `calc(100% - ${appBarHeight}px)`,
            },
          }}
        >
          <ChatList key={refreshKey} onChatSelect={handleDrawerToggle} />
        </Drawer>
      </Box>

      <Box
        component="main"
        sx={{
          flexGrow: 1,
          width: { sm: `calc(100% - ${drawerWidth}px)` },
          bgcolor: 'background.default',
          overflow: 'auto',
          mt: `${appBarHeight}px`,
          height: `calc(100vh - ${appBarHeight}px)`,
        }}
      >
        <Outlet />
      </Box>
    </Box>
  );
};