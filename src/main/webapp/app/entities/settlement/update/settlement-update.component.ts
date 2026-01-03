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
import { SettlementService } from '../service/settlement.service';
import { ISettlement } from '../settlement.model';
import { SettlementFormGroup, SettlementFormService } from './settlement-form.service';

@Component({
  selector: 'jhi-settlement-update',
  templateUrl: './settlement-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class SettlementUpdateComponent implements OnInit {
  isSaving = false;
  settlement: ISettlement | null = null;

  eventsSharedCollection: IEvent[] = [];
  membersSharedCollection: IMember[] = [];

  protected settlementService = inject(SettlementService);
  protected settlementFormService = inject(SettlementFormService);
  protected eventService = inject(EventService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: SettlementFormGroup = this.settlementFormService.createSettlementFormGroup();

  compareEvent = (o1: IEvent | null, o2: IEvent | null): boolean => this.eventService.compareEvent(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ settlement }) => {
      this.settlement = settlement;
      if (settlement) {
        this.updateForm(settlement);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const settlement = this.settlementFormService.getSettlement(this.editForm);
    if (settlement.id !== null) {
      this.subscribeToSaveResponse(this.settlementService.update(settlement));
    } else {
      this.subscribeToSaveResponse(this.settlementService.create(settlement));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISettlement>>): void {
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

  protected updateForm(settlement: ISettlement): void {
    this.settlement = settlement;
    this.settlementFormService.resetForm(this.editForm, settlement);

    this.eventsSharedCollection = this.eventService.addEventToCollectionIfMissing<IEvent>(this.eventsSharedCollection, settlement.event);
    this.membersSharedCollection = this.memberService.addMemberToCollectionIfMissing<IMember>(
      this.membersSharedCollection,
      settlement.fromMember,
      settlement.toMember,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.eventService
      .query()
      .pipe(map((res: HttpResponse<IEvent[]>) => res.body ?? []))
      .pipe(map((events: IEvent[]) => this.eventService.addEventToCollectionIfMissing<IEvent>(events, this.settlement?.event)))
      .subscribe((events: IEvent[]) => (this.eventsSharedCollection = events));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(
        map((members: IMember[]) =>
          this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.settlement?.fromMember, this.settlement?.toMember),
        ),
      )
      .subscribe((members: IMember[]) => (this.membersSharedCollection = members));
  }
}
