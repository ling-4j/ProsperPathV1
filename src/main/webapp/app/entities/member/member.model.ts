import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';

export interface IMember {
  id: number;
  name?: string | null;
  note?: string | null;
  createdAt?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewMember = Omit<IMember, 'id'> & { id: null };
