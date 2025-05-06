import { Routes } from '@angular/router';
import GoldProfitCalculatorComponent from '../gold-cal/gold/gold-profit-calculator.component';

export const goldCalRoutes: Routes = [
  {
    path: '',
    component: GoldProfitCalculatorComponent,
    data: {
      pageTitle: 'Gold Calculator', // Đặt tiêu đề cho trang
    },
  },
];
