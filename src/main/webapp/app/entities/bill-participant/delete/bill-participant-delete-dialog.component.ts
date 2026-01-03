import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IBillParticipant } from '../bill-participant.model';
import { BillParticipantService } from '../service/bill-participant.service';

@Component({
  templateUrl: './bill-participant-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class BillParticipantDeleteDialogComponent {
  billParticipant?: IBillParticipant;

  protected billParticipantService = inject(BillParticipantService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.billParticipantService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
