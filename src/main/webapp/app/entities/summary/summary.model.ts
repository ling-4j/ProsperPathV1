import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { PeriodType } from 'app/entities/enumerations/period-type.model';

export interface ISummary {
  id: number;
  periodType?: keyof typeof PeriodType | null;
  periodValue?: string | null;
  totalAssets?: number | null;
  totalIncome?: number | null;
  totalExpense?: number | null;
  totalProfit?: number | null;
  profitPercentage?: number | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewSummary = Omit<ISummary, 'id'> & { id: null };
