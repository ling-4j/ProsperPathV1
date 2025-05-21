import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IBudget } from '../budget.model';
import { CurrencyTypePipe } from 'app/shared/truncate/currencyType';

@Component({
  selector: 'jhi-budget-detail',
  styleUrls: ['./budget-detail.component.scss'],
  templateUrl: './budget-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe, CurrencyTypePipe],
})
export class BudgetDetailComponent {
  budget = input<IBudget | null>(null);

  previousState(): void {
    window.history.back();
  }
}
