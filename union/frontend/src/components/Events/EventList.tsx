import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  Card,
  CardContent,
  Button,
  Dialog,
  Box,
  Chip,
  CircularProgress,
  Alert,
  IconButton,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { eventService } from '../../services/event.service';
import { unionService } from '../../services/union.service';
import { Event, Union } from '../../types';
import { EventCreateDialog } from './EventCreateDialog';
import { format } from 'date-fns';
import { ru } from 'date-fns/locale';
import { useSnackbar } from 'notistack';
import { useAuth } from '../../hooks/useAuth';

export const EventList: React.FC = () => {
  const [events, setEvents] = useState<Event[]>([]);
  const [unions, setUnions] = useState<Union[]>([]);
  const [selectedUnion, setSelectedUnion] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editingEvent, setEditingEvent] = useState<Event | null>(null);
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();

  useEffect(() => {
    loadUnions();
  }, []);

  useEffect(() => {
    if (selectedUnion) {
      loadEvents();
    }
  }, [selectedUnion]);

  const loadUnions = async () => {
    try {
      const data = await unionService.getUserUnions();
      setUnions(data);
      if (data.length > 0) {
        setSelectedUnion(data[0].id);
      }
    } catch (error) {
      console.error('Failed to load unions:', error);
      enqueueSnackbar('Ошибка загрузки профсоюзов', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const loadEvents = async () => {
    if (!selectedUnion) return;
    try {
      const data = await eventService.getEventsByUnion(selectedUnion);
      setEvents(data);
    } catch (error) {
      console.error('Failed to load events:', error);
      enqueueSnackbar('Ошибка загрузки событий', { variant: 'error' });
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

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="lg">
      <Box sx={{ mb: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="h4">События</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => { setEditingEvent(null); setCreateDialogOpen(true); }}
          disabled={unions.length === 0}
          title={unions.length === 0 ? 'Сначала вступите или создайте профсоюз' : ''}
        >
          Создать событие
        </Button>
      </Box>

      {unions.length === 0 ? (
        <Alert severity="info" sx={{ mb: 2 }}>
          У вас нет профсоюзов. Создайте или вступите в профсоюз, чтобы планировать события.
        </Alert>
      ) : (
        <Paper elevation={3} sx={{ p: 2, mb: 3 }}>
          <Typography variant="subtitle1" gutterBottom>
            Выберите профсоюз:
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
            {unions.map((union) => (
              <Chip
                key={union.id}
                label={union.name}
                onClick={() => setSelectedUnion(union.id)}
                color={selectedUnion === union.id ? 'primary' : 'default'}
                variant={selectedUnion === union.id ? 'filled' : 'outlined'}
              />
            ))}
          </Box>
        </Paper>
      )}

      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {events.length === 0 ? (
          <Paper elevation={3} sx={{ p: 4, textAlign: 'center' }}>
            <Typography variant="body1" color="text.secondary">
              {selectedUnion ? 'Нет запланированных событий' : 'Выберите профсоюз'}
            </Typography>
          </Paper>
        ) : (
          events.map((event) => (
            <Card key={event.id} elevation={3}>
              <CardContent>
                <Box>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" gap={2}>
                    <Box>
                      <Typography variant="h6" gutterBottom>
                        {event.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        {event.description}
                      </Typography>
                    </Box>
                    {canManageEvent(event) && (
                      <Box display="flex" gap={1} flexWrap="wrap">
                        <IconButton size="small" onClick={() => { setEditingEvent(event); setCreateDialogOpen(true); }}>
                          <EditIcon />
                        </IconButton>
                        <IconButton size="small" color="error" onClick={() => handleDeleteClick(event)}>
                          <DeleteIcon />
                        </IconButton>
                      </Box>
                    )}
                  </Box>
                  <Box sx={{ mt: 2, display: 'flex', gap: 2, flexWrap: 'wrap' }}>
                    <Chip
                      label={event.eventDate ? `Дата: ${formatDate(event.eventDate)}` : 'Дата не указана'}
                      size="small"
                      color={isUpcoming(event.eventDate) ? 'primary' : 'default'}
                    />
                    {event.location && <Chip label={`Место: ${event.location}`} size="small" />}
                    <Chip
                      label={`Организатор: ${event.createdBy.fullName || event.createdBy.phoneNumber}`}
                      size="small"
                      variant="outlined"
                    />
                  </Box>
                </Box>
              </CardContent>
            </Card>
          ))
        )}
      </Box>

      <EventCreateDialog
        open={createDialogOpen}
        unionId={selectedUnion || 0}
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
    </Container>
  );
};
