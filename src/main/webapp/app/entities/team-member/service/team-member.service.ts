import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ITeamMember, NewTeamMember } from '../team-member.model';

export type PartialUpdateTeamMember = Partial<ITeamMember> & Pick<ITeamMember, 'id'>;

type RestOf<T extends ITeamMember | NewTeamMember> = Omit<T, 'joinedAt'> & {
  joinedAt?: string | null;
};

export type RestTeamMember = RestOf<ITeamMember>;

export type NewRestTeamMember = RestOf<NewTeamMember>;

export type PartialUpdateRestTeamMember = RestOf<PartialUpdateTeamMember>;

export type EntityResponseType = HttpResponse<ITeamMember>;
export type EntityArrayResponseType = HttpResponse<ITeamMember[]>;

@Injectable({ providedIn: 'root' })
export class TeamMemberService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/team-members');

  create(teamMember: NewTeamMember): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(teamMember);
    return this.http
      .post<RestTeamMember>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(teamMember: ITeamMember): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(teamMember);
    return this.http
      .put<RestTeamMember>(`${this.resourceUrl}/${this.getTeamMemberIdentifier(teamMember)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(teamMember: PartialUpdateTeamMember): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(teamMember);
    return this.http
      .patch<RestTeamMember>(`${this.resourceUrl}/${this.getTeamMemberIdentifier(teamMember)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestTeamMember>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestTeamMember[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getTeamMemberIdentifier(teamMember: Pick<ITeamMember, 'id'>): number {
    return teamMember.id;
  }

  compareTeamMember(o1: Pick<ITeamMember, 'id'> | null, o2: Pick<ITeamMember, 'id'> | null): boolean {
    return o1 && o2 ? this.getTeamMemberIdentifier(o1) === this.getTeamMemberIdentifier(o2) : o1 === o2;
  }

  addTeamMemberToCollectionIfMissing<Type extends Pick<ITeamMember, 'id'>>(
    teamMemberCollection: Type[],
    ...teamMembersToCheck: (Type | null | undefined)[]
  ): Type[] {
    const teamMembers: Type[] = teamMembersToCheck.filter(isPresent);
    if (teamMembers.length > 0) {
      const teamMemberCollectionIdentifiers = teamMemberCollection.map(teamMemberItem => this.getTeamMemberIdentifier(teamMemberItem));
      const teamMembersToAdd = teamMembers.filter(teamMemberItem => {
        const teamMemberIdentifier = this.getTeamMemberIdentifier(teamMemberItem);
        if (teamMemberCollectionIdentifiers.includes(teamMemberIdentifier)) {
          return false;
        }
        teamMemberCollectionIdentifiers.push(teamMemberIdentifier);
        return true;
      });
      return [...teamMembersToAdd, ...teamMemberCollection];
    }
    return teamMemberCollection;
  }

  protected convertDateFromClient<T extends ITeamMember | NewTeamMember | PartialUpdateTeamMember>(teamMember: T): RestOf<T> {
    return {
      ...teamMember,
      joinedAt: teamMember.joinedAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restTeamMember: RestTeamMember): ITeamMember {
    return {
      ...restTeamMember,
      joinedAt: restTeamMember.joinedAt ? dayjs(restTeamMember.joinedAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestTeamMember>): HttpResponse<ITeamMember> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestTeamMember[]>): HttpResponse<ITeamMember[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
