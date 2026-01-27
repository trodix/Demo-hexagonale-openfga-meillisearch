import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const authHeader = authService.getAuthHeader();
  const tenantId = authService.getTenantId();

  if (authHeader && tenantId && !req.url.includes('/login')) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Basic ${authHeader}`,
        'X-Tenant-Id': tenantId
      }
    });
    return next(authReq);
  }

  return next(req);
};
