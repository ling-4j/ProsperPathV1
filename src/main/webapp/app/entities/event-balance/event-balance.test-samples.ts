import { IEventBalance, NewEventBalance } from './event-balance.model';

export const sampleWithRequiredData: IEventBalance = {
  id: 19492,
  paid: 8206.67,
  shouldPay: 31937.79,
  balance: 22328.94,
};

export const sampleWithPartialData: IEventBalance = {
  id: 1679,
  paid: 11087.03,
  shouldPay: 17666.56,
  balance: 13253.38,
};

export const sampleWithFullData: IEventBalance = {
  id: 22397,
  paid: 12560.86,
  shouldPay: 14913.11,
  balance: 20069.7,
};

export const sampleWithNewData: NewEventBalance = {
  paid: 6954.91,
  shouldPay: 15465.87,
  balance: 17869.88,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
