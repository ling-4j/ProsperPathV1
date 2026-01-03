import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISettlement, NewSettlement } from '../settlement.model';

export type PartialUpdateSettlement = Partial<ISettlement> & Pick<ISettlement, 'id'>;

export type EntityResponseType = HttpResponse<ISettlement>;
export type EntityArrayResponseType = HttpResponse<ISettlement[]>;

@Injectable({ providedIn: 'root' })
export class SettlementService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/settlements');

  create(settlement: NewSettlement): Observable<EntityResponseType> {
    return this.http.post<ISettlement>(this.resourceUrl, settlement, { observe: 'response' });
  }

  update(settlement: ISettlement): Observable<EntityResponseType> {
    return this.http.put<ISettlement>(`${this.resourceUrl}/${this.getSettlementIdentifier(settlement)}`, settlement, {
      observe: 'response',
    });
  }

  partialUpdate(settlement: PartialUpdateSettlement): Observable<EntityResponseType> {
    return this.http.patch<ISettlement>(`${this.resourceUrl}/${this.getSettlementIdentifier(settlement)}`, settlement, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ISettlement>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ISettlement[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getSettlementIdentifier(settlement: Pick<ISettlement, 'id'>): number {
    return settlement.id;
  }

  compareSettlement(o1: Pick<ISettlement, 'id'> | null, o2: Pick<ISettlement, 'id'> | null): boolean {
    return o1 && o2 ? this.getSettlementIdentifier(o1) === this.getSettlementIdentifier(o2) : o1 === o2;
  }

  addSettlementToCollectionIfMissing<Type extends Pick<ISettlement, 'id'>>(
    settlementCollection: Type[],
    ...settlementsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const settlements: Type[] = settlementsToCheck.filter(isPresent);
    if (settlements.length > 0) {
      const settlementCollectionIdentifiers = settlementCollection.map(settlementItem => this.getSettlementIdentifier(settlementItem));
      const settlementsToAdd = settlements.filter(settlementItem => {
        const settlementIdentifier = this.getSettlementIdentifier(settlementItem);
        if (settlementCollectionIdentifiers.includes(settlementIdentifier)) {
          return false;
        }
        settlementCollectionIdentifiers.push(settlementIdentifier);
        return true;
      });
      return [...settlementsToAdd, ...settlementCollection];
    }
    return settlementCollection;
  }
}
