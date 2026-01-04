import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IBill } from 'app/entities/bill/bill.model';
import { BillService } from 'app/entities/bill/service/bill.service';
import { IMember } from 'app/entities/member/member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import { BillParticipantService } from '../service/bill-participant.service';
import { IBillParticipant } from '../bill-participant.model';
import { BillParticipantFormGroup, BillParticipantFormService } from './bill-participant-form.service';

@Component({
  selector: 'jhi-bill-participant-update',
  templateUrl: './bill-participant-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class BillParticipantUpdateComponent implements OnInit {
  isSaving = false;
  billParticipant: IBillParticipant | null = null;

  billsSharedCollection: IBill[] = [];
  membersSharedCollection: IMember[] = [];

  protected billParticipantService = inject(BillParticipantService);
  protected billParticipantFormService = inject(BillParticipantFormService);
  protected billService = inject(BillService);
  protected memberService = inject(MemberService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: BillParticipantFormGroup = this.billParticipantFormService.createBillParticipantFormGroup();

  compareBill = (o1: IBill | null, o2: IBill | null): boolean => this.billService.compareBill(o1, o2);

  compareMember = (o1: IMember | null, o2: IMember | null): boolean => this.memberService.compareMember(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ billParticipant }) => {
      this.billParticipant = billParticipant;
      if (billParticipant) {
        this.updateForm(billParticipant);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const billParticipant = this.billParticipantFormService.getBillParticipant(this.editForm);
    if (billParticipant.id !== null) {
      this.subscribeToSaveResponse(this.billParticipantService.update(billParticipant));
    } else {
      this.subscribeToSaveResponse(this.billParticipantService.create(billParticipant));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IBillParticipant>>): void {
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

  protected updateForm(billParticipant: IBillParticipant): void {
    this.billParticipant = billParticipant;
    this.billParticipantFormService.resetForm(this.editForm, billParticipant);

    this.billsSharedCollection = this.billService.addBillToCollectionIfMissing<IBill>(this.billsSharedCollection, billParticipant.bill);
    this.membersSharedCollection = this.memberService.addMemberToCollectionIfMissing<IMember>(
      this.membersSharedCollection,
      billParticipant.member,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.billService
      .query()
      .pipe(map((res: HttpResponse<IBill[]>) => res.body ?? []))
      .pipe(map((bills: IBill[]) => this.billService.addBillToCollectionIfMissing<IBill>(bills, this.billParticipant?.bill)))
      .subscribe((bills: IBill[]) => (this.billsSharedCollection = bills));

    this.memberService
      .query()
      .pipe(map((res: HttpResponse<IMember[]>) => res.body ?? []))
      .pipe(map((members: IMember[]) => this.memberService.addMemberToCollectionIfMissing<IMember>(members, this.billParticipant?.member)))
      .subscribe((members: IMember[]) => (this.membersSharedCollection = members));
  }
}
