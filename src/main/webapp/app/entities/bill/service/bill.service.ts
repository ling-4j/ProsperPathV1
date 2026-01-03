import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IBill, NewBill } from '../bill.model';

export type PartialUpdateBill = Partial<IBill> & Pick<IBill, 'id'>;

type RestOf<T extends IBill | NewBill> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestBill = RestOf<IBill>;

export type NewRestBill = RestOf<NewBill>;

export type PartialUpdateRestBill = RestOf<PartialUpdateBill>;

export type EntityResponseType = HttpResponse<IBill>;
export type EntityArrayResponseType = HttpResponse<IBill[]>;

@Injectable({ providedIn: 'root' })
export class BillService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/bills');

  create(bill: NewBill): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(bill);
    return this.http.post<RestBill>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(bill: IBill): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(bill);
    return this.http
      .put<RestBill>(`${this.resourceUrl}/${this.getBillIdentifier(bill)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(bill: PartialUpdateBill): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(bill);
    return this.http
      .patch<RestBill>(`${this.resourceUrl}/${this.getBillIdentifier(bill)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestBill>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestBill[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  getBillIdentifier(bill: Pick<IBill, 'id'>): number {
    return bill.id;
  }

  compareBill(o1: Pick<IBill, 'id'> | null, o2: Pick<IBill, 'id'> | null): boolean {
    return o1 && o2 ? this.getBillIdentifier(o1) === this.getBillIdentifier(o2) : o1 === o2;
  }

  addBillToCollectionIfMissing<Type extends Pick<IBill, 'id'>>(
    billCollection: Type[],
    ...billsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const bills: Type[] = billsToCheck.filter(isPresent);
    if (bills.length > 0) {
      const billCollectionIdentifiers = billCollection.map(billItem => this.getBillIdentifier(billItem));
      const billsToAdd = bills.filter(billItem => {
        const billIdentifier = this.getBillIdentifier(billItem);
        if (billCollectionIdentifiers.includes(billIdentifier)) {
          return false;
        }
        billCollectionIdentifiers.push(billIdentifier);
        return true;
      });
      return [...billsToAdd, ...billCollection];
    }
    return billCollection;
  }

  protected convertDateFromClient<T extends IBill | NewBill | PartialUpdateBill>(bill: T): RestOf<T> {
    return {
      ...bill,
      createdAt: bill.createdAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restBill: RestBill): IBill {
    return {
      ...restBill,
      createdAt: restBill.createdAt ? dayjs(restBill.createdAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestBill>): HttpResponse<IBill> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestBill[]>): HttpResponse<IBill[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
