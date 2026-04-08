import React, { useState } from 'react';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Alert,
  IconButton,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useNavigate } from 'react-router-dom';
import { unionService } from '../../services/union.service';
import { useSnackbar } from 'notistack';

export const UnionCreate: React.FC = () => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { enqueueSnackbar } = useSnackbar();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setError('Введите название профсоюза');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      await unionService.createUnion({ name, description });
      enqueueSnackbar('Профсоюз успешно создан!', { variant: 'success' });
      navigate('/chats');
    } catch (err: any) {
      setError(err.response?.data || 'Ошибка создания профсоюза');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ height: '100vh', overflow: 'auto', bgcolor: 'background.default' }}>
      <Container maxWidth="md" sx={{ py: 4 }}>
        <Box sx={{ mb: 2, display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={() => navigate('/chats')}>
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h5" component="h1">
            Создать профсоюз
          </Typography>
        </Box>

        <Paper elevation={3} sx={{ p: 4 }}>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Название профсоюза"
              value={name}
              onChange={(e) => setName(e.target.value)}
              margin="normal"
              required
              disabled={loading}
              autoFocus
            />
            <TextField
              fullWidth
              label="Описание (необязательно)"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              margin="normal"
              multiline
              rows={4}
              disabled={loading}
            />
            <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
              <Button
                type="submit"
                variant="contained"
                color="primary"
                disabled={loading}
              >
                {loading ? 'Создание...' : 'Создать'}
              </Button>
              <Button
                variant="outlined"
                onClick={() => navigate('/chats')}
                disabled={loading}
              >
                Отмена
              </Button>
            </Box>
          </form>
        </Paper>
      </Container>
    </Box>
  );
};