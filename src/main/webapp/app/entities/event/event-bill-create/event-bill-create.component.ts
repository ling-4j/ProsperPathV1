import { Component, inject, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { BillService } from 'app/entities/bill/service/bill.service';
import { EventService } from 'app/entities/event/service/event.service';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IEvent } from 'app/entities/event/event.model';
import { IMember } from 'app/entities/member/member.model';

@Component({
  standalone: true,
  selector: 'jhi-event-bill-create',
  templateUrl: './event-bill-create.component.html',
  styleUrls: ['./event-bill-create.component.scss'],
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class EventBillCreateComponent implements OnInit {
  protected route = inject(ActivatedRoute);
  protected router = inject(Router);

  protected billService = inject(BillService);
  protected eventService = inject(EventService);
  protected teamMemberService = inject(TeamMemberService);

  eventId!: number;
  event!: IEvent;

  members: IMember[] = [];

  editForm = new FormGroup({
    name: new FormControl<string>('', { nonNullable: true, validators: [Validators.required] }),
    amount: new FormControl<number>(0, { nonNullable: true, validators: [Validators.required] }),
    payer: new FormControl<IMember | null>(null, Validators.required),
  });

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.find(this.eventId).subscribe(res => {
      this.event = res.body!;
      this.loadTeamMembers(this.event.team!.id);
    });
  }

  loadTeamMembers(teamId: number): void {
    this.teamMemberService.query({ 'teamId.equals': teamId }).subscribe(res => {
      this.members = (res.body ?? []).map(tm => tm.member!);
    });
  }

  save(): void {
    const v = this.editForm.getRawValue();

    this.billService
      .create({
        id: null,
        name: v.name,
        amount: v.amount,
        payer: v.payer!,
        event: { id: this.eventId },
      })
      .subscribe(() => {
        this.router.navigate(['/event', this.eventId, 'view']);
      });
  }

  cancel(): void {
    this.router.navigate(['/event', this.eventId, 'view']);
  }
}
