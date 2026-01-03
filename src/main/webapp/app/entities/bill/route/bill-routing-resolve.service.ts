import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IBill } from '../bill.model';
import { BillService } from '../service/bill.service';

const billResolve = (route: ActivatedRouteSnapshot): Observable<null | IBill> => {
  const id = route.params.id;
  if (id) {
    return inject(BillService)
      .find(id)
      .pipe(
        mergeMap((bill: HttpResponse<IBill>) => {
          if (bill.body) {
            return of(bill.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default billResolve;
