import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IEventBalance } from '../event-balance.model';
import { EventBalanceService } from '../service/event-balance.service';

@Component({
  templateUrl: './event-balance-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class EventBalanceDeleteDialogComponent {
  eventBalance?: IEventBalance;

  protected eventBalanceService = inject(EventBalanceService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.eventBalanceService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
