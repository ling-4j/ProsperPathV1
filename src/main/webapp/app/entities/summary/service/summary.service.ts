import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, catchError, map, throwError } from 'rxjs';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ISummary, NewSummary } from '../summary.model';

export type PartialUpdateSummary = Partial<ISummary> & Pick<ISummary, 'id'>;

type RestOf<T extends ISummary | NewSummary> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type RestSummary = RestOf<ISummary>;

export type NewRestSummary = RestOf<NewSummary>;

export type PartialUpdateRestSummary = RestOf<PartialUpdateSummary>;

export type EntityResponseType = HttpResponse<ISummary>;
export type EntityArrayResponseType = HttpResponse<ISummary[]>;

// Định nghĩa interface cho dữ liệu chi tiết tài chính
export interface DetailedFinancialData {
  labels: string[];
  incomeData: number[];
  expenseData: number[];
  progressRateData: number[];
}

@Injectable({ providedIn: 'root' })
export class SummaryService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/summaries');

  create(summary: NewSummary): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(summary);
    return this.http
      .post<RestSummary>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(summary: ISummary): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(summary);
    return this.http
      .put<RestSummary>(`${this.resourceUrl}/${this.getSummaryIdentifier(summary)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(summary: PartialUpdateSummary): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(summary);
    return this.http
      .patch<RestSummary>(`${this.resourceUrl}/${this.getSummaryIdentifier(summary)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestSummary>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestSummary[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }
  
  /**
   * Lấy dữ liệu tổng quan cho một kỳ cụ thể.
   * @param period Kỳ muốn lấy dữ liệu (week, month, year).
   * @returns Observable của ISummary.
   */
  getSummary(period: 'week' | 'month' | 'year'): Observable<ISummary> {
    const url = `${this.resourceUrl}/summary`;
    const params = { period };
    return this.http.get<RestSummary>(url, { params, observe: 'response' }).pipe(
      map(res => this.convertResponseFromServer(res).body as ISummary),
      catchError(error => {
        if (error.status === 404) {
          console.warn(`No summary found for period: ${period}`);
          return throwError(() => new Error('Summary not found'));
        } else if (error.status === 401) {
          console.warn('Unauthorized access');
          return throwError(() => new Error('Unauthorized'));
        }
        console.error('Error fetching summary:', error);
        return throwError(() => new Error('An error occurred while fetching the summary'));
      }),
    );
  }

  /**
   * Lấy phần trăm thay đổi tài chính cho một kỳ cụ thể.
   * @param period Kỳ muốn lấy dữ liệu (week, month, year).
   * @returns Observable của đối tượng chứa phần trăm thay đổi cho tài sản, thu nhập, chi phí và lợi nhuận.
   */
  getFinancialChange(period: 'week' | 'month' | 'year'): Observable<{
    assetsChangePercentage: number;
    incomeChangePercentage: number;
    expenseChangePercentage: number;
    profitChangePercentage: number;
  }> {
    const url = `${this.resourceUrl}/financial-change`;
    const params = { period };
    return this.http.get(url, { params, observe: 'response' }).pipe(
      map(
        (res: HttpResponse<any>) =>
          res.body as {
            assetsChangePercentage: number;
            incomeChangePercentage: number;
            expenseChangePercentage: number;
            profitChangePercentage: number;
          },
      ),
      catchError(error => {
        if (error.status === 401) {
          console.warn('Unauthorized access');
          return throwError(() => new Error('Unauthorized'));
        }
        console.error('Error fetching financial change:', error);
        return throwError(() => new Error('An error occurred while fetching financial change'));
      }),
    );
  }

  /**
   * Lấy dữ liệu chi tiết tài chính cho biểu đồ.
   * @param period Kỳ muốn lấy dữ liệu (week, month, year).
   * @returns Observable của DetailedFinancialData chứa nhãn và dữ liệu cho biểu đồ.
   */
  getDetailedFinancialData(period: 'week' | 'month' | 'year'): Observable<DetailedFinancialData> {
    const url = `${this.resourceUrl}/detailed`;
    const params = { period };
    return this.http.get<DetailedFinancialData>(url, { params }).pipe(
      map(res => res),
      catchError(error => {
        if (error.status === 404) {
          console.warn(`Không tìm thấy dữ liệu chi tiết cho kỳ: ${period}`);
          return throwError(() => new Error('Detailed data not found'));
        } else if (error.status === 401) {
          console.warn('Không có quyền truy cập');
          return throwError(() => new Error('Unauthorized'));
        }
        console.error('Lỗi khi lấy dữ liệu chi tiết tài chính:', error);
        return throwError(() => new Error('Đã xảy ra lỗi khi lấy dữ liệu chi tiết tài chính'));
      }),
    );
  }

  getSummaryIdentifier(summary: Pick<ISummary, 'id'>): number {
    return summary.id;
  }

  compareSummary(o1: Pick<ISummary, 'id'> | null, o2: Pick<ISummary, 'id'> | null): boolean {
    return o1 && o2 ? this.getSummaryIdentifier(o1) === this.getSummaryIdentifier(o2) : o1 === o2;
  }

  addSummaryToCollectionIfMissing<Type extends Pick<ISummary, 'id'>>(
    summaryCollection: Type[],
    ...summariesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const summaries: Type[] = summariesToCheck.filter(isPresent);
    if (summaries.length > 0) {
      const summaryCollectionIdentifiers = summaryCollection.map(summaryItem => this.getSummaryIdentifier(summaryItem));
      const summariesToAdd = summaries.filter(summaryItem => {
        const summaryIdentifier = this.getSummaryIdentifier(summaryItem);
        if (summaryCollectionIdentifiers.includes(summaryIdentifier)) {
          return false;
        }
        summaryCollectionIdentifiers.push(summaryIdentifier);
        return true;
      });
      return [...summariesToAdd, ...summaryCollection];
    }
    return summaryCollection;
  }

  protected convertDateFromClient<T extends ISummary | NewSummary | PartialUpdateSummary>(summary: T): RestOf<T> {
    return {
      ...summary,
      createdAt: summary.createdAt?.toJSON() ?? null,
      updatedAt: summary.updatedAt?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restSummary: RestSummary): ISummary {
    return {
      ...restSummary,
      createdAt: restSummary.createdAt ? dayjs(restSummary.createdAt) : undefined,
      updatedAt: restSummary.updatedAt ? dayjs(restSummary.updatedAt) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestSummary>): HttpResponse<ISummary> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestSummary[]>): HttpResponse<ISummary[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
