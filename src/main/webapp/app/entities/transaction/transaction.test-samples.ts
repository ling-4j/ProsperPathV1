import dayjs from 'dayjs/esm';

import { ITransaction, NewTransaction } from './transaction.model';

export const sampleWithRequiredData: ITransaction = {
  id: 3793,
  amount: 26806.36,
  transactionType: 'INCOME',
  transactionDate: dayjs('2025-03-19T20:10'),
};

export const sampleWithPartialData: ITransaction = {
  id: 1969,
  amount: 11058.39,
  transactionType: 'EXPENSE',
  transactionDate: dayjs('2025-03-19T17:42'),
  createdAt: dayjs('2025-03-20T02:53'),
};

export const sampleWithFullData: ITransaction = {
  id: 20285,
  amount: 600.71,
  transactionType: 'EXPENSE',
  description: 'darn kissingly',
  transactionDate: dayjs('2025-03-19T07:41'),
  createdAt: dayjs('2025-03-19T16:43'),
  updatedAt: dayjs('2025-03-19T19:35'),
};

export const sampleWithNewData: NewTransaction = {
  amount: 22341.83,
  transactionType: 'EXPENSE',
  transactionDate: dayjs('2025-03-20T02:10'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
