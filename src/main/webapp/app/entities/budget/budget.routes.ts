import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import BudgetResolve from './route/budget-routing-resolve.service';

const budgetRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/budget.component').then(m => m.BudgetComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/budget-detail.component').then(m => m.BudgetDetailComponent),
    resolve: {
      budget: BudgetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/budget-update.component').then(m => m.BudgetUpdateComponent),
    resolve: {
      budget: BudgetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/budget-update.component').then(m => m.BudgetUpdateComponent),
    resolve: {
      budget: BudgetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default budgetRoute;
