import dayjs from 'dayjs/esm';

import { IBill, NewBill } from './bill.model';

export const sampleWithRequiredData: IBill = {
  id: 5076,
  name: 'kiddingly warp',
  amount: 21800.39,
};

export const sampleWithPartialData: IBill = {
  id: 12913,
  name: 'calmly',
  amount: 13568.16,
};

export const sampleWithFullData: IBill = {
  id: 9481,
  name: 'oof',
  amount: 18886.21,
  createdAt: dayjs('2026-01-03T01:08'),
};

export const sampleWithNewData: NewBill = {
  name: 'afore once',
  amount: 30988.12,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
