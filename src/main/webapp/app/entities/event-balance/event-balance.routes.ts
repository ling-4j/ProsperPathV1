import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import EventBalanceResolve from './route/event-balance-routing-resolve.service';

const eventBalanceRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/event-balance.component').then(m => m.EventBalanceComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/event-balance-detail.component').then(m => m.EventBalanceDetailComponent),
    resolve: {
      eventBalance: EventBalanceResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/event-balance-update.component').then(m => m.EventBalanceUpdateComponent),
    resolve: {
      eventBalance: EventBalanceResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/event-balance-update.component').then(m => m.EventBalanceUpdateComponent),
    resolve: {
      eventBalance: EventBalanceResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default eventBalanceRoute;
