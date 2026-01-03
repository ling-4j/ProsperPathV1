import dayjs from 'dayjs/esm';
import { IEvent } from 'app/entities/event/event.model';
import { IMember } from 'app/entities/member/member.model';

export interface IBill {
  id: number;
  name?: string | null;
  amount?: number | null;
  createdAt?: dayjs.Dayjs | null;
  event?: IEvent | null;
  payer?: IMember | null;
}

export type NewBill = Omit<IBill, 'id'> & { id: null };
