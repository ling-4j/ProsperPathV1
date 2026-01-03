import dayjs from 'dayjs/esm';

export interface ITeam {
  id: number;
  name?: string | null;
  createdAt?: dayjs.Dayjs | null;
}

export type NewTeam = Omit<ITeam, 'id'> & { id: null };
