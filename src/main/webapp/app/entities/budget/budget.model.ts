import dayjs from 'dayjs/esm';
import { ICategory } from 'app/entities/category/category.model';
import { IUser } from 'app/entities/user/user.model';

export interface IBudget {
  id: number;
  budgetAmount?: number | null;
  startDate?: dayjs.Dayjs | null;
  endDate?: dayjs.Dayjs | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  category?: ICategory | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewBudget = Omit<IBudget, 'id'> & { id: null };
