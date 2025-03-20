import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { NotificationType } from 'app/entities/enumerations/notification-type.model';

export interface INotification {
  id: number;
  message?: string | null;
  notificationType?: keyof typeof NotificationType | null;
  isRead?: boolean | null;
  createdAt?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewNotification = Omit<INotification, 'id'> & { id: null };
