import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Container,
  Paper,
  Typography,
  Box,
  Chip,
  Button,
  List,
  ListItem,
  ListItemText,
  ListItemAvatar,
  Avatar,
  CircularProgress,
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { unionService } from '../../services/union.service';
import { Union } from '../../types';
import { useSnackbar } from 'notistack';

export const UnionDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [union, setUnion] = useState<Union | null>(null);
  const [loading, setLoading] = useState(true);
  const { enqueueSnackbar } = useSnackbar();

  useEffect(() => {
    if (id) {
      loadUnion();
    }
  }, [id]);

  const loadUnion = async () => {
    try {
      const data = await unionService.getUnionById(parseInt(id!));
      setUnion(data);
    } catch (error) {
      console.error('Failed to load union:', error);
      enqueueSnackbar('Ошибка загрузки профсоюза', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    );
  }

  if (!union) {
    return (
      <Container>
        <Typography>Профсоюз не найден</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg">
      <Button
        startIcon={<ArrowBackIcon />}
        onClick={() => navigate('/')}
        sx={{ mb: 2 }}
      >
        Назад
      </Button>

      <Paper elevation={3} sx={{ p: 3 }}>
        <Typography variant="h4" gutterBottom>
          {union.name}
        </Typography>
        {union.description && (
          <Typography variant="body1" color="text.secondary" paragraph>
            {union.description}
          </Typography>
        )}

        <Box sx={{ mb: 3 }}>
          <Chip
            label={`Создатель: ${union.creator.fullName || union.creator.phoneNumber}`}
            variant="outlined"
          />
          <Chip
            label={`Участников: ${union.members.length}`}
            variant="outlined"
            sx={{ ml: 1 }}
          />
        </Box>

        <Typography variant="h6" gutterBottom>
          Участники:
        </Typography>
        <List>
          {union.members.map((member) => (
            <ListItem key={member.id}>
              <ListItemAvatar>
                <Avatar>
                  {member.fullName?.[0] || member.phoneNumber[1]}
                </Avatar>
              </ListItemAvatar>
              <ListItemText
                primary={member.fullName || member.phoneNumber}
                secondary={member.status}
              />
            </ListItem>
          ))}
        </List>
      </Paper>
    </Container>
  );
};