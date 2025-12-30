import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { TransactionService } from '../service/transaction.service';
import { FormsModule } from '@angular/forms';
import SharedModule from 'app/shared/shared.module';

@Component({
  templateUrl: './transaction-import-dialog.component.html',
  styleUrls: ['./transaction-import-dialog.component.scss'],
  imports: [SharedModule, FormsModule],
})
export class TransactionImportDialogComponent {
  selectedFile?: File;
  isUploading = false;

  constructor(
    protected activeModal: NgbActiveModal,
    protected transactionService: TransactionService,
  ) {}

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files?.length) {
      this.selectedFile = input.files[0];
    }
  }

  cancel(): void {
    this.activeModal.dismiss();
  }

  import(): void {
    if (!this.selectedFile) {
      return;
    }

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.isUploading = true;
    this.transactionService.import(formData).subscribe({
      next: () => this.activeModal.close('imported'),
      error: () => (this.isUploading = false),
    });
  }
}
