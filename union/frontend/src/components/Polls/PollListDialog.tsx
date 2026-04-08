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
  RadioGroup,
  FormControlLabel,
  Radio,
  LinearProgress,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CloseIcon from '@mui/icons-material/Close';
import BarChartIcon from '@mui/icons-material/BarChart';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { pollService } from '../../services/poll.service';
import { Poll } from '../../types';
import { PollCreateDialog } from './PollCreateDialog';
import { useSnackbar } from 'notistack';
import { useAuth } from '../../hooks/useAuth';

interface PollListDialogProps {
  open: boolean;
  unionId: number;
  unionName: string;
  onClose: () => void;
}

export const PollListDialog: React.FC<PollListDialogProps> = ({ open, unionId, unionName, onClose }) => {
  const [polls, setPolls] = useState<Poll[]>([]);
  const [loading, setLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editingPoll, setEditingPoll] = useState<Poll | null>(null);
  const [voteDialogOpen, setVoteDialogOpen] = useState(false);
  const [resultsDialogOpen, setResultsDialogOpen] = useState(false);
  const [selectedPoll, setSelectedPoll] = useState<Poll | null>(null);
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();

  useEffect(() => {
    if (open) {
      loadPolls();
    }
  }, [open, unionId]);

  const loadPolls = async () => {
    setLoading(true);
    try {
      const data = await pollService.getPollsByUnion(unionId);
      setPolls(data);
    } catch (error) {
      console.error('Failed to load polls:', error);
      enqueueSnackbar('Ошибка загрузки голосований', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const isPollActive = (expiresAt: string | null) => {
    if (!expiresAt) return true;
    return new Date(expiresAt) > new Date();
  };

  const hasUserVoted = (poll: Poll) => {
    return poll.options?.some(option => option.voted === true) ?? false;
  };

  const canManagePoll = (poll: Poll) => user?.id === poll.createdBy.id;

  const handleVote = async () => {
    if (!selectedPoll || !selectedOption) return;

    try {
      await pollService.vote(selectedPoll.id, selectedOption);
      enqueueSnackbar('Голос принят!', { variant: 'success' });
      setVoteDialogOpen(false);
      setSelectedOption(null);
      loadPolls();
    } catch (error: any) {
      enqueueSnackbar(error.response?.data?.message || 'Ошибка голосования', { variant: 'error' });
    }
  };

  const handleViewResults = async (poll: Poll) => {
    try {
      const results = await pollService.getPollResults(poll.id);
      setSelectedPoll(results);
      setResultsDialogOpen(true);
    } catch (error) {
      enqueueSnackbar('Ошибка загрузки результатов', { variant: 'error' });
    }
  };

  const handleVoteClick = (poll: Poll) => {
    setSelectedPoll(poll);
    setSelectedOption(null);
    setVoteDialogOpen(true);
  };

  const handleEditClick = (poll: Poll) => {
    setEditingPoll(poll);
    setCreateDialogOpen(true);
  };

  const handleDeleteClick = async (poll: Poll) => {
    if (!window.confirm(`Удалить голосование «${poll.question}»?`)) return;

    try {
      await pollService.deletePoll(poll.id);
      enqueueSnackbar('Голосование удалено', { variant: 'success' });
      loadPolls();
    } catch (error: any) {
      enqueueSnackbar(error.response?.data?.message || 'Ошибка удаления голосования', { variant: 'error' });
    }
  };

  return (
    <>
      <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Голосования: {unionName}</Typography>
            <Box>
              <IconButton onClick={() => { setEditingPoll(null); setCreateDialogOpen(true); }} color="primary" size="small">
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
          ) : polls.length === 0 ? (
            <Box textAlign="center" py={3}>
              <Typography color="text.secondary">Нет голосований</Typography>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={() => { setEditingPoll(null); setCreateDialogOpen(true); }}
                sx={{ mt: 2 }}
              >
                Создать голосование
              </Button>
            </Box>
          ) : (
            <List>
              {polls.map((poll, index) => {
                const active = isPollActive(poll.expiresAt);
                const voted = hasUserVoted(poll);
                const canVote = active && !voted;
                const canManage = canManagePoll(poll);

                return (
                  <React.Fragment key={poll.id}>
                    {index > 0 && <Divider />}
                    <ListItem alignItems="flex-start">
                      <ListItemText
                        primary={
                          <Box display="flex" alignItems="center" gap={1} flexWrap="wrap">
                            <Typography variant="subtitle1" fontWeight="bold">{poll.question}</Typography>
                            <Chip
                              label={active ? 'Активно' : 'Завершено'}
                              size="small"
                              color={active ? 'success' : 'default'}
                            />
                            {voted && <Chip label="Вы голосовали" size="small" variant="outlined" />}
                          </Box>
                        }
                        secondary={
                          <Box mt={1}>
                            <Typography variant="caption" color="text.secondary" display="block">
                              Создано: {new Date(poll.createdAt).toLocaleDateString()}
                            </Typography>
                            {poll.expiresAt && (
                              <Typography variant="caption" color="text.secondary" display="block">
                                До: {new Date(poll.expiresAt).toLocaleDateString()}
                              </Typography>
                            )}
                            {!poll.expiresAt && (
                              <Typography variant="caption" color="text.secondary" display="block">
                                Бессрочное голосование
                              </Typography>
                            )}
                            <Typography variant="caption" color="text.secondary" display="block">
                              Вариантов: {poll.options.length}
                            </Typography>
                            <Box display="flex" gap={1} mt={1} flexWrap="wrap">
                              <Button size="small" startIcon={<BarChartIcon />} onClick={() => handleViewResults(poll)}>
                                Результаты
                              </Button>
                              {canVote && (
                                <Button size="small" variant="contained" onClick={() => handleVoteClick(poll)}>
                                  Голосовать
                                </Button>
                              )}
                              {canManage && (
                                <>
                                  <Button size="small" startIcon={<EditIcon />} onClick={() => handleEditClick(poll)}>
                                    Изменить
                                  </Button>
                                  <Button size="small" color="error" startIcon={<DeleteIcon />} onClick={() => handleDeleteClick(poll)}>
                                    Удалить
                                  </Button>
                                </>
                              )}
                            </Box>
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

      <PollCreateDialog
        open={createDialogOpen}
        unionId={unionId}
        poll={editingPoll}
        onClose={() => {
          setCreateDialogOpen(false);
          setEditingPoll(null);
        }}
        onSuccess={() => {
          setCreateDialogOpen(false);
          setEditingPoll(null);
          loadPolls();
        }}
      />

      <Dialog open={voteDialogOpen} onClose={() => setVoteDialogOpen(false)}>
        <DialogTitle>{selectedPoll?.question}</DialogTitle>
        <DialogContent>
          <RadioGroup value={selectedOption} onChange={(e) => setSelectedOption(Number(e.target.value))}>
            {selectedPoll?.options.map((option) => (
              <FormControlLabel
                key={option.id}
                value={option.id}
                control={<Radio />}
                label={option.text}
              />
            ))}
          </RadioGroup>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setVoteDialogOpen(false)}>Отмена</Button>
          <Button onClick={handleVote} variant="contained" disabled={!selectedOption}>
            Проголосовать
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={resultsDialogOpen} onClose={() => setResultsDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Результаты: {selectedPoll?.question}</DialogTitle>
        <DialogContent>
          <List>
            {selectedPoll?.options.map((option) => {
              const totalVotes = selectedPoll.options.reduce((sum, opt) => sum + (opt.voteCount || 0), 0);
              const voteCount = option.voteCount || 0;
              const percentage = totalVotes > 0 ? (voteCount / totalVotes) * 100 : 0;

              return (
                <ListItem key={option.id}>
                  <Box width="100%">
                    <Box display="flex" justifyContent="space-between" mb={1}>
                      <Typography>{option.text}</Typography>
                      <Typography variant="body2">
                        {voteCount} голосов ({percentage.toFixed(1)}%)
                      </Typography>
                    </Box>
                    <LinearProgress variant="determinate" value={percentage} sx={{ height: 8, borderRadius: 4 }} />
                  </Box>
                </ListItem>
              );
            })}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setResultsDialogOpen(false)}>Закрыть</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
