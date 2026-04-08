import React, { useEffect, useMemo, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Alert,
} from '@mui/material';
import { eventService } from '../../services/event.service';
import { Event } from '../../types';
import { useSnackbar } from 'notistack';

interface EventCreateDialogProps {
  open: boolean;
  unionId: number;
  onClose: () => void;
  onSuccess: () => void;
  event?: Event | null;
}

const toLocalInputValue = (dateString?: string | null) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  if (Number.isNaN(date.getTime())) return '';
  const offset = date.getTimezoneOffset();
  return new Date(date.getTime() - offset * 60_000).toISOString().slice(0, 16);
};

const getMinDateTime = () => {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  return now.toISOString().slice(0, 16);
};

export const EventCreateDialog: React.FC<EventCreateDialogProps> = ({
  open,
  unionId,
  onClose,
  onSuccess,
  event,
}) => {
  const isEditMode = !!event;
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [eventDate, setEventDate] = useState('');
  const [location, setLocation] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    if (!open) return;

    if (event) {
      setTitle(event.title || '');
      setDescription(event.description || '');
      setLocation(event.location || '');
      setEventDate(toLocalInputValue(event.eventDate));
    } else {
      setTitle('');
      setDescription('');
      setLocation('');
      setEventDate('');
    }
    setError('');
  }, [open, event]);

  const dialogTitle = useMemo(
    () => (isEditMode ? 'Редактировать событие' : 'Создать событие'),
    [isEditMode]
  );

  const handleSubmit = async () => {
    setError('');

    if (!title.trim()) {
      setError('Введите название события');
      return;
    }

    if (isEditMode && eventDate) {
      const selectedDate = new Date(eventDate);
      if (Number.isNaN(selectedDate.getTime())) {
        setError('Выберите корректную дату и время');
        return;
      }
      if (selectedDate <= new Date()) {
        setError('Дата события должна быть в будущем');
        return;
      }
    }

    setLoading(true);
    try {
      if (event) {
        await eventService.updateEvent(event.id, {
          title: title.trim(),
          description: description.trim() || undefined,
          location: location.trim() || undefined,
          eventDate: eventDate ? new Date(eventDate).toISOString() : null,
        });
        enqueueSnackbar('Событие обновлено!', { variant: 'success' });
      } else {
        await eventService.createEvent(unionId, {
          title: title.trim(),
          description: description.trim() || undefined,
          location: location.trim() || undefined,
        });
        enqueueSnackbar('Событие создано!', { variant: 'success' });
      }
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || (event ? 'Ошибка обновления события' : 'Ошибка создания события'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{dialogTitle}</DialogTitle>
      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2, mt: 1 }}>
            {error}
          </Alert>
        )}
        <TextField
          fullWidth
          label="Название"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          margin="normal"
          required
          autoFocus
          placeholder="Например: Встреча команды"
        />
        <TextField
          fullWidth
          label="Описание"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          margin="normal"
          multiline
          rows={3}
          placeholder="Подробное описание события"
        />
        {isEditMode && (
          <TextField
            fullWidth
            label="Дата и время"
            type="datetime-local"
            value={eventDate}
            onChange={(e) => setEventDate(e.target.value)}
            margin="normal"
            InputLabelProps={{ shrink: true }}
            inputProps={{ min: getMinDateTime() }}
            helperText="Оставьте пустым, чтобы сохранить текущее значение"
          />
        )}
        <TextField
          fullWidth
          label="Место проведения"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
          margin="normal"
          placeholder="Например: Конференц-зал, Zoom ссылка"
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={loading}>
          {loading ? (event ? 'Сохранение...' : 'Создание...') : (event ? 'Сохранить' : 'Создать')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
