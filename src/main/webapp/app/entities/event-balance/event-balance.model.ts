import { IEvent } from 'app/entities/event/event.model';
import { IMember } from 'app/entities/member/member.model';

export interface IEventBalance {
  id: number;
  paid?: number | null;
  shouldPay?: number | null;
  balance?: number | null;
  event?: IEvent | null;
  member?: IMember | null;
}

export type NewEventBalance = Omit<IEventBalance, 'id'> & { id: null };
