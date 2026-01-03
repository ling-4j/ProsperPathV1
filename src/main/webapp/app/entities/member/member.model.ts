import dayjs from 'dayjs/esm';

export interface IMember {
  id: number;
  name?: string | null;
  note?: string | null;
  createdAt?: dayjs.Dayjs | null;
}

export type NewMember = Omit<IMember, 'id'> & { id: null };
