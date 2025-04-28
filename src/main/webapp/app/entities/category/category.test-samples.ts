import dayjs from 'dayjs/esm';

import { ICategory, NewCategory } from './category.model';

export const sampleWithRequiredData: ICategory = {
  id: 8109,
  categoryName: 'midst croon cautiously',
  categoryType: 'INCOME',
};

export const sampleWithPartialData: ICategory = {
  id: 23998,
  categoryName: 'appropriate functional',
  categoryType: 'INCOME',
  createdAt: dayjs('2025-03-19T22:15'),
  updatedAt: dayjs('2025-03-19T21:51'),
  categoryIcon: 'midst bah within',
};

export const sampleWithFullData: ICategory = {
  id: 28780,
  categoryName: 'ouch',
  categoryType: 'INCOME',
  createdAt: dayjs('2025-03-19T07:00'),
  updatedAt: dayjs('2025-03-19T15:41'),
  categoryIcon: 'upon gee',
};

export const sampleWithNewData: NewCategory = {
  categoryName: 'where pfft regularly',
  categoryType: 'EXPENSE',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
