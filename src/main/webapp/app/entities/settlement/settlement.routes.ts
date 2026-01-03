import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import SettlementResolve from './route/settlement-routing-resolve.service';

const settlementRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/settlement.component').then(m => m.SettlementComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/settlement-detail.component').then(m => m.SettlementDetailComponent),
    resolve: {
      settlement: SettlementResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/settlement-update.component').then(m => m.SettlementUpdateComponent),
    resolve: {
      settlement: SettlementResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/settlement-update.component').then(m => m.SettlementUpdateComponent),
    resolve: {
      settlement: SettlementResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default settlementRoute;
