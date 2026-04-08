import React, { useEffect, useMemo, useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  Box,
  IconButton,
  Typography,
  Alert,
} from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import { pollService } from '../../services/poll.service';
import { Poll } from '../../types';
import { useSnackbar } from 'notistack';

interface PollCreateDialogProps {
  open: boolean;
  unionId: number;
  onClose: () => void;
  onSuccess: () => void;
  poll?: Poll | null;
}

export const PollCreateDialog: React.FC<PollCreateDialogProps> = ({
  open,
  unionId,
  onClose,
  onSuccess,
  poll,
}) => {
  const isEditMode = !!poll;
  const [question, setQuestion] = useState('');
  const [options, setOptions] = useState<string[]>(['', '']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    if (!open) return;

    if (poll) {
      setQuestion(poll.question || '');
      const mappedOptions = poll.options?.map((option) => option.text) ?? [];
      setOptions(mappedOptions.length >= 2 ? mappedOptions : ['', '']);
    } else {
      setQuestion('');
      setOptions(['', '']);
    }
    setError('');
  }, [open, poll]);

  const dialogTitle = useMemo(
    () => (isEditMode ? 'Редактировать голосование' : 'Создать голосование'),
    [isEditMode]
  );

  const addOption = () => {
    if (options.length < 10) {
      setOptions([...options, '']);
    }
  };

  const removeOption = (index: number) => {
    if (options.length > 2) {
      setOptions(options.filter((_, i) => i !== index));
    }
  };

  const updateOption = (index: number, value: string) => {
    const next = [...options];
    next[index] = value;
    setOptions(next);
  };

  const handleSubmit = async () => {
    setError('');

    const trimmedQuestion = question.trim();
    const validOptions = options.map(option => option.trim()).filter(Boolean);

    if (!trimmedQuestion) {
      setError('Введите вопрос');
      return;
    }
    if (validOptions.length < 2) {
      setError('Добавьте минимум 2 варианта ответа');
      return;
    }

    setLoading(true);
    try {
      if (poll) {
        await pollService.updatePoll(poll.id, {
          question: trimmedQuestion,
          options: validOptions,
        });
        enqueueSnackbar('Голосование обновлено!', { variant: 'success' });
      } else {
        await pollService.createPoll(unionId, {
          question: trimmedQuestion,
          options: validOptions,
        });
        enqueueSnackbar('Голосование создано!', { variant: 'success' });
      }
      onSuccess();
    } catch (err: any) {
      setError(err.response?.data?.message || (poll ? 'Ошибка обновления голосования' : 'Ошибка создания голосования'));
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
          label="Вопрос"
          value={question}
          onChange={(e) => setQuestion(e.target.value)}
          margin="normal"
          required
          autoFocus
        />
        <Typography variant="subtitle2" sx={{ mt: 2, mb: 1 }}>
          Варианты ответов (минимум 2):
        </Typography>
        {options.map((option, index) => (
          <Box key={index} sx={{ display: 'flex', gap: 1, mb: 1 }}>
            <TextField
              fullWidth
              placeholder={`Вариант ${index + 1}`}
              value={option}
              onChange={(e) => updateOption(index, e.target.value)}
              size="small"
            />
            {options.length > 2 && (
              <IconButton onClick={() => removeOption(index)} color="error" size="small">
                <DeleteIcon />
              </IconButton>
            )}
          </Box>
        ))}
        {options.length < 10 && (
          <Button startIcon={<AddIcon />} onClick={addOption} size="small" sx={{ mt: 1 }}>
            Добавить вариант
          </Button>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Отмена</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={loading}>
          {loading ? (poll ? 'Сохранение...' : 'Создание...') : (poll ? 'Сохранить' : 'Создать')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
