import dayjs from 'dayjs/esm';
import { ICategory } from 'app/entities/category/category.model';
import { IUser } from 'app/entities/user/user.model';
import { TransactionType } from 'app/entities/enumerations/transaction-type.model';

export interface ITransaction {
  id: number;
  amount?: number | null;
  transactionType?: keyof typeof TransactionType | null;
  description?: string | null;
  transactionDate?: dayjs.Dayjs | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  category?: ICategory | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewTransaction = Omit<ITransaction, 'id'> & { id: null };
