import dayjs from 'dayjs/esm';

import { IEvent, NewEvent } from './event.model';

export const sampleWithRequiredData: IEvent = {
  id: 18174,
  name: 'scent yum',
};

export const sampleWithPartialData: IEvent = {
  id: 30736,
  name: 'zowie regularly optimistically',
  createdAt: dayjs('2026-01-02T10:51'),
};

export const sampleWithFullData: IEvent = {
  id: 14744,
  name: 'know yahoo',
  description: 'opera overstay',
  createdAt: dayjs('2026-01-02T23:20'),
};

export const sampleWithNewData: NewEvent = {
  name: 'throughout solder aw',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
