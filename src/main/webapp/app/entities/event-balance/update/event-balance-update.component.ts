import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IEvent } from 'app/entities/event/event.model';
import { EventService } from 'app/entities/event/service/event.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { EventBalanceService } from '../service/event-balance.service';
import { IEventBalance } from '../event-balance.model';
import { EventBalanceFormGroup, EventBalanceFormService } from './event-balance-form.service';

@Component({
  selector: 'jhi-event-balance-update',
  templateUrl: './event-balance-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class EventBalanceUpdateComponent implements OnInit {
  isSaving = false;
  eventBalance: IEventBalance | null = null;

  eventsSharedCollection: IEvent[] = [];
  membersSharedCollection: IMember[] = [];

  protected eventBalanceService = inject(EventBalanceService);
  protected eventBalanceFormService = inject(EventBalanceFormService);
  protected eventService = inject(EventService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: EventBalanceFormGroup = this.eventBalanceFormService.createEventBalanceFormGroup();

  compareEvent = (o1: IEvent | null, o2: IEvent | null): boolean => this.eventService.compareEvent(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ eventBalance }) => {
      this.eventBalance = eventBalance;
      if (eventBalance) {
        this.updateForm(eventBalance);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const eventBalance = this.eventBalanceFormService.getEventBalance(this.editForm);
    if (eventBalance.id !== null) {
      this.subscribeToSaveResponse(this.eventBalanceService.update(eventBalance));
    } else {
      this.subscribeToSaveResponse(this.eventBalanceService.create(eventBalance));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IEventBalance>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(eventBalance: IEventBalance): void {
    this.eventBalance = eventBalance;
    this.eventBalanceFormService.resetForm(this.editForm, eventBalance);

    this.eventsSharedCollection = this.eventService.addEventToCollectionIfMissing<IEvent>(this.eventsSharedCollection, eventBalance.event);
    this.membersSharedCollection = this.memberService.addMemberToCollectionIfMissing<IMember>(
      this.membersSharedCollection,
      eventBalance.member,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.eventService
      .query()
      .pipe(map((res: HttpResponse<IEvent[]>) => res.body ?? []))
      .pipe(map((events: IEvent[]) => this.eventService.addEventToCollectionIfMissing<IEvent>(events, this.eventBalance?.event)))
      .subscribe((events: IEvent[]) => (this.eventsSharedCollection = events));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.eventBalance?.member)))
      .subscribe((members: IMember[]) => (this.membersSharedCollection = members));
  }
}
