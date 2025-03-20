import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { ISummary } from '../summary.model';

@Component({
  selector: 'jhi-summary-detail',
  templateUrl: './summary-detail.component.html',
  imports: [SharedModule, RouterModule, FormatMediumDatetimePipe],
})
export class SummaryDetailComponent {
  summary = input<ISummary | null>(null);

  previousState(): void {
    window.history.back();
  }
}
