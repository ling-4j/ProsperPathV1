import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { PeriodType } from 'app/entities/enumerations/period-type.model';
import { SummaryService } from '../service/summary.service';
import { ISummary } from '../summary.model';
import { SummaryFormGroup, SummaryFormService } from './summary-form.service';

@Component({
  selector: 'jhi-summary-update',
  templateUrl: './summary-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class SummaryUpdateComponent implements OnInit {
  isSaving = false;
  summary: ISummary | null = null;
  periodTypeValues = Object.keys(PeriodType);

  usersSharedCollection: IUser[] = [];

  protected summaryService = inject(SummaryService);
  protected summaryFormService = inject(SummaryFormService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: SummaryFormGroup = this.summaryFormService.createSummaryFormGroup();

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ summary }) => {
      this.summary = summary;
      if (summary) {
        this.updateForm(summary);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const summary = this.summaryFormService.getSummary(this.editForm);
    if (summary.id !== null) {
      this.subscribeToSaveResponse(this.summaryService.update(summary));
    } else {
      this.subscribeToSaveResponse(this.summaryService.create(summary));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISummary>>): void {
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

  protected updateForm(summary: ISummary): void {
    this.summary = summary;
    this.summaryFormService.resetForm(this.editForm, summary);

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, summary.user);
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.summary?.user)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }
}
