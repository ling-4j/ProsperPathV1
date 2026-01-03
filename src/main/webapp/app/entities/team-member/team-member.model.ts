import dayjs from 'dayjs/esm';
import { ITeam } from 'app/entities/team/team.model';
import { IMember } from 'app/entities/member/member.model';

export interface ITeamMember {
  id: number;
  joinedAt?: dayjs.Dayjs | null;
  team?: ITeam | null;
  member?: IMember | null;
}

export type NewTeamMember = Omit<ITeamMember, 'id'> & { id: null };
