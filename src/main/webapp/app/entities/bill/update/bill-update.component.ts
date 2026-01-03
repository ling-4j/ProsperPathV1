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
import { BillService } from '../service/bill.service';
import { IBill } from '../bill.model';
import { BillFormGroup, BillFormService } from './bill-form.service';

@Component({
  selector: 'jhi-bill-update',
  templateUrl: './bill-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class BillUpdateComponent implements OnInit {
  isSaving = false;
  bill: IBill | null = null;

  eventsSharedCollection: IEvent[] = [];
  membersSharedCollection: IMember[] = [];

  protected billService = inject(BillService);
  protected billFormService = inject(BillFormService);
  protected eventService = inject(EventService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: BillFormGroup = this.billFormService.createBillFormGroup();

  compareEvent = (o1: IEvent | null, o2: IEvent | null): boolean => this.eventService.compareEvent(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ bill }) => {
      this.bill = bill;
      if (bill) {
        this.updateForm(bill);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const bill = this.billFormService.getBill(this.editForm);
    if (bill.id !== null) {
      this.subscribeToSaveResponse(this.billService.update(bill));
    } else {
      this.subscribeToSaveResponse(this.billService.create(bill));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBill>>): void {
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

  protected updateForm(bill: IBill): void {
    this.bill = bill;
    this.billFormService.resetForm(this.editForm, bill);

    this.eventsSharedCollection = this.eventService.addEventToCollectionIfMissing<IEvent>(this.eventsSharedCollection, bill.event);
    this.membersSharedCollection = this.memberService.addMemberToCollectionIfMissing<IMember>(this.membersSharedCollection, bill.payer);
  }

  protected loadRelationshipsOptions(): void {
    this.eventService
      .query()
      .pipe(map((res: HttpResponse<IEvent[]>) => res.body ?? []))
      .pipe(map((events: IEvent[]) => this.eventService.addEventToCollectionIfMissing<IEvent>(events, this.bill?.event)))
      .subscribe((events: IEvent[]) => (this.eventsSharedCollection = events));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.bill?.payer)))
      .subscribe((members: IMember[]) => (this.membersSharedCollection = members));
  }
}
