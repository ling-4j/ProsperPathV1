import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'prosperPathApp.adminAuthority.home.title' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'budget',
    data: { pageTitle: 'prosperPathApp.budget.home.title' },
    loadChildren: () => import('./budget/budget.routes'),
  },
  {
    path: 'category',
    data: { pageTitle: 'prosperPathApp.category.home.title' },
    loadChildren: () => import('./category/category.routes'),
  },
  {
    path: 'notification',
    data: { pageTitle: 'prosperPathApp.notification.home.title' },
    loadChildren: () => import('./notification/notification.routes'),
  },
  {
    path: 'transaction',
    data: { pageTitle: 'prosperPathApp.transaction.home.title' },
    loadChildren: () => import('./transaction/transaction.routes'),
  },
  {
    path: 'summary',
    data: { pageTitle: 'prosperPathApp.summary.home.title' },
    loadChildren: () => import('./summary/summary.routes'),
  },
  {
    path: 'gold-cal',
    data: { pageTitle: 'Gold Calculator' },
    loadChildren: () => import('./gold-cal/gold-cal.routes').then(m => m.goldCalRoutes),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
