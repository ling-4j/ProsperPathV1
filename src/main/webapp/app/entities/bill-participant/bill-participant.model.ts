import { IBill } from 'app/entities/bill/bill.model';
import { IMember } from 'app/entities/member/member.model';

export interface IBillParticipant {
  id: number;
  shareAmount?: number | null;
  bill?: IBill | null;
  member?: IMember | null;
}

export type NewBillParticipant = Omit<IBillParticipant, 'id'> & { id: null };
