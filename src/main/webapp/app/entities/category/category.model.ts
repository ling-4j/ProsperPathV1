import dayjs from 'dayjs/esm';
import { IUser } from 'app/entities/user/user.model';
import { CategoryType } from 'app/entities/enumerations/category-type.model';

export interface ICategory {
  id: number;
  categoryName?: string | null;
  categoryType?: keyof typeof CategoryType | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  categoryIcon?: string | null;
  user?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewCategory = Omit<ICategory, 'id'> & { id: null };
