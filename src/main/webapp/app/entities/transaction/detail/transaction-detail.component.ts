import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ITransaction } from '../transaction.model';
import { CurrencyTypePipe } from 'app/shared/truncate/currencyType';

@Component({
  selector: 'jhi-transaction-detail',
  styleUrls: ['./transaction-detail.component.scss'],
  templateUrl: './transaction-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe, CurrencyTypePipe],
})
export class TransactionDetailComponent {
  transaction = input<ITransaction | null>(null);

  previousState(): void {
    window.history.back();
  }
}
