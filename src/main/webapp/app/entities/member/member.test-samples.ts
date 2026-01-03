import dayjs from 'dayjs/esm';

import { IMember, NewMember } from './member.model';

export const sampleWithRequiredData: IMember = {
  id: 26797,
  name: 'huzzah',
};

export const sampleWithPartialData: IMember = {
  id: 8524,
  name: 'rawhide indeed an',
};

export const sampleWithFullData: IMember = {
  id: 30531,
  name: 'triangular first',
  note: 'unearth nor',
  createdAt: dayjs('2026-01-02T20:31'),
};

export const sampleWithNewData: NewMember = {
  name: 'mystify',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
