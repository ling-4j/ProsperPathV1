import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IBillParticipant, NewBillParticipant } from '../bill-participant.model';

export type PartialUpdateBillParticipant = Partial<IBillParticipant> & Pick<IBillParticipant, 'id'>;

export type EntityResponseType = HttpResponse<IBillParticipant>;
export type EntityArrayResponseType = HttpResponse<IBillParticipant[]>;

@Injectable({ providedIn: 'root' })
export class BillParticipantService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/bill-participants');

  create(billParticipant: NewBillParticipant): Observable<EntityResponseType> {
    return this.http.post<IBillParticipant>(this.resourceUrl, billParticipant, { observe: 'response' });
  }

  update(billParticipant: IBillParticipant): Observable<EntityResponseType> {
    return this.http.put<IBillParticipant>(`${this.resourceUrl}/${this.getBillParticipantIdentifier(billParticipant)}`, billParticipant, {
      observe: 'response',
    });
  }

  partialUpdate(billParticipant: PartialUpdateBillParticipant): Observable<EntityResponseType> {
    return this.http.patch<IBillParticipant>(`${this.resourceUrl}/${this.getBillParticipantIdentifier(billParticipant)}`, billParticipant, {
      observe: 'response',
    });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IBillParticipant>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IBillParticipant[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
  saveBillParticipants(billId: number, memberIds: number[]): Observable<HttpResponse<void>> {
    // Gọi POST lên endpoint mới
    return this.http.post<void>(`${this.resourceUrl}/save-bill-participants/${billId}`, memberIds, { observe: 'response' });
  }

  getBillParticipantIdentifier(billParticipant: Pick<IBillParticipant, 'id'>): number {
    return billParticipant.id;
  }

  compareBillParticipant(o1: Pick<IBillParticipant, 'id'> | null, o2: Pick<IBillParticipant, 'id'> | null): boolean {
    return o1 && o2 ? this.getBillParticipantIdentifier(o1) === this.getBillParticipantIdentifier(o2) : o1 === o2;
  }

  addBillParticipantToCollectionIfMissing<Type extends Pick<IBillParticipant, 'id'>>(
    billParticipantCollection: Type[],
    ...billParticipantsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const billParticipants: Type[] = billParticipantsToCheck.filter(isPresent);
    if (billParticipants.length > 0) {
      const billParticipantCollectionIdentifiers = billParticipantCollection.map(billParticipantItem =>
        this.getBillParticipantIdentifier(billParticipantItem),
      );
      const billParticipantsToAdd = billParticipants.filter(billParticipantItem => {
        const billParticipantIdentifier = this.getBillParticipantIdentifier(billParticipantItem);
        if (billParticipantCollectionIdentifiers.includes(billParticipantIdentifier)) {
          return false;
        }
        billParticipantCollectionIdentifiers.push(billParticipantIdentifier);
        return true;
      });
      return [...billParticipantsToAdd, ...billParticipantCollection];
    }
    return billParticipantCollection;
  }
}
