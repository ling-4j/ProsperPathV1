import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IEventBalance, NewEventBalance } from '../event-balance.model';

export type PartialUpdateEventBalance = Partial<IEventBalance> & Pick<IEventBalance, 'id'>;

export type EntityResponseType = HttpResponse<IEventBalance>;
export type EntityArrayResponseType = HttpResponse<IEventBalance[]>;

@Injectable({ providedIn: 'root' })
export class EventBalanceService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/event-balances');

  create(eventBalance: NewEventBalance): Observable<EntityResponseType> {
    return this.http.post<IEventBalance>(this.resourceUrl, eventBalance, { observe: 'response' });
  }

  update(eventBalance: IEventBalance): Observable<EntityResponseType> {
    return this.http.put<IEventBalance>(`${this.resourceUrl}/${this.getEventBalanceIdentifier(eventBalance)}`, eventBalance, {
      observe: 'response',
    });
  }

  partialUpdate(eventBalance: PartialUpdateEventBalance): Observable<EntityResponseType> {
    return this.http.patch<IEventBalance>(`${this.resourceUrl}/${this.getEventBalanceIdentifier(eventBalance)}`, eventBalance, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IEventBalance>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IEventBalance[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getEventBalanceIdentifier(eventBalance: Pick<IEventBalance, 'id'>): number {
    return eventBalance.id;
  }

  compareEventBalance(o1: Pick<IEventBalance, 'id'> | null, o2: Pick<IEventBalance, 'id'> | null): boolean {
    return o1 && o2 ? this.getEventBalanceIdentifier(o1) === this.getEventBalanceIdentifier(o2) : o1 === o2;
  }

  addEventBalanceToCollectionIfMissing<Type extends Pick<IEventBalance, 'id'>>(
    eventBalanceCollection: Type[],
    ...eventBalancesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const eventBalances: Type[] = eventBalancesToCheck.filter(isPresent);
    if (eventBalances.length > 0) {
      const eventBalanceCollectionIdentifiers = eventBalanceCollection.map(eventBalanceItem =>
        this.getEventBalanceIdentifier(eventBalanceItem),
      );
      const eventBalancesToAdd = eventBalances.filter(eventBalanceItem => {
        const eventBalanceIdentifier = this.getEventBalanceIdentifier(eventBalanceItem);
        if (eventBalanceCollectionIdentifiers.includes(eventBalanceIdentifier)) {
          return false;
        }
        eventBalanceCollectionIdentifiers.push(eventBalanceIdentifier);
        return true;
      });
      return [...eventBalancesToAdd, ...eventBalanceCollection];
    }
    return eventBalanceCollection;
  }
}
