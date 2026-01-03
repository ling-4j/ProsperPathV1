import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IBillParticipant } from '../bill-participant.model';

@Component({
  selector: 'jhi-bill-participant-detail',
  templateUrl: './bill-participant-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class BillParticipantDetailComponent {
  billParticipant = input<IBillParticipant | null>(null);

  previousState(): void {
    window.history.back();
  }
}
