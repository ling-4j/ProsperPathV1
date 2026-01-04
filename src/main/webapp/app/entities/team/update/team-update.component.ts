import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ITeam } from '../team.model';
import { TeamService } from '../service/team.service';
import { TeamFormGroup, TeamFormService } from './team-form.service';
import { IMember } from 'app/entities/member/member.model';
import { TeamMemberService } from 'app/entities/team-member/service/team-member.service';
import { ITeamMember, NewTeamMember } from 'app/entities/team-member/team-member.model';
import { MemberService } from 'app/entities/member/service/member.service';
import dayjs from 'dayjs/esm';

@Component({
  selector: 'jhi-team-update',
  templateUrl: './team-update.component.html',
  styleUrls: ['./team-update.component.scss'],
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TeamUpdateComponent implements OnInit {
  isSaving = false;
  team: ITeam | null = null;
  allMembers: IMember[] = [];
  selectedMembers: IMember[] = [];
  isAllSelected = false; // Tracks state of "Select All" checkbox

  private teamMemberService = inject(TeamMemberService);
  private memberService = inject(MemberService);
  protected teamService = inject(TeamService);
  protected teamFormService = inject(TeamFormService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: TeamFormGroup = this.teamFormService.createTeamFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ team }) => {
      this.team = team || null;
      if (team) {
        this.updateForm(team);
      }

      if (team?.id) {
        this.teamMemberService.query({ 'teamId.equals': team.id }).subscribe(res => {
          this.selectedMembers = (res.body ?? []).map(tm => tm.member!).filter(Boolean);
          this.updateSelectAllState();
        });
      } else {
        this.selectedMembers = [];
        this.updateSelectAllState();
      }
    });

    this.memberService.query().subscribe(res => {
      this.allMembers = res.body ?? [];
      this.updateSelectAllState();
    });
  }

  // Helper: Is a specific member selected?
  isMemberSelected(member: IMember): boolean {
    return this.selectedMembers.some(m => m.id === member.id);
  }

  // New: Toggle individual member
  onMemberToggle(member: IMember, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      if (!this.isMemberSelected(member)) {
        this.selectedMembers = [...this.selectedMembers, member];
      }
    } else {
      this.selectedMembers = this.selectedMembers.filter(m => m.id !== member.id);
    }

    this.updateSelectAllState();
  }

  // New: Select All / Deselect All
  toggleSelectAll(event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (checked) {
      this.selectedMembers = [...this.allMembers];
    } else {
      this.selectedMembers = [];
    }

    this.isAllSelected = checked;
  }

  // Sync "Select All" checkbox state
  private updateSelectAllState(): void {
    if (this.allMembers.length === 0) {
      this.isAllSelected = false;
      return;
    }
    this.isAllSelected = this.allMembers.every(member => this.isMemberSelected(member));
  }

  get availableMembers(): IMember[] {
    const selectedIds = this.selectedMembers.map(m => m.id);
    return this.allMembers.filter(m => !selectedIds.includes(m.id));
  }

  save(): void {
    if (this.selectedMembers.length === 0) {
      return;
    }

    this.isSaving = true;
    const teamToSave = this.teamFormService.getTeam(this.editForm);

    const saveObs: Observable<HttpResponse<ITeam>> =
      teamToSave.id !== null ? this.teamService.update(teamToSave) : this.teamService.create(teamToSave);

    saveObs
      .pipe(
        finalize((): void => {
          // intentionally empty
        }),
      )
      .subscribe({
        next: res => this.syncTeamMembers(res.body!.id),
        error: (): void => this.onSaveError(),
      });
  }

  private syncTeamMembers(teamId: number): void {
    this.teamMemberService.query({ 'teamId.equals': teamId }).subscribe(res => {
      const existing = res.body ?? [];
      const existingMemberIds = existing.map(tm => tm.member?.id).filter(Boolean) as number[];
      const selectedMemberIds = this.selectedMembers.map(m => m.id);

      const toAdd = this.selectedMembers.filter(m => !existingMemberIds.includes(m.id));
      const toRemove = existing.filter(tm => !selectedMemberIds.includes(tm.member!.id));

      const operations: Observable<any>[] = [
        ...toAdd.map(m =>
          this.teamMemberService.create({
            id: null,
            joinedAt: dayjs(),
            team: { id: teamId },
            member: { id: m.id },
          } as NewTeamMember),
        ),
        ...toRemove.map(tm => this.teamMemberService.delete(tm.id)),
      ];

      if (operations.length === 0) {
        this.isSaving = false;
        this.previousState();
        return;
      }

      forkJoin(operations)
        .pipe(finalize(() => (this.isSaving = false)))
        .subscribe({
          next: () => this.previousState(),
          error: () => this.onSaveError(),
        });
    });
  }

  previousState(): void {
    window.history.back();
  }

  protected onSaveError(): void {
    this.isSaving = false;
  }

  protected updateForm(team: ITeam): void {
    this.team = team;
    this.teamFormService.resetForm(this.editForm, team);
  }
}
