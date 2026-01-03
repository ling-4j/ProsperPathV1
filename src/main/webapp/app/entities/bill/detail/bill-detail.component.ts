import { Component, input, inject, effect } from '@angular/core';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

import SharedModule from 'app/shared/shared.module';
import { IBill } from '../bill.model';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { BillParticipantService } from 'app/entities/bill-participant/service/bill-participant.service';
import { EventService } from 'app/entities/event/service/event.service';
import { CurrencyTypePipe } from 'app/shared/truncate/currencyType';

@Component({
  selector: 'jhi-bill-detail',
  templateUrl: './bill-detail.component.html',
  imports: [SharedModule, RouterModule, FormsModule, CurrencyTypePipe],
})
export class BillDetailComponent {
  bill = input<IBill | null>(null);

  protected teamMemberService = inject(TeamMemberService);
  protected billParticipantService = inject(BillParticipantService);
  protected eventService = inject(EventService);

  existingParticipants: Map<number, number> = new Map();

  members: {
    id: number;
    name?: string;
    selected: boolean;
    shareAmount: number;
  }[] = [];

  constructor() {
    effect(() => {
      const bill = this.bill();
      const eventId = bill?.event?.id;
      const billId = bill?.id;

      if (eventId && billId) {
        this.loadMembersFromEvent(eventId, billId);
      }
    });
  }

  loadMembersFromEvent(eventId: number, billId: number): void {
    this.eventService.find(eventId).subscribe(res => {
      const teamId = res.body?.team?.id;
      if (!teamId) return;

      this.teamMemberService.query({ 'teamId.equals': teamId }).subscribe(res2 => {
        this.members = (res2.body ?? []).map(tm => ({
          id: tm.member!.id!,
          name: tm.member?.name ?? undefined,
          selected: false,
          shareAmount: 0,
        }));

        this.loadBillParticipants(billId);
      });
    });
  }

  loadBillParticipants(billId: number): void {
    this.billParticipantService.query({ 'billId.equals': billId }).subscribe(res => {
      const participants = res.body ?? [];

      this.existingParticipants.clear();

      participants.forEach(p => {
        if (p.member?.id && p.id) {
          this.existingParticipants.set(p.member.id, p.id);
        }
      });

      this.members = this.members.map(m => {
        const p = participants.find(bp => bp.member?.id === m.id);
        return {
          ...m,
          selected: !!p,
          shareAmount: p?.shareAmount ?? 0,
        };
      });
    });
  }

  saveParticipants(): void {
    const billId = this.bill()?.id;
    if (!billId) return;

    const selectedMemberIds = this.members.filter(m => m.selected).map(m => m.id);

    this.billParticipantService.saveBillParticipants(billId, selectedMemberIds).subscribe(() => {
      this.loadBillParticipants(billId);
    });
  }

  previousState(): void {
    window.history.back();
  }
}
