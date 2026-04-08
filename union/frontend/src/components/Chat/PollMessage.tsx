import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  Chip,
  LinearProgress,
  RadioGroup,
  FormControlLabel,
  Radio,
  Divider,
  IconButton,
  Collapse,
} from '@mui/material';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';
import HowToVoteIcon from '@mui/icons-material/HowToVote';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import { Poll } from '../../types';
import { pollService } from '../../services/poll.service';
import { useSnackbar } from 'notistack';
import { useAuth } from '../../hooks/useAuth';

interface PollMessageProps {
  poll: Poll;
  onVoteComplete?: () => void;
}

export const PollMessage: React.FC<PollMessageProps> = ({ poll, onVoteComplete }) => {
  const [selectedOption, setSelectedOption] = useState<number | null>(null);
  const [voting, setVoting] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [localPoll, setLocalPoll] = useState<Poll>(poll);
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();

  const isActive = () => {
    if (!localPoll.expiresAt) return true;
    return new Date(localPoll.expiresAt) > new Date();
  };

  const hasUserVoted = () => {
    return localPoll.hasUserVoted || localPoll.options.some(opt => opt.voted === true);
  };

  const handleVote = async () => {
    if (!selectedOption) return;

    setVoting(true);
    try {
      await pollService.vote(localPoll.id, selectedOption);
      enqueueSnackbar('Голос принят!', { variant: 'success' });

      const updatedPoll = await pollService.getPollResults(localPoll.id);
      setLocalPoll(updatedPoll);
      setShowResults(true);

      if (onVoteComplete) onVoteComplete();
    } catch (error: any) {
      enqueueSnackbar(error.response?.data?.message || 'Ошибка голосования', { variant: 'error' });
    } finally {
      setVoting(false);
    }
  };

  const active = isActive();
  const voted = hasUserVoted();
  const canVote = active && !voted;

  const totalVotes = localPoll.options.reduce((sum, opt) => sum + (opt.voteCount || 0), 0);
  const userVotedOptionId = localPoll.options.find(opt => opt.voted === true)?.id;

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', my: 1 }}>
      <Paper
        elevation={2}
        sx={{
          maxWidth: '80%',
          p: 2,
          bgcolor: 'background.default',
          borderRadius: 3,
          border: '1px solid',
          borderColor: 'divider',
        }}
      >
        {/* Заголовок */}
        <Box display="flex" alignItems="center" gap={1} mb={1}>
          <HowToVoteIcon fontSize="small" color="primary" />
          <Typography variant="caption" color="primary">
            Голосование
          </Typography>
          <Chip
            label={active ? 'Активно' : 'Завершено'}
            size="small"
            color={active ? 'success' : 'default'}
            sx={{ height: 20, fontSize: '0.7rem' }}
          />
          {voted && !active && (
            <Chip
              label="Вы голосовали"
              size="small"
              variant="outlined"
              sx={{ height: 20, fontSize: '0.7rem' }}
            />
          )}
        </Box>

        {/* Вопрос */}
        <Typography variant="body1" fontWeight="bold" gutterBottom>
          {localPoll.question}
        </Typography>

        {/* Опции для голосования */}
        {!showResults && canVote && (
          <Box sx={{ mt: 1 }}>
            <RadioGroup value={selectedOption} onChange={(e) => setSelectedOption(Number(e.target.value))}>
              {localPoll.options.map((option) => (
                <FormControlLabel
                  key={option.id}
                  value={option.id}
                  control={<Radio size="small" />}
                  label={option.text}
                  sx={{ '& .MuiTypography-root': { fontSize: '0.9rem' } }}
                />
              ))}
            </RadioGroup>
            <Button
              variant="contained"
              size="small"
              onClick={handleVote}
              disabled={!selectedOption || voting}
              sx={{ mt: 1 }}
            >
              {voting ? 'Отправка...' : 'Проголосовать'}
            </Button>
          </Box>
        )}

        {/* Результаты */}
        {(showResults || !canVote) && (
          <Box sx={{ mt: 1 }}>
            {localPoll.options.map((option) => {
              const voteCount = option.voteCount || 0;
              const percentage = totalVotes > 0 ? (voteCount / totalVotes) * 100 : 0;
              const isUserVote = userVotedOptionId === option.id;

              return (
                <Box key={option.id} sx={{ mb: 1.5 }}>
                  <Box display="flex" justifyContent="space-between" mb={0.5}>
                    <Typography variant="body2">
                      {option.text}
                      {isUserVote && (
                        <CheckCircleIcon fontSize="small" color="success" sx={{ ml: 0.5, verticalAlign: 'middle' }} />
                      )}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {voteCount} голосов ({percentage.toFixed(1)}%)
                    </Typography>
                  </Box>
                  <LinearProgress
                    variant="determinate"
                    value={percentage}
                    sx={{ height: 6, borderRadius: 3 }}
                  />
                </Box>
              );
            })}
            <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
              Всего голосов: {totalVotes}
            </Typography>
            {localPoll.expiresAt && !active && (
              <Typography variant="caption" color="text.secondary" display="block">
                Завершено: {new Date(localPoll.expiresAt).toLocaleDateString()}
              </Typography>
            )}
          </Box>
        )}

        {/* Кнопка показать/скрыть результаты */}
        {canVote && !showResults && (
          <Button
            size="small"
            onClick={() => setShowResults(true)}
            sx={{ mt: 1 }}
          >
            Показать результаты
          </Button>
        )}

        {canVote && showResults && (
          <Button
            size="small"
            onClick={() => setShowResults(false)}
            sx={{ mt: 1 }}
          >
            Скрыть результаты
          </Button>
        )}

        {/* Дата создания */}
        <Typography variant="caption" color="text.secondary" display="block" sx={{ mt: 1 }}>
          Создано: {new Date(localPoll.createdAt).toLocaleString()}
        </Typography>
      </Paper>
    </Box>
  );
};