import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISummary } from '../summary.model';
import { SummaryService } from '../service/summary.service';

const summaryResolve = (route: ActivatedRouteSnapshot): Observable<null | ISummary> => {
  const id = route.params.id;
  if (id) {
    return inject(SummaryService)
      .find(id)
      .pipe(
        mergeMap((summary: HttpResponse<ISummary>) => {
          if (summary.body) {
            return of(summary.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default summaryResolve;
