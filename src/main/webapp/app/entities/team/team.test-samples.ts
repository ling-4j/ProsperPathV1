import dayjs from 'dayjs/esm';

import { ITeam, NewTeam } from './team.model';

export const sampleWithRequiredData: ITeam = {
  id: 31463,
  name: 'mysterious triumphantly',
};

export const sampleWithPartialData: ITeam = {
  id: 6584,
  name: 'impeccable shear sans',
  createdAt: dayjs('2026-01-02T17:31'),
};

export const sampleWithFullData: ITeam = {
  id: 19481,
  name: 'upwardly well-lit wide',
  createdAt: dayjs('2026-01-02T23:42'),
};

export const sampleWithNewData: NewTeam = {
  name: 'unfreeze instructive waver',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
