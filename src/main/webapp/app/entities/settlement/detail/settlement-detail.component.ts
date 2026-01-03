import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { ISettlement } from '../settlement.model';

@Component({
  selector: 'jhi-settlement-detail',
  templateUrl: './settlement-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class SettlementDetailComponent {
  settlement = input<ISettlement | null>(null);

  previousState(): void {
    window.history.back();
  }
}
