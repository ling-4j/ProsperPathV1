import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ISummary } from '../summary.model';
import { SummaryService } from '../service/summary.service';

@Component({
  templateUrl: './summary-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class SummaryDeleteDialogComponent {
  summary?: ISummary;

  protected summaryService = inject(SummaryService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.summaryService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
