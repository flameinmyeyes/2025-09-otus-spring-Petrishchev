import React, { useState } from 'react';
import {
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Typography,
  Divider,
  Box,
  ListItemIcon,
} from '@mui/material';
import { Logout, Person, Settings } from '@mui/icons-material';
import { useAuth } from '../../hooks/useAuth';
import { useNavigate } from 'react-router-dom';
import { ProfileEdit } from '../Profile/ProfileEdit';

interface UserMenuProps {
  onProfileUpdate?: () => void;
}

export const UserMenu: React.FC<UserMenuProps> = ({ onProfileUpdate }) => {
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [profileOpen, setProfileOpen] = useState(false);
  const { user, logout, updateUser } = useAuth();
  const navigate = useNavigate();

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
    handleClose();
  };

  const handleProfile = () => {
    setProfileOpen(true);
    handleClose();
  };

  const handleProfileUpdate = (updatedUser: any) => {
    updateUser(updatedUser);
    if (onProfileUpdate) onProfileUpdate();
  };

  const getInitials = () => {
    if (user?.fullName) {
      return user.fullName[0].toUpperCase();
    }
    return user?.phoneNumber?.[1]?.toUpperCase() || 'U';
  };

  return (
    <>
      <IconButton onClick={handleMenu} color="inherit" size="small">
        <Avatar sx={{ width: 32, height: 32, bgcolor: 'secondary.main' }}>
          {getInitials()}
        </Avatar>
      </IconButton>
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
        transformOrigin={{ horizontal: 'right', vertical: 'top' }}
        anchorOrigin={{ horizontal: 'right', vertical: 'bottom' }}
      >
        <Box sx={{ px: 2, py: 1 }}>
          <Typography variant="subtitle1" fontWeight="bold">
            {user?.fullName || user?.phoneNumber}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {user?.phoneNumber}
          </Typography>
          {user?.status && (
            <Typography variant="caption" display="block" color="text.secondary">
              {user.status}
            </Typography>
          )}
        </Box>
        <Divider />
        <MenuItem onClick={handleProfile}>
          <ListItemIcon>
            <Person fontSize="small" />
          </ListItemIcon>
          <Typography>Редактировать профиль</Typography>
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <Logout fontSize="small" />
          </ListItemIcon>
          <Typography>Выйти</Typography>
        </MenuItem>
      </Menu>

      {user && (
        <ProfileEdit
          open={profileOpen}
          user={user}
          onClose={() => setProfileOpen(false)}
          onUpdate={handleProfileUpdate}
        />
      )}
    </>
  );
};