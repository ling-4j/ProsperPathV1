import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import BillResolve from './route/bill-routing-resolve.service';

const billRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/bill.component').then(m => m.BillComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/bill-detail.component').then(m => m.BillDetailComponent),
    resolve: {
      bill: BillResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/bill-update.component').then(m => m.BillUpdateComponent),
    resolve: {
      bill: BillResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/bill-update.component').then(m => m.BillUpdateComponent),
    resolve: {
      bill: BillResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default billRoute;
