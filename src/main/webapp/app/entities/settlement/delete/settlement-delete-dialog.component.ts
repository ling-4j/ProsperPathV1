import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ISettlement } from '../settlement.model';
import { SettlementService } from '../service/settlement.service';

@Component({
  templateUrl: './settlement-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class SettlementDeleteDialogComponent {
  settlement?: ISettlement;

  protected settlementService = inject(SettlementService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.settlementService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
