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
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class TeamUpdateComponent implements OnInit {
  isSaving = false;
  team: ITeam | null = null;
  allMembers: IMember[] = [];
  selectedMembers: IMember[] = [];

  private teamMemberService = inject(TeamMemberService);
  private memberService = inject(MemberService);
  protected teamService = inject(TeamService);
  protected teamFormService = inject(TeamFormService);
  protected activatedRoute = inject(ActivatedRoute);

  editForm: TeamFormGroup = this.teamFormService.createTeamFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ team }) => {
      this.team = team;
      if (team) this.updateForm(team);
      if (team?.id)
        this.teamMemberService.query({ 'teamId.equals': team.id }).subscribe(res => {
          this.selectedMembers = (res.body ?? []).map(tm => tm.member!) as IMember[];
        });
    });

    this.memberService.query().subscribe(res => (this.allMembers = res.body ?? []));
  }

  get availableMembers(): IMember[] {
    const usedIds = this.selectedMembers.map(m => m.id);
    return this.allMembers.filter(m => !usedIds.includes(m.id));
  }

  onMemberToggle(member: IMember, e: Event): void {
    const checked = (e.target as HTMLInputElement).checked;
    this.selectedMembers = checked ? [...this.selectedMembers, member] : this.selectedMembers.filter(m => m.id !== member.id);
  }

  save(): void {
    if (!this.selectedMembers.length) return alert('Select at least one member.');

    this.isSaving = true;
    const teamToSave = this.teamFormService.getTeam(this.editForm);

    const saveObs: Observable<HttpResponse<ITeam>> =
      teamToSave.id !== null ? this.teamService.update(teamToSave) : this.teamService.create(teamToSave);

    saveObs.subscribe({
      next: res => this.syncTeamMembers(res.body!.id!),
      error: () => this.onSaveError(),
    });
  }

  private syncTeamMembers(teamId: number): void {
    this.teamMemberService.query({ 'teamId.equals': teamId }).subscribe(res => {
      const existing = res.body ?? [];
      const existingIds = existing.map(tm => tm.member!.id!);
      const selectedIds = this.selectedMembers.map(m => m.id!);

      const toAdd = this.selectedMembers.filter(m => !existingIds.includes(m.id!));
      const toRemove = existing.filter(tm => !selectedIds.includes(tm.member!.id!));

      const ops = [
        ...toAdd.map(m => this.teamMemberService.create({ id: null, joinedAt: dayjs(), team: { id: teamId }, member: { id: m.id! } })),
        ...toRemove.map(tm => this.teamMemberService.delete(tm.id!)),
      ];

      forkJoin(ops)
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

  protected onSaveError(): void {}
  protected updateForm(team: ITeam): void {
    this.team = team;
    this.teamFormService.resetForm(this.editForm, team);
  }
}
