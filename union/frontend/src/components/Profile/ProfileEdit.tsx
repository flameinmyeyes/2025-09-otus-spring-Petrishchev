import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  Typography,
} from '@mui/material';
import { userService } from '../../services/user.service';
import { User } from '../../types';
import { useSnackbar } from 'notistack';

interface ProfileEditProps {
  open: boolean;
  user: User;
  onClose: () => void;
  onUpdate: (user: User) => void;
}

export const ProfileEdit: React.FC<ProfileEditProps> = ({ open, user, onClose, onUpdate }) => {
  const [fullName, setFullName] = useState(user.fullName || '');
  const [status, setStatus] = useState(user.status || '');
  const [loading, setLoading] = useState(false);
  const { enqueueSnackbar } = useSnackbar();

  const handleSubmit = async () => {
    setLoading(true);
    try {
      const updated = await userService.updateProfile({ fullName, status });
      onUpdate(updated);
      enqueueSnackbar('Профиль обновлен', { variant: 'success' });
      onClose();
    } catch (error) {
      enqueueSnackbar('Ошибка обновления', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Редактировать профиль</DialogTitle>
      <DialogContent>
        <Box sx={{ mt: 2 }}>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Телефон: {user.phoneNumber}
          </Typography>
          <TextField
            fullWidth
            label="Имя"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            margin="normal"
          />
          <TextField
            fullWidth
            label="Статус"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
            margin="normal"
            placeholder="На работе, в отпуске и т.д."
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={loading}>
          Сохранить
        </Button>
      </DialogActions>
    </Dialog>
  );
};