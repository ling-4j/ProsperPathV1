import dayjs from 'dayjs/esm';
import { ITeam } from 'app/entities/team/team.model';
import { IMember } from 'app/entities/member/member.model';

export interface IEvent {
  id: number;
  name?: string | null;
  description?: string | null;
  createdAt?: dayjs.Dayjs | null;
  team?: ITeam | null;
  keyPayer?: IMember | null;
}

export type NewEvent = Omit<IEvent, 'id'> & { id: null };
