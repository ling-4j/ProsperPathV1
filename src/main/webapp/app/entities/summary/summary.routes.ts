import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import SummaryResolve from './route/summary-routing-resolve.service';

const summaryRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/summary.component').then(m => m.SummaryComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/summary-detail.component').then(m => m.SummaryDetailComponent),
    resolve: {
      summary: SummaryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/summary-update.component').then(m => m.SummaryUpdateComponent),
    resolve: {
      summary: SummaryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/summary-update.component').then(m => m.SummaryUpdateComponent),
    resolve: {
      summary: SummaryResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default summaryRoute;
