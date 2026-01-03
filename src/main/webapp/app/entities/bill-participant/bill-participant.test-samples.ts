import { IBillParticipant, NewBillParticipant } from './bill-participant.model';

export const sampleWithRequiredData: IBillParticipant = {
  id: 20059,
  shareAmount: 25021.8,
};

export const sampleWithPartialData: IBillParticipant = {
  id: 3838,
  shareAmount: 20606.89,
};

export const sampleWithFullData: IBillParticipant = {
  id: 20337,
  shareAmount: 22270.14,
};

export const sampleWithNewData: NewBillParticipant = {
  shareAmount: 5991.98,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
