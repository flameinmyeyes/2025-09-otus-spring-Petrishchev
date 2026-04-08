import React from 'react';
import { Menu, MenuItem, ListItemIcon, ListItemText } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import { Message } from '../../types';

interface MessageContextMenuProps {
  anchorEl: HTMLElement | null;
  message: Message | null;
  isOwn: boolean;
  onClose: () => void;
  onEdit: (message: Message) => void;
  onDelete: (messageId: number) => void;
}

export const MessageContextMenu: React.FC<MessageContextMenuProps> = ({
  anchorEl,
  message,
  isOwn,
  onClose,
  onEdit,
  onDelete,
}) => {
  if (!message) return null;

  const handleEdit = () => {
    onEdit(message);
    onClose();
  };

  const handleDelete = () => {
    onDelete(message.id);
    onClose();
  };

  return (
    <Menu
      anchorEl={anchorEl}
      open={Boolean(anchorEl)}
      onClose={onClose}
    >
      {isOwn && (
        <MenuItem onClick={handleEdit}>
          <ListItemIcon>
            <EditIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Редактировать</ListItemText>
        </MenuItem>
      )}
      {isOwn && (
        <MenuItem onClick={handleDelete}>
          <ListItemIcon>
            <DeleteIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Удалить</ListItemText>
        </MenuItem>
      )}
    </Menu>
  );
};