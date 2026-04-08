import React, { useEffect, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  List,
  ListItem,
  ListItemText,
  Typography,
  Box,
  Chip,
  CircularProgress,
  IconButton,
  Divider,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { eventService } from '../../services/event.service';
import { Event } from '../../types';
import { EventCreateDialog } from './EventCreateDialog';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import { useSnackbar } from 'notistack';
import { useAuth } from '../../hooks/useAuth';

interface EventListDialogProps {
  open: boolean;
  unionId: number;
  unionName: string;
  onClose: () => void;
}

export const EventListDialog: React.FC<EventListDialogProps> = ({ open, unionId, unionName, onClose }) => {
  const [events, setEvents] = useState<Event[]>([]);
  const [loading, setLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Event | null>(null);
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();

  useEffect(() => {
    if (open) {
      loadEvents();
    }
  }, [open, unionId]);

  const loadEvents = async () => {
    setLoading(true);
    try {
      const data = await eventService.getEventsByUnion(unionId);
      setEvents(data);
    } catch (error) {
      console.error('Failed to load events:', error);
      enqueueSnackbar('Ошибка загрузки событий', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const isUpcoming = (eventDate?: string | null) => {
    if (!eventDate) return false;
    return new Date(eventDate) > new Date();
  };

  const formatDate = (dateString?: string | null) => {
    if (!dateString) return 'Дата не указана';
    try {
      return format(new Date(dateString), 'dd MMMM yyyy, HH:mm', { locale: ru });
    } catch {
      return dateString;
    }
  };

  const canManageEvent = (event: Event) => user?.id === event.createdBy.id;

  const handleEditClick = (event: Event) => {
    setEditingEvent(event);
    setCreateDialogOpen(true);
  };

  const handleDeleteClick = async (event: Event) => {
    if (!window.confirm(`Удалить событие «${event.title}»?`)) return;

    try {
      await eventService.deleteEvent(event.id);
      enqueueSnackbar('Событие удалено', { variant: 'success' });
      loadEvents();
    } catch (error: any) {
      enqueueSnackbar(error.response?.data?.message || 'Ошибка удаления события', { variant: 'error' });
    }
  };

  return (
    <>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">События: {unionName}</Typography>
            <Box>
              <IconButton onClick={() => { setEditingEvent(null); setCreateDialogOpen(true); }} color="primary" size="small">
                <AddIcon />
              </IconButton>
              <IconButton onClick={onClose} size="small">
                <CloseIcon />
              </IconButton>
            </Box>
          </Box>
        </DialogTitle>
        <Divider />
        <DialogContent>
          {loading ? (
            <Box display="flex" justifyContent="center" p={3}>
              <CircularProgress />
            </Box>
          ) : events.length === 0 ? (
            <Box textAlign="center" py={3}>
              <Typography color="text.secondary">Нет запланированных событий</Typography>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={() => { setEditingEvent(null); setCreateDialogOpen(true); }}
                sx={{ mt: 2 }}
              >
                Создать первое событие
              </Button>
            </Box>
          ) : (
            <List>
              {events.map((event, index) => {
                const upcoming = isUpcoming(event.eventDate);
                const canManage = canManageEvent(event);

                return (
                  <React.Fragment key={event.id}>
                    {index > 0 && <Divider />}
                    <ListItem alignItems="flex-start">
                      <ListItemText
                        primary={
                          <Box display="flex" alignItems="center" gap={1} flexWrap="wrap">
                            <Typography variant="subtitle1" fontWeight="bold">{event.title}</Typography>
                            <Chip
                              label={upcoming ? 'Предстоит' : event.eventDate ? 'Прошло' : 'Без даты'}
                              size="small"
                              color={upcoming ? 'success' : 'default'}
                            />
                          </Box>
                        }
                        secondary={
                          <Box mt={1}>
                            <Typography variant="body2" color="text.secondary">{event.description}</Typography>
                            <Box display="flex" gap={2} mt={1} flexWrap="wrap">
                              <Chip label={`📅 ${formatDate(event.eventDate)}`} size="small" variant="outlined" />
                              {event.location && <Chip label={`📍 ${event.location}`} size="small" variant="outlined" />}
                              <Chip label={`👤 ${event.createdBy.fullName || event.createdBy.phoneNumber}`} size="small" variant="outlined" />
                            </Box>
                            {canManage && (
                              <Box display="flex" gap={1} mt={1} flexWrap="wrap">
                                <Button size="small" startIcon={<EditIcon />} onClick={() => handleEditClick(event)}>
                                  Изменить
                                </Button>
                                <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => handleDeleteClick(event)}>
                                  Удалить
                                </Button>
                              </Box>
                            )}
                          </Box>
                        }
                      />
                    </ListItem>
                  </React.Fragment>
                );
              })}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={onClose}>Закрыть</Button>
        </DialogActions>
      </Dialog>

      <EventCreateDialog
        open={createDialogOpen}
        unionId={unionId}
        event={editingEvent}
        onClose={() => {
          setCreateDialogOpen(false);
          setEditingEvent(null);
        }}
        onSuccess={() => {
          setCreateDialogOpen(false);
          setEditingEvent(null);
          loadEvents();
        }}
      />
    </>
  );
};
