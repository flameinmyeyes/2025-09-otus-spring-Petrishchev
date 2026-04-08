import React, { useEffect, useState } from 'react';
import {
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

interface PollCreateProps {
  unionId?: number;
  poll?: Poll | null;
  onClose: () => void;
  onSuccess: () => void;
}

export const PollCreate: React.FC<PollCreateProps> = ({ unionId, poll, onClose, onSuccess }) => {
  const isEditMode = !!poll;
  const [question, setQuestion] = useState('');
  const [options, setOptions] = useState<string[]>(['', '']);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    if (poll) {
      setQuestion(poll.question || '');
      const mapped = poll.options?.map((option) => option.text) ?? [];
      setOptions(mapped.length >= 2 ? mapped : ['', '']);
    } else {
      setQuestion('');
      setOptions(['', '']);
    }
    setError('');
  }, [poll]);

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
    if (!poll && !unionId) {
      setError('Выберите профсоюз');
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
        await pollService.createPoll(unionId!, {
          question: trimmedQuestion,
          options: validOptions,
        });
        enqueueSnackbar('Голосование создано!', { variant: 'success' });
      }
      onSuccess();
    } catch (error: any) {
      enqueueSnackbar(error.response?.data?.message || (poll ? 'Ошибка обновления' : 'Ошибка создания'), { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <DialogTitle>{poll ? 'Редактировать голосование' : 'Создать голосование'}</DialogTitle>
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
    </>
  );
};
