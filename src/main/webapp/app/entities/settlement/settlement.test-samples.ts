import { ISettlement, NewSettlement } from './settlement.model';

export const sampleWithRequiredData: ISettlement = {
  id: 6217,
  amount: 3778.84,
};

export const sampleWithPartialData: ISettlement = {
  id: 5480,
  amount: 24590.86,
};

export const sampleWithFullData: ISettlement = {
  id: 19948,
  amount: 28823.87,
};

export const sampleWithNewData: NewSettlement = {
  amount: 21225.85,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
