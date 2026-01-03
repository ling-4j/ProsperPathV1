import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import BillParticipantResolve from './route/bill-participant-routing-resolve.service';

const billParticipantRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/bill-participant.component').then(m => m.BillParticipantComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/bill-participant-detail.component').then(m => m.BillParticipantDetailComponent),
    resolve: {
      billParticipant: BillParticipantResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/bill-participant-update.component').then(m => m.BillParticipantUpdateComponent),
    resolve: {
      billParticipant: BillParticipantResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/bill-participant-update.component').then(m => m.BillParticipantUpdateComponent),
    resolve: {
      billParticipant: BillParticipantResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default billParticipantRoute;
