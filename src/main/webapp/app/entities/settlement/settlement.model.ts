import { IEvent } from 'app/entities/event/event.model';
import { IMember } from 'app/entities/member/member.model';

export interface ISettlement {
  id: number;
  amount?: number | null;
  event?: IEvent | null;
  fromMember?: IMember | null;
  toMember?: IMember | null;
}

export type NewSettlement = Omit<ISettlement, 'id'> & { id: null };
