import dayjs from 'dayjs/esm';

import { ITeamMember, NewTeamMember } from './team-member.model';

export const sampleWithRequiredData: ITeamMember = {
  id: 2894,
};

export const sampleWithPartialData: ITeamMember = {
  id: 24739,
};

export const sampleWithFullData: ITeamMember = {
  id: 10195,
  joinedAt: dayjs('2026-01-03T07:38'),
};

export const sampleWithNewData: NewTeamMember = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
