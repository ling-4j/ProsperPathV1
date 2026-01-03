import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IEventBalance } from '../event-balance.model';

@Component({
  selector: 'jhi-event-balance-detail',
  templateUrl: './event-balance-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class EventBalanceDetailComponent {
  eventBalance = input<IEventBalance | null>(null);

  previousState(): void {
    window.history.back();
  }
}
