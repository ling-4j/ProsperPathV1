import dayjs from 'dayjs/esm';

import { INotification, NewNotification } from './notification.model';

export const sampleWithRequiredData: INotification = {
  id: 10110,
  message: 'reclassify makeover',
  notificationType: 'BUDGET_EXCEEDED',
};

export const sampleWithPartialData: INotification = {
  id: 1745,
  message: 'during ouch',
  notificationType: 'OTHER',
};

export const sampleWithFullData: INotification = {
  id: 5787,
  message: 'some extra-large',
  notificationType: 'BUDGET_EXCEEDED',
  isRead: true,
  createdAt: dayjs('2025-03-20T01:34'),
};

export const sampleWithNewData: NewNotification = {
  message: 'consequently voluntarily ew',
  notificationType: 'OTHER',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
