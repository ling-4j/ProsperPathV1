import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IBill } from '../bill.model';
import { BillService } from '../service/bill.service';

@Component({
  templateUrl: './bill-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class BillDeleteDialogComponent {
  bill?: IBill;

  protected billService = inject(BillService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.billService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
