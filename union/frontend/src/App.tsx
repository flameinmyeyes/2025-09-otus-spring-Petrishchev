import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useParams } from 'react-router-dom';
import { ThemeProvider, createTheme, Box, Typography } from '@mui/material';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { SnackbarProvider } from 'notistack';
import { Login } from './components/Auth/Login';
import { Register } from './components/Auth/Register';
import { MainLayout } from './components/Layout/MainLayout';
import { ChatRoom } from './components/Chat/ChatRoom';
import { UnionCreate } from './components/Unions/UnionCreate';
import { PollList } from './components/Polls/PollList';
import { EventList } from './components/Events/EventList';
import { FriendList } from './components/Friends/FriendList';
import { useAuth } from './hooks/useAuth';
import { unionService } from './services/union.service';
import { userService } from './services/user.service';

const theme = createTheme({
  palette: {
    primary: { main: '#1976d2' },
    secondary: { main: '#dc004e' },
    background: { default: '#f5f5f5' },
  },
});

const queryClient = new QueryClient();

const PrivateRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { user, loading } = useAuth();

  if (loading) return null;
  if (!user) return <Navigate to="/login" />;

  return <>{children}</>;
};

const ChatWrapper: React.FC = () => {
  const { unionId, friendId } = useParams<{ unionId?: string; friendId?: string }>();
  const [chatInfo, setChatInfo] = React.useState<{ id: number; name: string; type: 'union' | 'friend' } | null>(null);
  const [loading, setLoading] = React.useState(true);

  React.useEffect(() => {
    const loadChat = async () => {
      setLoading(true);
      try {
        if (unionId) {
          const data = await unionService.getUnionById(Number(unionId));
          setChatInfo({ id: data.id, name: data.name, type: 'union' });
        } else if (friendId) {
          const data = await userService.getUserById(Number(friendId));
          setChatInfo({ id: data.id, name: data.fullName || data.phoneNumber, type: 'friend' });
        } else {
          setChatInfo(null);
        }
      } catch (error) {
        console.error('Failed to load chat:', error);
        setChatInfo(null);
      } finally {
        setLoading(false);
      }
    };

    loadChat();
  }, [unionId, friendId]);

  if (loading) return <div>Загрузка...</div>;
  if (!chatInfo) return <div>Чат не найден</div>;

  return <ChatRoom chatId={chatInfo.id} chatName={chatInfo.name} chatType={chatInfo.type} />;
};

const ChatsPlaceholder: React.FC = () => {
  return (
    <Box display="flex" justifyContent="center" alignItems="center" height="100%">
      <Typography variant="h6" color="text.secondary">
        Выберите чат из списка слева
      </Typography>
    </Box>
  );
};

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ThemeProvider theme={theme}>
        <SnackbarProvider maxSnack={3}>
          <BrowserRouter>
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />
              <Route path="/unions/create" element={<PrivateRoute><UnionCreate /></PrivateRoute>} />

              <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
                <Route index element={<Navigate to="/chats" />} />
                <Route path="chats" element={<ChatsPlaceholder />} />
                <Route path="chats/union/:unionId" element={<ChatWrapper />} />
                <Route path="chats/private/:friendId" element={<ChatWrapper />} />
                <Route path="polls" element={<PollList />} />
                <Route path="events" element={<EventList />} />
                <Route path="friends" element={<FriendList />} />
              </Route>
            </Routes>
          </BrowserRouter>
        </SnackbarProvider>
      </ThemeProvider>
    </QueryClientProvider>
  );
}

export default App;
