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
  IconButton,
  Typography,
  Box,
  Alert,
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import PersonRemoveIcon from '@mui/icons-material/PersonRemove';
import { unionService } from '../../services/union.service';
import { Union, User } from '../../types';
import { useSnackbar } from 'notistack';

interface UnionSettingsProps {
  open: boolean;
  union: Union;
  onClose: () => void;
  onUpdate: () => void;
}

export const UnionSettings: React.FC<UnionSettingsProps> = ({ open, union, onClose, onUpdate }) => {
  const [name, setName] = useState(union.name);
  const [description, setDescription] = useState(union.description || '');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { enqueueSnackbar } = useSnackbar();

  const handleUpdate = async () => {
    if (!name.trim()) {
      setError('Введите название');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await unionService.updateUnion(union.id, { name, description });
      enqueueSnackbar('Профсоюз обновлен', { variant: 'success' });
      onUpdate();
      onClose();
    } catch (error: any) {
      setError(error.response?.data || 'Ошибка обновления');
    } finally {
      setLoading(false);
    }
  };

  const handleRemoveMember = async (userId: number) => {
    try {
      await unionService.removeMember(union.id, userId);
      enqueueSnackbar('Участник удален', { variant: 'success' });
      onUpdate();
    } catch (error) {
      enqueueSnackbar('Ошибка удаления', { variant: 'error' });
    }
  };

  const handleDeleteUnion = async () => {
    if (window.confirm('Вы уверены, что хотите удалить профсоюз? Это действие необратимо.')) {
      try {
        await unionService.deleteUnion(union.id);
        enqueueSnackbar('Профсоюз удален', { variant: 'success' });
        onUpdate();
        onClose();
      } catch (error) {
        enqueueSnackbar('Ошибка удаления', { variant: 'error' });
      }
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Настройки профсоюза</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        <TextField
          fullWidth
          label="Название"
          value={name}
          onChange={(e) => setName(e.target.value)}
          margin="normal"
        />
        <TextField
          fullWidth
          label="Описание"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          margin="normal"
          multiline
          rows={3}
        />

        <Typography variant="h6" sx={{ mt: 3, mb: 2 }}>
          Участники ({union.members.length})
        </Typography>
        <List>
          {union.members.map((member) => (
            <ListItem
              key={member.id}
              secondaryAction={
                member.id !== union.creator.id && (
                  <IconButton edge="end" onClick={() => handleRemoveMember(member.id)}>
                    <PersonRemoveIcon />
                  </IconButton>
                )
              }
            >
              <ListItemText
                primary={member.fullName || member.phoneNumber}
                secondary={member.id === union.creator.id ? 'Создатель' : ''}
              />
            </ListItem>
          ))}
        </List>

        <Button
          variant="outlined"
          color="error"
          onClick={handleDeleteUnion}
          startIcon={<DeleteIcon />}
          sx={{ mt: 2 }}
        >
          Удалить профсоюз
        </Button>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button onClick={handleUpdate} variant="contained" disabled={loading}>
          Сохранить
        </Button>
      </DialogActions>
    </Dialog>
  );
};