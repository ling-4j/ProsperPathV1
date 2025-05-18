import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IBudget } from '../budget.model';
import { BudgetService } from '../service/budget.service';

@Component({
  templateUrl: './budget-delete-dialog.component.html',
  styleUrls: ['./budget-delete-dialog.component.scss'],
  imports: [SharedModule, FormsModule],
})
export class BudgetDeleteDialogComponent {
  budget?: IBudget;

  protected budgetService = inject(BudgetService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.budgetService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
