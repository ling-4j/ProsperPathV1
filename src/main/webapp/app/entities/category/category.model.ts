import dayjs from 'dayjs/esm';
import { CategoryType } from 'app/entities/enumerations/category-type.model';

export interface ICategory {
  id: number;
  categoryName?: string | null;
  categoryType?: keyof typeof CategoryType | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  categoryIcon?: string | null;
}

export type NewCategory = Omit<ICategory, 'id'> & { id: null };
