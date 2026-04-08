import React, { useEffect, useState } from 'react';
import {
  Container,
  Paper,
  Typography,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  Chip,
  Box,
  CircularProgress,
} from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { unionService } from '../../services/union.service';
import { Union } from '../../types';

export const UnionList: React.FC = () => {
  const [unions, setUnions] = useState<Union[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    loadUnions();
  }, []);

  const loadUnions = async () => {
    try {
      const data = await unionService.getUserUnions();
      setUnions(data);
    } catch (error) {
      console.error('Failed to load unions:', error);
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

  return (
    <Container maxWidth="md">
      <Typography variant="h4" gutterBottom>
        Мои профсоюзы
      </Typography>

      <Paper elevation={3}>
        <List>
          {unions.length === 0 ? (
            <ListItem>
              <ListItemText primary="Вы еще не состоите в профсоюзах" />
            </ListItem>
          ) : (
            unions.map((union) => (
              <ListItem
                key={union.id}
                disablePadding
                secondaryAction={
                  <Chip
                    label={`${union.members.length} участников`}
                    size="small"
                  />
                }
              >
                <ListItemButton onClick={() => navigate(`/chats/${union.id}`)}>
                  <ListItemText
                    primary={union.name}
                    secondary={union.description}
                  />
                </ListItemButton>
              </ListItem>
            ))
          )}
        </List>
      </Paper>
    </Container>
  );
};