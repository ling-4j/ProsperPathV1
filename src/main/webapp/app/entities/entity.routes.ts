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
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
