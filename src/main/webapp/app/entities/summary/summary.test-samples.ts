import dayjs from 'dayjs/esm';

import { ISummary, NewSummary } from './summary.model';

export const sampleWithRequiredData: ISummary = {
  id: 17990,
  periodType: 'MONTH',
  periodValue: 'since forenenst premium',
};

export const sampleWithPartialData: ISummary = {
  id: 27213,
  periodType: 'WEEK',
  periodValue: 'fen',
  totalAssets: 20327.85,
  totalIncome: 23617.35,
  profitPercentage: 3462.75,
  createdAt: dayjs('2025-03-19T13:33'),
  updatedAt: dayjs('2025-03-19T15:18'),
};

export const sampleWithFullData: ISummary = {
  id: 9135,
  periodType: 'MONTH',
  periodValue: 'laughter',
  totalAssets: 25136.07,
  totalIncome: 12869.63,
  totalExpense: 19309.36,
  totalProfit: 24463.36,
  profitPercentage: 7508.16,
  createdAt: dayjs('2025-03-19T23:51'),
  updatedAt: dayjs('2025-03-19T22:19'),
};

export const sampleWithNewData: NewSummary = {
  periodType: 'YEAR',
  periodValue: 'superficial adrenalin wrongly',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
