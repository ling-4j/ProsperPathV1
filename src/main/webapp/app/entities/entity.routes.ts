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
  {
    path: 'member',
    data: { pageTitle: 'prosperPathApp.member.home.title' },
    loadChildren: () => import('./member/member.routes'),
  },
  {
    path: 'team',
    data: { pageTitle: 'prosperPathApp.team.home.title' },
    loadChildren: () => import('./team/team.routes'),
  },
  {
    path: 'team-member',
    data: { pageTitle: 'prosperPathApp.teamMember.home.title' },
    loadChildren: () => import('./team-member/team-member.routes'),
  },
  {
    path: 'event',
    data: { pageTitle: 'prosperPathApp.event.home.title' },
    loadChildren: () => import('./event/event.routes'),
  },
  {
    path: 'bill',
    data: { pageTitle: 'prosperPathApp.bill.home.title' },
    loadChildren: () => import('./bill/bill.routes'),
  },
  {
    path: 'bill-participant',
    data: { pageTitle: 'prosperPathApp.billParticipant.home.title' },
    loadChildren: () => import('./bill-participant/bill-participant.routes'),
  },
  {
    path: 'event-balance',
    data: { pageTitle: 'prosperPathApp.eventBalance.home.title' },
    loadChildren: () => import('./event-balance/event-balance.routes'),
  },
  {
    path: 'settlement',
    data: { pageTitle: 'prosperPathApp.settlement.home.title' },
    loadChildren: () => import('./settlement/settlement.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
