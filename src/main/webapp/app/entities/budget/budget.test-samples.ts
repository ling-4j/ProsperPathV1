import dayjs from 'dayjs/esm';

import { IBudget, NewBudget } from './budget.model';

export const sampleWithRequiredData: IBudget = {
  id: 28039,
  budgetAmount: 12183.2,
  startDate: dayjs('2025-03-19T07:00'),
  endDate: dayjs('2025-03-20T00:49'),
};

export const sampleWithPartialData: IBudget = {
  id: 22927,
  budgetAmount: 23840.47,
  startDate: dayjs('2025-03-19T21:44'),
  endDate: dayjs('2025-03-19T21:17'),
  updatedAt: dayjs('2025-03-19T11:13'),
  status: 'ENDED',
};

export const sampleWithFullData: IBudget = {
  id: 6840,
  budgetAmount: 1844.8,
  startDate: dayjs('2025-03-19T05:48'),
  endDate: dayjs('2025-03-19T22:06'),
  createdAt: dayjs('2025-03-19T14:58'),
  updatedAt: dayjs('2025-03-19T20:46'),
  status: 'ACTIVE',
};

export const sampleWithNewData: NewBudget = {
  budgetAmount: 1339.85,
  startDate: dayjs('2025-03-19T04:39'),
  endDate: dayjs('2025-03-19T05:38'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
