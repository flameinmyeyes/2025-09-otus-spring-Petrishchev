import React, { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  CircularProgress,
  Box,
  Typography,
} from '@mui/material';
import { userService } from '../../services/user.service';
import { unionService } from '../../services/union.service';
import { User } from '../../types';
import { useSnackbar } from 'notistack';

interface AddMemberDialogProps {
  open: boolean;
  unionId: number;
  onClose: () => void;
  onSuccess: () => void;
}

export const AddMemberDialog: React.FC<AddMemberDialogProps> = ({
  open,
  unionId,
  onClose,
  onSuccess,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<User[]>([]);
  const [searching, setSearching] = useState(false);
  const { enqueueSnackbar } = useSnackbar();

  const handleSearch = async () => {
    if (!searchQuery.trim()) return;
    setSearching(true);
    try {
      const users = await userService.searchUsers(searchQuery);
      setResults(users);
    } catch (error) {
      enqueueSnackbar('Ошибка поиска', { variant: 'error' });
    } finally {
      setSearching(false);
    }
  };

  const handleAddMember = async (userId: number) => {
    try {
      await unionService.addMember(unionId, userId);
      enqueueSnackbar('Участник добавлен', { variant: 'success' });
      onSuccess();
      onClose();
    } catch (error) {
      enqueueSnackbar('Ошибка добавления', { variant: 'error' });
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Добавить участника</DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', gap: 1, mt: 1 }}>
          <TextField
            fullWidth
            placeholder="Поиск по номеру телефона или имени"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
          />
          <Button onClick={handleSearch} variant="contained" disabled={searching}>
            Поиск
          </Button>
        </Box>

        {searching && (
          <Box display="flex" justifyContent="center" sx={{ mt: 2 }}>
            <CircularProgress />
          </Box>
        )}

        {results.length > 0 && (
          <List sx={{ mt: 2 }}>
            {results.map((user) => (
              <ListItem key={user.id}>
                <ListItemText
                  primary={user.fullName || user.phoneNumber}
                  secondary={user.status}
                />
                <Button onClick={() => handleAddMember(user.id)} variant="outlined" size="small">
                  Добавить
                </Button>
              </ListItem>
            ))}
          </List>
        )}

        {results.length === 0 && searchQuery && !searching && (
          <Typography sx={{ mt: 2, textAlign: 'center' }} color="text.secondary">
            Пользователи не найдены
          </Typography>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Закрыть</Button>
      </DialogActions>
    </Dialog>
  );
};