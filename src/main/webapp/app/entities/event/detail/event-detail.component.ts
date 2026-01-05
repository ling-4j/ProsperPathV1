import { Component, input } from '@angular/core';
import { NavigationEnd, RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { IEvent } from '../event.model';
import { inject, effect } from '@angular/core';
import { Router } from '@angular/router';
import { BillService } from 'app/entities/bill/service/bill.service';
import { IBill } from 'app/entities/bill/bill.model';
import { filter } from 'rxjs';
import { EventBalanceService } from 'app/entities/event-balance/service/event-balance.service';
import { IEventBalance } from 'app/entities/event-balance/event-balance.model';
import { CurrencyTypePipe } from 'app/shared/truncate/currencyType';

@Component({
  selector: 'jhi-event-detail',
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.scss'],
  imports: [SharedModule, RouterModule, CurrencyTypePipe],
})
export class EventDetailComponent {
  event = input<IEvent | null>(null);

  bills: IBill[] = [];
  balances: IEventBalance[] = [];

  protected billService = inject(BillService);
  protected eventBalanceService = inject(EventBalanceService);
  protected router = inject(Router);

  constructor() {
    effect(() => {
      const ev = this.event();
      if (ev?.id) {
        this.loadBills();
        this.loadBalances();
      }
    });
  }

  loadBills(): void {
    const ev = this.event();
    if (ev?.id) {
      this.billService.query({ 'eventId.equals': ev.id }).subscribe(res => (this.bills = res.body ?? []));
    }
  }

  loadBalances(): void {
    const ev = this.event();
    if (ev?.id) {
      this.eventBalanceService.query({ 'eventId.equals': ev.id }).subscribe(res => (this.balances = res.body ?? []));
    }
  }

  goToBill(id: number): void {
    this.router.navigate(['/bill', id, 'view']);
  }

  addBill(): void {
    this.router.navigate(['/event', this.event()!.id, 'bill', 'new']);
  }

  previousState(): void {
    this.router.navigate(['/event']);
  }
}
