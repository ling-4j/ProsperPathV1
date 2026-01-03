import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IEventBalance } from '../event-balance.model';
import { EventBalanceService } from '../service/event-balance.service';

const eventBalanceResolve = (route: ActivatedRouteSnapshot): Observable<null | IEventBalance> => {
  const id = route.params.id;
  if (id) {
    return inject(EventBalanceService)
      .find(id)
      .pipe(
        mergeMap((eventBalance: HttpResponse<IEventBalance>) => {
          if (eventBalance.body) {
            return of(eventBalance.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default eventBalanceResolve;
