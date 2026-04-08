import React, { useEffect, useState } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Chip,
  LinearProgress,
  RadioGroup,
  FormControlLabel,
  Radio,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  CircularProgress,
} from '@mui/material';
import BarChartIcon from '@mui/icons-material/BarChart';
import HowToVoteIcon from '@mui/icons-material/HowToVote';
import CloseIcon from '@mui/icons-material/Close';
import { pollService } from '../../services/poll.service';
import { Poll } from '../../types';
import { useSnackbar } from 'notistack';
import { useAuth } from '../../hooks/useAuth';

interface PollsInChatProps {
  unionId: number;
}

export const PollsInChat: React.FC<PollsInChatProps> = ({ unionId }) => {
  const [polls, setPolls] = useState<Poll[]>([]);
  const [loading, setLoading] = useState(true);
  const [voteDialogOpen, setVoteDialogOpen] = useState(false);
  const [resultsDialogOpen, setResultsDialogOpen] = useState(false);
  const [selectedPoll, setSelectedPoll] = useState<Poll | null>(null);
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();

  useEffect(() => {
    loadPolls();
  }, [unionId]);

  const loadPolls = async () => {
    setLoading(true);
    try {
      const data = await pollService.getPollsByUnion(unionId);
      setPolls(data);
    } catch (error) {
      console.error('Failed to load polls:', error);
    } finally {
      setLoading(false);
    }
  };

  const isPollActive = (expiresAt: string | null) => {
    if (!expiresAt) return true;
    return new Date(expiresAt) > new Date();
  };

  const hasUserVoted = (poll: Poll) => {
    if (!poll.options) return false;
    return poll.options.some(option => option.voted === true);
  };

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

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" p={2}>
        <CircularProgress size={24} />
      </Box>
    );
  }

  if (polls.length === 0) {
    return null;
  }

  // Показываем только активные голосования
  const activePolls = polls.filter(poll => isPollActive(poll.expiresAt) && !hasUserVoted(poll));
  const completedPolls = polls.filter(poll => !isPollActive(poll.expiresAt));

  if (activePolls.length === 0 && completedPolls.length === 0) {
    return null;
  }

  return (
    <>
      <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
        <Typography variant="subtitle2" color="text.secondary" gutterBottom>
          <HowToVoteIcon fontSize="small" sx={{ mr: 0.5, verticalAlign: 'middle' }} />
          Активные голосования
        </Typography>

        {activePolls.length === 0 ? (
          <Typography variant="body2" color="text.secondary" sx={{ py: 1 }}>
            Нет активных голосований
          </Typography>
        ) : (
          activePolls.map(poll => (
            <PollCard
              key={poll.id}
              poll={poll}
              onVote={() => handleVoteClick(poll)}
              onResults={() => handleViewResults(poll)}
              isActive={true}
            />
          ))
        )}

        {completedPolls.length > 0 && (
          <>
            <Typography variant="subtitle2" color="text.secondary" sx={{ mt: 2 }} gutterBottom>
              Завершенные голосования
            </Typography>
            {completedPolls.map(poll => (
              <PollCard
                key={poll.id}
                poll={poll}
                onVote={() => {}}
                onResults={() => handleViewResults(poll)}
                isActive={false}
              />
            ))}
          </>
        )}
      </Box>

      {/* Диалог голосования */}
      <Dialog open={voteDialogOpen} onClose={() => setVoteDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Голосование</Typography>
            <IconButton onClick={() => setVoteDialogOpen(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
            {selectedPoll?.question}
          </Typography>
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

      {/* Диалог результатов */}
      <Dialog open={resultsDialogOpen} onClose={() => setResultsDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          <Box display="flex" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Результаты голосования</Typography>
            <IconButton onClick={() => setResultsDialogOpen(false)} size="small">
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
            {selectedPoll?.question}
          </Typography>
          {selectedPoll?.options.map((option) => {
            const totalVotes = selectedPoll.options.reduce((sum, opt) => sum + (opt.voteCount || 0), 0);
            const voteCount = option.voteCount || 0;
            const percentage = totalVotes > 0 ? (voteCount / totalVotes) * 100 : 0;

            return (
              <Box key={option.id} sx={{ mb: 2 }}>
                <Box display="flex" justifyContent="space-between" mb={0.5}>
                  <Typography variant="body2">{option.text}</Typography>
                  <Typography variant="body2" color="text.secondary">
                    {voteCount} голосов ({percentage.toFixed(1)}%)
                  </Typography>
                </Box>
                <LinearProgress variant="determinate" value={percentage} sx={{ height: 8, borderRadius: 4 }} />
              </Box>
            );
          })}
          <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
            Всего голосов: {selectedPoll?.options.reduce((sum, opt) => sum + (opt.voteCount || 0), 0)}
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setResultsDialogOpen(false)}>Закрыть</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};

// Компонент карточки голосования
const PollCard: React.FC<{
  poll: Poll;
  onVote: () => void;
  onResults: () => void;
  isActive: boolean;
}> = ({ poll, onVote, onResults, isActive }) => {
  const totalVotes = poll.options.reduce((sum, opt) => sum + (opt.voteCount || 0), 0);
  const hasUserVoted = poll.hasUserVoted;

  return (
    <Card variant="outlined" sx={{ mb: 1, bgcolor: 'background.default' }}>
      <CardContent sx={{ py: 1.5, '&:last-child': { pb: 1.5 } }}>
        <Typography variant="body2" fontWeight="bold" gutterBottom>
          {poll.question}
        </Typography>

        {/* Показываем краткую статистику для завершенных голосований */}
        {!isActive && totalVotes > 0 && (
          <Box mb={1}>
            {poll.options.slice(0, 2).map(option => {
              const percentage = totalVotes > 0 ? (option.voteCount / totalVotes) * 100 : 0;
              return (
                <Box key={option.id} display="flex" alignItems="center" gap={1} mb={0.5}>
                  <Typography variant="caption" color="text.secondary" noWrap sx={{ width: 100 }}>
                    {option.text}:
                  </Typography>
                  <LinearProgress
                    variant="determinate"
                    value={percentage}
                    sx={{ flex: 1, height: 4, borderRadius: 2 }}
                  />
                  <Typography variant="caption" color="text.secondary" sx={{ minWidth: 45 }}>
                    {percentage.toFixed(0)}%
                  </Typography>
                </Box>
              );
            })}
            {poll.options.length > 2 && (
              <Typography variant="caption" color="text.secondary">
                + еще {poll.options.length - 2} вариантов
              </Typography>
            )}
          </Box>
        )}

        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box display="flex" gap={1}>
            <Button size="small" onClick={onResults} startIcon={<BarChartIcon />}>
              Результаты
            </Button>
            {isActive && !hasUserVoted && (
              <Button size="small" variant="contained" onClick={onVote}>
                Голосовать
              </Button>
            )}
            {isActive && hasUserVoted && (
              <Chip label="Вы уже голосовали" size="small" variant="outlined" />
            )}
          </Box>
          {poll.expiresAt && (
            <Typography variant="caption" color="text.secondary">
              До: {new Date(poll.expiresAt).toLocaleDateString()}
            </Typography>
          )}
        </Box>
      </CardContent>
    </Card>
  );
};