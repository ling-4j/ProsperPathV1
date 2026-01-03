import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import TeamResolve from './route/team-routing-resolve.service';

const teamRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/team.component').then(m => m.TeamComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/team-detail.component').then(m => m.TeamDetailComponent),
    resolve: {
      team: TeamResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/team-update.component').then(m => m.TeamUpdateComponent),
    resolve: {
      team: TeamResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/team-update.component').then(m => m.TeamUpdateComponent),
    resolve: {
      team: TeamResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default teamRoute;
