import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Message, MessageDto } from '../types';

const getWebSocketUrl = () => {
  return process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws';
};

export const useWebSocket = (chatId: number, chatType: 'union' | 'friend') => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [connected, setConnected] = useState(false);
  const stompClient = useRef<Client | null>(null);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token || !chatId) {
      setConnected(false);
      setMessages([]);
      return;
    }

    const topic = chatType === 'union'
      ? `/topic/union/${chatId}`
      : `/topic/private/${chatId}`;

    const client = new Client({
      webSocketFactory: () => new SockJS(getWebSocketUrl()),
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        setConnected(true);
        client.subscribe(topic, (message) => {
          try {
            const newMessage: Message = JSON.parse(message.body);
            setMessages((prev) => {
              if (prev.some((item) => item.id === newMessage.id)) {
                return prev;
              }
              return [...prev, newMessage];
            });
          } catch (error) {
            console.error('Failed to parse websocket message:', error);
          }
        });
      },
      onStompError: (frame) => {
        console.error('WebSocket STOMP error:', frame);
        setConnected(false);
      },
      onWebSocketError: (error) => {
        console.error('WebSocket error:', error);
        setConnected(false);
      },
      onDisconnect: () => {
        setConnected(false);
      },
    });

    stompClient.current = client;
    client.activate();

    return () => {
      client.deactivate();
      stompClient.current = null;
      setConnected(false);
      setMessages([]);
    };
  }, [chatId, chatType]);

  const sendMessage = (messageDto: MessageDto) => {
    if (stompClient.current && stompClient.current.connected) {
      stompClient.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(messageDto),
      });
    } else {
      console.warn('WebSocket not connected');
    }
  };

  return { messages, sendMessage, connected };
};
